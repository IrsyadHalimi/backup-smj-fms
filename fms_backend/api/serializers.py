import time
import re
import uuid  # Tambahkan import UUID untuk pengaman cadangan tingkat tinggi
from rest_framework import serializers
from .models import FuelTransaction

class FuelTransactionSerializer(serializers.ModelSerializer):
    id_transaction = serializers.CharField(validators=[], required=False, allow_null=True, allow_blank=True)
    transaction_code = serializers.CharField(validators=[], required=False, allow_null=True, allow_blank=True)

    class Meta:
        model = FuelTransaction
        fields = '__all__'

    def to_internal_value(self, data):
        if hasattr(data, 'dict'):
            raw_data = data.dict()
        else:
            raw_data = data.copy() if hasattr(data, 'copy') else dict(data)

        # =====================================================================
        # 1. EXPLICIT MAPPING (Amankan CamelCase ke snake_case)
        # =====================================================================
        mapping_lain = {
            'idTransaction': 'id_transaction',
            'transactionCode': 'transaction_code',
            'qtyValue': 'qty_value',
            'fillingActivity': 'filling_activity',
            'poNumber': 'po_number',
            'vendorName': 'vendor_name',
            'vendorDeliveryOrder': 'vendor_delivery_order',
            'fotoSuratJalan': 'foto_surat_jalan',
            'locationTujuanId': 'location_tujuan_id',
            'idTransactionReference': 'id_transaction_reference',
            'pitLocationId': 'pit_location_id',
            'locationId': 'location_id',
        }

        for camel_key, snake_key in mapping_lain.items():
            if camel_key in raw_data and (snake_key not in raw_data or raw_data[snake_key] in [None, '', 'null']):
                raw_data[snake_key] = raw_data[camel_key]

        # =====================================================================
        # 2. INTERSEPSI AWAL: PAKSA PIT_LOCATION_ID SAMA DENGAN LOCATION_ID (FIXED)
        # =====================================================================
        loc_val = raw_data.get('location_id')

        # Bersihkan string array bawaan flutter jikalau ada kotoran string "["
        if loc_val is not None and '[' in str(loc_val):
            angka_loc = re.findall(r'\d+', str(loc_val))
            loc_val = angka_loc[0] if angka_loc else None

        # Kunci mati: Paksa pit_location_id mengikuti isi dari location_id kiriman HP
        # FIXED SECURITY: Hanya timpa jika pit_location_id dari lapangan benar-benar kosong/tidak valid
        pit_val = raw_data.get('pit_location_id') or loc_val

        # Suntik kembali ke raw_data dengan manajemen tipe data yang aman (Anti-Crash Alphanumeric)
        if pit_val is not None:
            try:
                # Jika ID berupa nominal angka murni, konversi ke Integer agar disukai PostgreSQL Foreign Key
                raw_data['pit_location_id'] = int(float(str(pit_val)))
            except ValueError:
                # Jika ID mengandung huruf (ex: FT2801), biarkan lolos sebagai String/Raw text asli
                raw_data['pit_location_id'] = str(pit_val).strip()

        # Bersihkan paksa key bayangan 'pit_location' agar tidak mengotori DRF validation map
        raw_data.pop('pit_location', None)

        # =====================================================================
        # 3. AUTO DATE NORMALIZER (JETTY FIX: DD-MM-YYYY -> YYYY-MM-DD)
        # =====================================================================
        if 'date' in raw_data and raw_data['date']:
            date_str = str(raw_data['date']).strip()
            if '-' in date_str and len(date_str) == 10:
                parts = date_str.split('-')
                if len(parts) == 3 and len(parts[0]) == 2 and len(parts[2]) == 4:
                    raw_data['date'] = f"{parts[2]}-{parts[1]}-{parts[0]}"

        # =====================================================================
        # 4. ANTI 400 BAD REQUEST FOR NUMERIC FIELDS
        # =====================================================================
        for f in ['qty_value', 'consumed_qty', 'hm_km_unit', 'last_hm', 'flow_meter_value']:
            if f in raw_data and str(raw_data[f]).strip().lower() in ['null', 'none', '']:
                raw_data[f] = 0.0

        for f in ['final_hm_value', 'final_flow_value', 'ai_hm_read', 'ai_flow_read']:
            if f in raw_data and str(raw_data[f]).strip().lower() in ['null', 'none', '']:
                raw_data[f] = None

        try:
            return super().to_internal_value(raw_data)
        except serializers.ValidationError as e:
            return raw_data

    def create(self, validated_data):
        insert_data = {}
        model_fields = [f.name for f in FuelTransaction._meta.fields]

        for field_name in model_fields:
            if field_name == 'id':
                continue
                
            val = validated_data.get(field_name)

            if val is not None and hasattr(val, 'pk'):
                val = val.pk

            if val is not None and isinstance(val, str):
                val_str = val.strip()
                if "['" in val_str or "[" in val_str:
                    angka_saja = re.findall(r'\d+', val_str)
                    val = int(angka_saja[0]) if angka_saja else 1

            if val is None or str(val).strip().lower() in ['null', 'none', '']:
                if field_name.endswith('_status') or field_name in [
                    'po_number', 'vendor_name', 'vendor_delivery_order', 
                    'foto_surat_jalan', 'location_tujuan_id', 'id_transaction_reference',
                    'hm_status', 'flow_status'
                ]:
                    insert_data[field_name] = ""
                elif field_name in ['is_transferred', 'is_sync', 'status']:
                    insert_data[field_name] = 0
                else:
                    insert_data[field_name] = None
            else:
                insert_data[field_name] = val

        # Konversi tipe data numerik
        for num_field in ['qty_value', 'consumed_qty', 'hm_km_unit', 'last_hm', 'flow_meter_value']:
            if num_field in insert_data and insert_data[num_field] is not None:
                try:
                    insert_data[num_field] = float(insert_data[num_field])
                except:
                    insert_data[num_field] = 0.0

        # Konversi tipe data integer
        for int_field in ['is_transferred', 'is_sync', 'flowmeter_id', 'pit_location_id', 'location_id']:
            if int_field in insert_data and insert_data[int_field] is not None:
                try:
                    insert_data[int_field] = int(float(str(insert_data[int_field])))
                except:
                    pass

        if 'request' in self.context:
            request = self.context['request']
            if request and request.FILES:
                for file_key in request.FILES:
                    if file_key in model_fields:
                        insert_data[file_key] = request.FILES[file_key]

        # =====================================================================
        # 🔥 CRITICAL PERBAIKAN: RE-STRUCTURE ID_TRANSACTION ANTI-BENTROK MULTI-HP
        # =====================================================================
        # Ambil kode pengenal unit (bisa pakai gas_station atau nama unit sap fuel truck)
        unit_identifier = str(insert_data.get('gas_station') or insert_data.get('no_unit_sap') or 'UNK_UNIT').strip().replace(" ", "_")
        raw_id_trans = insert_data.get('id_transaction')
        
        # Eksekusi Pembuatan ID Transaksi Unik Bergaransi Lintas Device
        if not raw_id_trans or str(raw_id_trans).strip().lower() in ['null', 'none', '']:
            # Jika kosong, buat fallback kombinasi Timestamp + Nama Unit + String Acak
            string_acak = uuid.uuid4().hex[:6].upper()
            id_trans = f"TRX-{unit_identifier}-{int(time.time())}-{string_acak}"
        else:
            # JIKA HP MENGIRIM ID (Misal angka increment: 1, 2, 3), RE-FORMAT DI SERVER:
            # Format Baru: TX-FT2801-1 (ID bawaan HP tidak akan pernah bentrok lagi dengan milik FT2802)
            if not str(raw_id_trans).startswith("TX-"):
                id_trans = f"TX-{unit_identifier}-{raw_id_trans}"
            else:
                id_trans = str(raw_id_trans)

        insert_data['id_transaction'] = id_trans
        # =====================================================================

        trx_code = insert_data.get('transaction_code')
        if not trx_code or str(trx_code).strip().lower() in ['null', 'none', '']:
            insert_data['transaction_code'] = f"GRV-{int(time.time())}"
        else:
            if FuelTransaction.objects.filter(transaction_code=trx_code).exclude(id_transaction=id_trans).exists():
                insert_data['transaction_code'] = f"{trx_code}-{int(time.time())}"

        # Saring defaults_data agar HANYA berisi field yang mutlak ada di database
        defaults_data = {k: v for k, v in insert_data.items() if k != 'id_transaction'}

        # =====================================================================
        # 5. PROTEKSI DATA DB LAMA (Mencegah Overwrite Nilai 1 Saat Re-Sync)
        # =====================================================================
        if id_trans:
            existing_trx = FuelTransaction.objects.filter(id_transaction=id_trans).first()
            if existing_trx:
                # Proteksi Status agar tidak turun kasta ke pending jika sudah diverifikasi di React
                if existing_trx.screening_status in ['Verified', 'Downloaded']:
                    defaults_data['screening_status'] = existing_trx.screening_status
                    defaults_data['final_hm_value'] = existing_trx.final_hm_value
                    defaults_data['final_flow_value'] = existing_trx.final_flow_value

                # Proteksi field location_id
                if 'location_id' in defaults_data and (defaults_data['location_id'] in [None, '', 0, 1, '1']):
                    val_loc = getattr(existing_trx, 'location_id', None)
                    if val_loc:
                        defaults_data['location_id'] = getattr(val_loc, 'pk', val_loc)

                # Proteksi field pit_location_id
                if 'pit_location_id' in defaults_data and (defaults_data['pit_location_id'] in [None, '', 0, 1, '1']):
                    val_pit = getattr(existing_trx, 'pit_location_id', None)
                    if val_pit:
                        defaults_data['pit_location_id'] = getattr(val_pit, 'pk', val_pit)

        # =====================================================================
        # 6. PENYELAMATAN AKHIR PIT_LOCATION_ID (DIKUNCI TOTAL KE LOCATION_ID)
        # =====================================================================
        final_location = defaults_data.get('location_id')
        
        if final_location is not None and str(final_location).strip().lower() not in ['null', 'none', '', '0', '1']:
            if not defaults_data.get('pit_location_id'):
                defaults_data['pit_location_id'] = final_location
        else:
            defaults_data['pit_location_id'] = defaults_data.get('pit_location_id') or 1

        final_pit = defaults_data['pit_location_id']
        try:
            defaults_data['pit_location_id'] = int(float(str(final_pit)))
        except ValueError:
            defaults_data['pit_location_id'] = str(final_pit).strip()

        defaults_data.pop('pit_location', None)

        # =====================================================================
        # 7. EKSEKUSI SAVE / UPDATE_OR_CREATE (SAFE DOUBLE-ROUTE LOGIC)
        # =====================================================================
        try:
            transaction, created = FuelTransaction.objects.update_or_create(
                id_transaction=id_trans,
                defaults=defaults_data
            )
            return transaction
        
        except Exception as db_err:
            err_msg = str(db_err).lower()
            if "status" in err_msg or "cannot be null" in err_msg or "incorrect integer value" in err_msg:
                for f_name in insert_data:
                    if f_name.endswith('_status') and insert_data[f_name] == "":
                        defaults_data[f_name] = 0
                
                if "flow_status" in err_msg: defaults_data['flow_status'] = 0
                if "hm_status" in err_msg: defaults_data['hm_status'] = 0
                
                if "location_id" in err_msg and defaults_data.get('location_id') is None:
                    defaults_data['location_id'] = 1
                if "pit_location_id" in err_msg and defaults_data.get('pit_location_id') is None:
                    defaults_data['pit_location_id'] = defaults_data.get('location_id') or 1
                    
                defaults_data.pop('pit_location', None)

                transaction, created = FuelTransaction.objects.update_or_create(
                    id_transaction=id_trans,
                    defaults=defaults_data
                )
                return transaction
                
            raise db_err