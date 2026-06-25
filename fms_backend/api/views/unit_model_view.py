import pandas as pd
from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from ..models import ModelUnit
from ..serializers import ModelUnitSerializer

class ModelUnitViewSet(viewsets.ModelViewSet):
    """
    Endpoint CRUD otomatis untuk ModelUnit.
    Mendukung: GET, POST, PUT, PATCH, DELETE, dan UPLOAD EXCEL.
    """
    queryset = ModelUnit.objects.all().order_by('-created_at')
    serializer_class = ModelUnitSerializer

    @action(detail=False, methods=['post'], url_path='upload-excel')
    def upload_excel(self, request):
        """
        Endpoint untuk import massal ModelUnit via file Excel.
        Format Kolom Excel yang dibutuhkan: 'unit_model' dan 'desc'
        """
        file_obj = request.FILES.get('file')
        
        # Validasi jika file tidak dikirim
        if not file_obj:
            return Response(
                {"error": "Format salah. Sediakan file Excel dengan key 'file'."}, 
                status=status.HTTP_400_BAD_REQUEST
            )

        # Validasi ekstensi file
        if not file_obj.name.endswith(('.xlsx', '.xls')):
            return Response(
                {"error": "Format file tidak didukung. Harus berupa file .xlsx atau .xls"}, 
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            # Membaca excel menggunakan pandas
            df = pd.read_excel(file_obj)

            # Validasi ketersediaan kolom wajib
            if 'unit_model' not in df.columns:
                return Response(
                    {"error": "Kolom 'unit_model' wajib ada di dalam file Excel."}, 
                    status=status.HTTP_400_BAD_REQUEST
                )

            success_count = 0
            errors = []

            # Lakukan perulangan baris demi baris di Excel
            for index, row in df.iterrows():
                unit_model_val = str(row['unit_model']).strip()
                desc_val = str(row['desc']).strip() if 'desc' in df.columns and pd.notna(row['desc']) else None

                # Skip jika baris unit_model kosong
                if not unit_model_val or unit_model_val == 'nan':
                    continue

                # Gunakan update_or_create agar data yang duplikat otomatis ter-update (tidak crash)
                try:
                    ModelUnit.objects.update_or_create(
                        unit_model=unit_model_val,
                        defaults={'desc': desc_val}
                    )
                    success_count += 1
                except Exception as e:
                    errors.append(f"Baris {index + 2}: Gagal menyimpan {unit_model_val} ({str(e)})")

            # Response Akhir
            return Response({
                "message": f"Proses import selesai. {success_count} data berhasil dimasukkan/diperbarui.",
                "failed_rows": errors
            }, status=status.HTTP_200_OK if not errors else status.HTTP_270_MULTI_STATUS)

        except Exception as e:
            return Response(
                {"error": f"Gagal membaca file Excel: {str(e)}"}, 
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )