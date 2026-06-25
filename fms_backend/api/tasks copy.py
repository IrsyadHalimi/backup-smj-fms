# api/tasks.py
from celery import shared_task
from .models import FuelTransaction
from .gemini_service import GeminiScreeningService
import logging
from django.utils import timezone

from channels.layers import get_channel_layer
from asgiref.sync import async_to_sync

logger = logging.getLogger(__name__)

@shared_task
def task_process_ai_screening(transaction_id):
    try:
        # 1. Ambil data transaksi saat ini
        current_trx = FuelTransaction.objects.get(id=transaction_id)
        current_trx.screening_status = 'Processing'
        current_trx.save()

        # 2. Cari data terakhir untuk unit yang sama (untuk konteks Last HM)
        last_hm_trx = FuelTransaction.objects.filter(
            tech_id=current_trx.tech_id
        ).exclude(id=transaction_id).order_by('-id').first()
        
        last_hm_val = last_hm_trx.hm_km_unit if last_hm_trx else 0.0

        # 3. Cari data Flow Meter terakhir (Global)
        last_flow_trx = FuelTransaction.objects.exclude(id=transaction_id).order_by('-id').first()
        last_flow_val = last_flow_trx.flow_meter_value if last_flow_trx else 0.0

        # 4. Tentukan Model Unit (bisa disesuaikan logikanya)
        # Contoh: jika tech_id mengandung 'HD78', kita set model ke HD785
        unit_model = "GENERIC"
        if "HD78" in current_trx.tech_id:
            unit_model = "HD785"
        elif "HW" in current_trx.tech_id:
            unit_model = "HONEYWELL"

        # 5. Validasi foto berdasarkan jenis transaksi
        is_initial = current_trx.entry_type.upper() == "INITIAL"

        # INITIAL → hanya flow meter wajib
        if is_initial:
            if not current_trx.flow_meter_foto:
                current_trx.screening_status = "Error Initial Flow Photo Missing"
                current_trx.save()
                logger.warning(
                    f"Trx {transaction_id} skipped: initial flow photo missing"
                )
                return

            hm_image_path = None
            flow_image_path = current_trx.flow_meter_foto.path

        # NORMAL → HM + Flow wajib
        else:
            if not current_trx.hm_foto:
                current_trx.screening_status = "Error HM Photo Missing"
                current_trx.save()
                logger.warning(
                    f"Trx {transaction_id} skipped: hm_foto missing"
                )
                return

            if not current_trx.flow_meter_foto:
                current_trx.screening_status = "Error Flow Photo Missing"
                current_trx.save()
                logger.warning(
                    f"Trx {transaction_id} skipped: flow_meter_foto missing"
                )
                return

            hm_image_path = current_trx.hm_foto.path
            flow_image_path = current_trx.flow_meter_foto.path

        # 6. Jalankan Screening AI
        ai_service = GeminiScreeningService()

        ai_results = ai_service.screen_fuel_images(
            hm_image_path=hm_image_path,
            flow_image_path=flow_image_path,
            unit_model=unit_model,
            last_hm=last_hm_val,
            last_flow=last_flow_val
        )

        # 7. Validasi Hasil (Logika Sinkronisasi Baru)
        if ai_results["status"] == "success":
            current_trx.ai_hm_read = ai_results["hm"]
            current_trx.ai_flow_read = ai_results["flow"]
            
            # Toleransi selisih
            hm_is_match = abs(current_trx.hm_km_unit - (ai_results["hm"] or 0)) <= 0.5
            flow_is_match = abs(current_trx.flow_meter_value - (ai_results["flow"] or 0)) <= 1.0

            # Jika INITIAL, HM otomatis dianggap Match
            if is_initial:
                hm_is_match = True

            # Logika Status Baru
            if hm_is_match and flow_is_match:
                current_trx.screening_status = 'Verified'
            elif hm_is_match and not flow_is_match:
                current_trx.screening_status = 'HM Match'
            elif not hm_is_match and flow_is_match:
                current_trx.screening_status = 'Flow Match'
            else:
                current_trx.screening_status = 'Mismatch'
        else:
            current_trx.screening_status = 'Error AI'
        
        # Set date_screening tepat sebelum save agar React bisa memfilter
        current_trx.date_screening = timezone.now().date()

        current_trx.save()
        logger.info(f"Screening completed for Trx {transaction_id}: {current_trx.screening_status}")

        # ---Sinyal Selesai ke React ---
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            "fuel_sync_group", # Harus sama dengan group_name di consumers.py
            {
                "type": "send_new_record", # Fungsi di consumers.py yang akan dipanggil
                "data": {
                    "id": current_trx.id,
                    "screening_status": current_trx.screening_status,
                    "ai_hm_read": current_trx.ai_hm_read,
                    "ai_flow_read": current_trx.ai_flow_read,
                    "no_unit_sap": current_trx.no_unit_sap,
                }
            }
        )

    except FuelTransaction.DoesNotExist:
        logger.error(f"Transaction ID {transaction_id} not found.")
    except Exception as e:
        logger.error(f"Task Error: {str(e)}")