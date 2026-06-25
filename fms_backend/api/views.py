from django.shortcuts import render 
from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from .models import FuelTransaction
from .serializers import FuelTransactionSerializer

from django.db.models import Sum, Count
from django.utils import timezone
from datetime import timedelta

# Import utilities untuk menangani WebSocket dari lingkungan sync ke async
from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer

# TAMBAHAN PERBAIKAN CASE: Class khusus untuk mengontrol pagination server-side
from rest_framework.pagination import PageNumberPagination

from api.services.fuel_explore_service import FuelExploreService
from django.http import JsonResponse


class FuelTransactionPagination(PageNumberPagination):
    page_size = 50                  # Default baris jika frontend tidak mengirim limit
    page_size_query_param = 'limit' # Menangkap parameter ?limit= dari React
    max_page_size = 2000            # Batas maksimal baris yang diizinkan

class FuelTransactionViewSet(viewsets.ModelViewSet):
    queryset = FuelTransaction.objects.all().order_by('-id')
    serializer_class = FuelTransactionSerializer
    pagination_class = FuelTransactionPagination # Terapkan pagination untuk mengatasi ERR_CONNECTION_RESET

    def get_queryset(self):
        """
        Overriding get_queryset untuk menangkap parameter filter dinamis dari React panel atas.
        Pencarian distandarkan menggunakan icontains/iexact agar aman dari masalah Case-Sensitive.
        """
        queryset = FuelTransaction.objects.all().order_by('-id')
        params = self.request.query_params

        # 1. Filter Rentang Tanggal (Berdasarkan field 'date')
        start_date = params.get('start_date', None)
        end_date = params.get('end_date', None)
        if start_date:
            queryset = queryset.filter(date__gte=start_date)
        if end_date:
            queryset = queryset.filter(date__lte=end_date)

        # 2. Filter Storage (location_id)
        location_id = params.get('location_id', None)
        if location_id:
            queryset = queryset.filter(location_id=location_id)

        # 3. Filter Tech ID (Pencarian Parsial)
        tech_id = params.get('tech_id', None)
        if tech_id:
            queryset = queryset.filter(tech_id__icontains=tech_id)

        # 4. Filter No Unit SAP (Pencarian Parsial)
        no_unit_sap = params.get('no_unit_sap', None)
        if no_unit_sap:
            queryset = queryset.filter(no_unit_sap__icontains=no_unit_sap)

        # 5. Filter Shift (Exact Match - Case Insensitive)
        shift = params.get('shift', None)
        if shift:
            queryset = queryset.filter(shift__iexact=shift)

        # 6. Filter Screening Status / AI & OCR Status
        # Frontend mengirim ai_status & ocr_status. Kita petakan ke field database 'screening_status'
        ai_status = params.get('ai_status', None)
        if ai_status:
            queryset = queryset.filter(screening_status__icontains=ai_status)
            
        ocr_status = params.get('ocr_status', None)
        if ocr_status:
            queryset = queryset.filter(screening_status__icontains=ocr_status)

        return queryset

    def perform_create(self, serializer):
        """
        Di-override untuk memantau data kiriman dari HP di console VPS
        """
        # LINE DEBUGGING: Cetak payload mentah yang sudah divalidasi oleh serializer
        print("=== [DEBUG PAYLOAD GR RECEIVED BY DJANGO] ===")
        print("PO Number:", serializer.validated_data.get('po_number'))
        print("Vendor Name:", serializer.validated_data.get('vendor_name'))
        print("Trx Code:", serializer.validated_data.get('transaction_code'))
        print("ID Trx:", serializer.validated_data.get('id_transaction'))
        print("=============================================")

        # 1. Simpan transaksi ke database
        transaction = serializer.save()

        # 2. Kirim sinyal realtime ke React via WebSockets
        try:
            channel_layer = get_channel_layer()
            if channel_layer:
                async_to_sync(channel_layer.group_send)(
                    "fuel_sync_group",
                    {
                        "type": "fuel.message",
                        "id": transaction.id,
                        "status": "New Delivery"
                    }
                )
        except Exception as ws_err:
            print("Gagal broadcast WebSocket saat create:", str(ws_err))

        # 3. Alihkan pemrosesan gambar/OCR langsung ke Celery
        try:
            from api.tasks import task_process_ai_screening
            task_process_ai_screening.delay(transaction.id)
        except Exception as celery_err:
            print("Gagal memicu task Celery:", str(celery_err))

    # =========================================================
    # CUSTOM ACTIONS (VALIDATE & RE-SCREENING)
    # =========================================================
    @action(detail=True, methods=['patch', 'post'])
    def validate(self, request, pk=None):
        transaction = self.get_object() 
        print("Data diterima di Backend:", request.data) 

        hm_val = request.data.get('final_hm_value')
        flow_val = request.data.get('final_flow_value')
        status_val = request.data.get('screening_status', 'Verified')

        # Proteksi ekstra: Pastikan data tidak kosong ("") sebelum di-cast ke float
        if hm_val is not None and str(hm_val).strip() != "":
            try:
                transaction.final_hm_value = float(hm_val)
            except ValueError:
                pass
        if flow_val is not None and str(flow_val).strip() != "":
            try:
                transaction.final_flow_value = float(flow_val)
            except ValueError:
                pass
        
        # 🛡️ INTERVENSI UTAMA BACKEND:
        transaction.screening_status = status_val
        transaction.save()

        # 🔥 SOLUSI TOTAL RACE CONDITION CELERY:
        try:
            from core.celery import app as celery_app
            inspector = celery_app.control.inspect()
            active_tasks = inspector.active() or {}
            reserved_tasks = inspector.reserved() or {}
            
            all_tasks = []
            for worker, tasks in active_tasks.items():
                all_tasks.extend(tasks)
            for worker, tasks in reserved_tasks.items():
                all_tasks.extend(tasks)
                
            for task in all_tasks:
                if task.get('name') == 'api.tasks.task_process_ai_screening':
                    args = task.get('args', [])
                    if args and args[0] == transaction.id:
                        celery_app.control.revoke(task.get('id'), terminate=True, signal='SIGKILL')
                        print(f"=== [CELERY INTERVENTION] Berhasil mematikan paksa screening task ID: {task.get('id')} ===")
        except Exception as celery_kill_err:
            print("Gagal melakukan intervensi pembatalan task Celery:", str(celery_kill_err))

        # Kirim WebSocket realtime update ke dashboard
        try:
            channel_layer = get_channel_layer()
            if channel_layer:
                async_to_sync(channel_layer.group_send)(
                    "fuel_sync_group",
                    {
                        "type": "fuel.message",
                        "id": transaction.id,
                        "status": "Verified"
                    }
                )
        except Exception as e:
            print("Gagal broadcast WebSocket saat validate:", str(e))

        serializer = self.get_serializer(transaction)
        return Response(serializer.data, status=status.HTTP_200_OK)

    @action(detail=True, methods=['post'])
    def re_screening(self, request, pk=None):
        transaction = self.get_object()
        
        try:
            transaction.screening_status = 'Processing'
            transaction.ai_hm_read = None
            transaction.ai_flow_read = None
            transaction.save()

            # Beritahu React bahwa status berubah menjadi 'Processing'
            try:
                channel_layer = get_channel_layer()
                async_to_sync(channel_layer.group_send)(
                    "fuel_sync_group",
                    {
                        "type": "fuel.message",
                        "id": transaction.id,
                        "status": "Processing"
                    }
                )
            except Exception as ws_err:
                print("Gagal broadcast WebSocket saat re-screening:", str(ws_err))

            from api.tasks import task_process_ai_screening
            task_process_ai_screening.delay(transaction.id)

            serializer = self.get_serializer(transaction)
            return Response(serializer.data, status=status.HTTP_200_OK)
            
        except Exception as e:
            return Response(
                {"error": str(e)}, 
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )
    
    @action(detail=False, methods=['get'], url_path='operator-summary')
    def operator_summary(self, request):
        today = timezone.now().date()
        
        total_liters = FuelTransaction.objects.filter(
            created_at__date=today
        ).aggregate(Sum('qty_value'))['qty_value__sum'] or 0

        total_trx = FuelTransaction.objects.filter(
            created_at__date=today
        ).count()

        stock_percentage = 72 

        now = timezone.now()
        hourly_trend = []
        for i in range(6, 19, 2):
            hour_val = FuelTransaction.objects.filter(
                created_at__date=today,
                created_at__hour=i
            ).aggregate(Sum('qty_value'))['qty_value__sum'] or 0
            hourly_trend.append({"hour": f"{i:02d}:00", "qty": hour_val})

        recent_data = FuelTransaction.objects.all().order_by('-created_at')[:5]
        recent_feed = []
        for trx in recent_data:
            recent_feed.append({
                "unit": trx.unit_id if hasattr(trx, 'unit_id') else "Unknown", 
                "qty": trx.qty_value,
                "time": trx.created_at.strftime("%H:%M"),
                "status": "Synchronized" if trx.is_transferred else "Pending"
            })

        return Response({
            "summary": {
                "total_liters": round(total_liters, 2),
                "total_transactions": total_trx,
                "stock_level": stock_percentage,
                "system_status": "Online",
                "flowmeter": "Normal"
            },
            "charts": hourly_trend,
            "recent_activity": recent_feed
        })

    # =========================================================
    # CUSTOM ACTION: DASHBOARD KPI SUMMARY (REALTIME VALUE)
    # =========================================================
    @action(detail=False, methods=['get'], url_path='dashboard-kpis')
    def dashboard_kpis(self, request):
        """
        Endpoint khusus untuk menghitung akumulasi total data transaksi secara server-side
        menggunakan filter spesifik transaction_code & unit_class.
        """
        from django.db.models import Q

        # Ambil queryset dasar yang sudah terfilter jika panel filter atas digunakan
        base_queryset = self.get_queryset()

        # 1. TOTAL PENGELUARAN (OFI & TFO)
        total_ofi = base_queryset.filter(transaction_code__icontains='OFI').aggregate(total=Sum('consumed_qty'))['total'] or 0
        total_tfo = base_queryset.filter(transaction_code__icontains='TFO').aggregate(total=Sum('consumed_qty'))['total'] or 0
        total_pengeluaran = total_ofi + total_tfo

        # 2. TOTAL PENERIMAAN (GR & TFI)
        total_gr = base_queryset.filter(transaction_code__icontains='GR').aggregate(total=Sum('consumed_qty'))['total'] or 0
        total_tfi = base_queryset.filter(transaction_code__icontains='TFI').aggregate(total=Sum('consumed_qty'))['total'] or 0
        total_penerimaan = total_gr + total_tfi

        # 3. STOCK ON HAND
        stock_on_hand = total_penerimaan - total_pengeluaran

        # 4. TOTAL UNIT ACTIVE (Distinct tech_id atau no_unit_sap)
        # Menghitung unit unik dari tech_id
        tech_units = base_queryset.exclude(tech_id__isnull=True).exclude(tech_id='').values_list('tech_id', flat=True)
        # Menghitung unit unik dari no_unit_sap
        sap_units = base_queryset.exclude(no_unit_sap__isnull=True).exclude(no_unit_sap='').values_list('no_unit_sap', flat=True)
        
        # Gabungkan set untuk mendapatkan nilai total distinct murni lintas kolom
        total_unit_active = len(set(list(tech_units) + list(sap_units)))

        # 5. PRODUCTION EQUIPMENT SC (OFI & unit_class == 'Production')
        prod_equipment_sc = base_queryset.filter(
            transaction_code__icontains='OFI',
            unit_class__iexact='Production'
        ).aggregate(total=Sum('consumed_qty'))['total'] or 0

        # 6. NON PRODUCTION EQUIPMENT SC (OFI & unit_class == 'Non Production')
        non_prod_equipment_sc = base_queryset.filter(
            transaction_code__icontains='OFI',
            unit_class__iexact='Non Production'
        ).aggregate(total=Sum('consumed_qty'))['total'] or 0

        return Response({
            "total_pengeluaran": round(total_pengeluaran, 2),
            "total_ofi": round(total_ofi, 2),
            "total_tfo": round(total_tfo, 2),
            "total_penerimaan": round(total_penerimaan, 2),
            "total_gr": round(total_gr, 2),
            "total_tfi": round(total_tfi, 2),
            "stock_on_hand": round(stock_on_hand, 2),
            "total_unit_active": total_unit_active,
            "prod_equipment_sc": round(prod_equipment_sc, 2),
            "non_prod_equipment_sc": round(non_prod_equipment_sc, 2),
        }, status=status.HTTP_200_OK)

def fuel_monitor_view(request):
    transactions = FuelTransaction.objects.all().order_by('-id')[:50]
    return render(request, 'monitor.html', {'transactions': transactions})

def fuel_explore_data(request):

    payload = FuelExploreService.build_payload()

    return JsonResponse(payload)