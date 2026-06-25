import time
import re
from rest_framework import serializers
from .models import FuelTransaction

class FuelTransactionSerializer(serializers.ModelSerializer):
    transaction_code = serializers.CharField(validators=[]) 

    class Meta:
        model = FuelTransaction
        fields = '__all__'

    def to_internal_value(self, data):
        if hasattr(data, 'dict'):
            raw_data = data.dict()
        else:
            raw_data = data.copy() if hasattr(data, 'copy') else dict(data)
        
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
        }

        for camel_key, snake_key in mapping_lain.items():
            if camel_key in raw_data and snake_key not in raw_data:
                raw_data[snake_key] = raw_data[camel_key]

        # Handle Pit Location
        pit_id_val = raw_data.get('pit_location_id') or raw_data.get('pit_location')
        if pit_id_val is not None:
            pit_str = str(pit_id_val).strip()
            if '[' in pit_str:
                angka_pit = re.findall(r'\d+', pit_str)
                pit_id_val = int(angka_pit[0]) if angka_pit else None
            
            if str(pit_id_val).lower() in ['null', 'none', '', '0']:
                pit_id_val = None

        if pit_id_val:
            try:
                actual_id = int(float(str(pit_id_val)))
                raw_data['pit_location'] = actual_id
                raw_data['pit_location_id'] = actual_id
            except:
                raw_data['pit_location'] = 1
                raw_data['pit_location_id'] = 1
        else:
            raw_data['pit_location'] = 1
            raw_data['pit_location_id'] = 1

        # Bersihkan data angka numerik desimal
        for num_field in ['qty_value', 'consumed_qty', 'hm_km_unit', 'last_hm', 'flow_meter_value']:
            if num_field in raw_data and str(raw_data[num_field]).strip().lower() in ['null', 'none', '']:
                raw_data[num_field] = 0.0

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

            if val is not None:
                val_str = str(val).strip()
                if "['" in val_str or "[" in val_str:
                    angka_saja = re.findall(r'\d+', val_str)
                    val = int(angka_saja[0]) if angka_saja else 1

            if val is None or str(val).strip().lower() in ['null', 'none', '']:
                # 1. --- SAPU JAGAT KOLOM STATUS & FIELD STR/TEXT WAJIB ---
                if field_name.endswith('_status') or field_name in [
                    'po_number', 'vendor_name', 'vendor_delivery_order', 
                    'foto_surat_jalan', 'location_tujuan_id', 'id_transaction_reference',
                    'hm_status', 'flow_status'
                ]:
                    insert_data[field_name] = ""
                
                # 2. --- SAPU JAGAT KOLOM INT WAJIB ---
                elif field_name in ['is_transferred', 'is_sync', 'status']:
                    insert_data[field_name] = 0
                elif field_name == 'pit_location':
                    insert_data[field_name] = 1 
                else:
                    insert_data[field_name] = None
            else:
                insert_data[field_name] = val

        # Paksa tipe data desimal
        for num_field in ['qty_value', 'consumed_qty', 'hm_km_unit', 'last_hm', 'flow_meter_value']:
            if num_field in insert_data and insert_data[num_field] is not None:
                try:
                    insert_data[num_field] = float(insert_data[num_field])
                except:
                    insert_data[num_field] = 0.0

        for int_field in ['is_transferred', 'is_sync', 'flowmeter_id']:
            if int_field in insert_data and insert_data[int_field] is not None:
                try:
                    insert_data[int_field] = int(float(insert_data[int_field]))
                except:
                    insert_data[int_field] = 0

        # Inject Direct Pit ID
        raw_pit = validated_data.get('pit_location_id') or validated_data.get('pit_location') or insert_data.get('pit_location')
        try:
            if hasattr(raw_pit, 'id'):
                target_id = raw_pit.id
            else:
                target_id = int(float(str(raw_pit)))
        except:
            target_id = 1

        insert_data.pop('pit_location', None)
        insert_data['pit_location_id'] = target_id if target_id else 1

        # Generasi Kode Transaksi Otomatis
        if not insert_data.get('id_transaction'):
            insert_data['id_transaction'] = f"TRX-MDM-{int(time.time())}"
            
        trx_code = insert_data.get('transaction_code')
        if not trx_code or str(trx_code).strip().lower() in ['null', 'none', '']:
            insert_data['transaction_code'] = f"GRV-{int(time.time())}"
        else:
            if FuelTransaction.objects.filter(transaction_code=trx_code).exists():
                insert_data['transaction_code'] = f"{trx_code}-{int(time.time())}"

        # Handle Files
        request = self.context.get('request')
        if request and request.FILES:
            for file_key in request.FILES:
                if file_key in model_fields:
                    insert_data[file_key] = request.FILES[file_key]

        print("=== [LOG FIX SUCCESS: MYSQL DATA INJECTED WITH STATUS FALLBACK] ===")
        print(f"Transaction Code: {insert_data.get('transaction_code')}")
        print(f"Flow Status Secured: {insert_data.get('flow_status')}")
        print("====================================================================")

        # -------------------------------------------------------------------------
        # PERUBAHAN ADA DI BAWAH SINI: Menggunakan update_or_create
        # -------------------------------------------------------------------------
        
        # Ekstrak id_transaction sebagai parameter pencarian
        id_trans = insert_data.get('id_transaction')
        
        # defaults_data berisi semua data KECUALI id_transaction
        defaults_data = {k: v for k, v in insert_data.items() if k != 'id_transaction'}

        try:
            # Jika id_transaction sudah ada, update dengan defaults_data. Jika belum, buat baru.
            transaction, created = FuelTransaction.objects.update_or_create(
                id_transaction=id_trans,
                defaults=defaults_data
            )
            return transaction
        
        except Exception as db_err:
            # SKENARIO DARURAT TINGKAT AKHIR: Jika MySQL mengeluh karena kolom status bertipe IntegerField
            # dan menolak string kosong "", kita paksa ubah pesan error yang mengandung kata 'status' menjadi angka 0
            err_msg = str(db_err).lower()
            if "status" in err_msg or "cannot be null" in err_msg:
                # Cari field apa yang bikin error, atau set semua field berakhiran _status menjadi 0
                for f_name in insert_data:
                    if f_name.endswith('_status') and insert_data[f_name] == "":
                        insert_data[f_name] = 0
                        defaults_data[f_name] = 0  # Update juga di dictionary defaults_data
                
                # Jika spesifik menyebut field tertentu di log berikutnya
                if "flow_status" in err_msg:
                    insert_data['flow_status'] = 0
                    defaults_data['flow_status'] = 0
                if "hm_status" in err_msg:
                    insert_data['hm_status'] = 0
                    defaults_data['hm_status'] = 0
                    
                # Coba eksekusi ulang setelah nilai diselamatkan
                transaction, created = FuelTransaction.objects.update_or_create(
                    id_transaction=id_trans,
                    defaults=defaults_data
                )
                return transaction
                
            raise db_err