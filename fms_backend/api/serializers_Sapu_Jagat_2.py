import time
import re
from rest_framework import serializers
from .models import FuelTransaction

class FuelTransactionSerializer(serializers.ModelSerializer):
    # WAJIB: Override validator bawaan DRF agar tidak menolak request (400 Bad Request) 
    # jika mendeteksi ID duplikat. Biarkan update_or_create di bawah yang menanganinya.
    id_transaction = serializers.CharField(validators=[], required=False, allow_null=True, allow_blank=True)
    transaction_code = serializers.CharField(validators=[], required=False, allow_null=True, allow_blank=True)

    class Meta:
        model = FuelTransaction
        fields = '__all__'

    def to_internal_value(self, data):
        # MultipartRequest data dikirim dalam bentuk QueryDict yang immutable, kita copy dulu
        if hasattr(data, 'copy'):
            raw_data = data.copy()
        else:
            raw_data = dict(data)

        # 1. AMANKAN STRATEGIC DOUBLE-ROUTE (Flowmeter vs Pit Location)
        flutter_flowmeter = raw_data.get('flowmeter_id') or raw_data.get('flowmeterId')
        flutter_pit = raw_data.get('pit_location_id') or raw_data.get('pitLocationId') or raw_data.get('pit_location')

        if flutter_pit and not flutter_flowmeter:
            raw_data['pit_location_id'] = flutter_pit
            raw_data['flowmeter_id'] = flutter_pit
        elif flutter_flowmeter and not flutter_pit:
            raw_data['flowmeter_id'] = flutter_flowmeter
            raw_data['pit_location_id'] = flutter_flowmeter
        elif flutter_flowmeter and flutter_pit:
            raw_data['flowmeter_id'] = flutter_flowmeter
            raw_data['pit_location_id'] = flutter_pit

        # 2. EXPLICIT MAPPING (CamelCase ke snake_case)
        mapping_keys = [
            ('idTransaction', 'id_transaction'),
            ('transactionCode', 'transaction_code'),
            ('qtyValue', 'qty_value'),
            ('fillingActivity', 'filling_activity'),
            ('poNumber', 'po_number'),
            ('vendorName', 'vendor_name'),
            ('vendorDeliveryOrder', 'vendor_delivery_order'),
            ('locationTujuanId', 'location_tujuan_id'),
            ('idTransactionReference', 'id_transaction_reference'),
            ('createdBy', 'created_by'),
            ('remarkFlow', 'remark_flow'),
        ]

        for camel_key, snake_key in mapping_keys:
            if camel_key in raw_data and raw_data[camel_key] is not None:
                val_str = str(raw_data[camel_key]).strip()
                if val_str != '' and val_str.lower() != 'null':
                    raw_data[snake_key] = raw_data[camel_key]

        # 3. ANTI 400 BAD REQUEST: Inject nilai default untuk field yang tidak dikirim oleh Flutter
        
        # Field Angka/Desimal
        num_fields = ['qty_value', 'consumed_qty', 'hm_km_unit', 'last_hm', 'flow_meter_value']
        for f in num_fields:
            val = str(raw_data.get(f, '')).strip().lower()
            if val in ['null', '', 'none']:
                raw_data[f] = 0.0

        # Biarkan field AI tetap null jika kosong agar status 'Pending' relevan
        ai_fields = ['final_hm_value', 'final_flow_value', 'ai_hm_read', 'ai_flow_read']
        for f in ai_fields:
            val = str(raw_data.get(f, '')).strip().lower()
            if val in ['null', '', 'none']:
                raw_data[f] = None
                
        # Field Status / Integer
        int_fields = ['is_transferred', 'is_sync', 'status']
        for f in int_fields:
            val = str(raw_data.get(f, '')).strip().lower()
            if val in ['null', '', 'none']:
                raw_data[f] = 0

        # Field String Status
        str_fields = ['hm_status', 'flow_status']
        for f in str_fields:
            val = str(raw_data.get(f, '')).strip().lower()
            if val in ['null', '', 'none']:
                raw_data[f] = ""

        try:
            return super().to_internal_value(raw_data)
        except serializers.ValidationError as e:
            # Jika masih tembus 400, log error detailnya agar kita tahu field mana biang keroknya!
            print("\n=== [DRF VALIDATION ERROR 400 DETECTED] ===")
            print(e.detail)
            print("============================================\n")
            raise e

    def create(self, validated_data):
        insert_data = {}
        model_fields = [f.name for f in FuelTransaction._meta.fields]

        for field_name in model_fields:
            if field_name == 'id':
                continue
                
            val = validated_data.get(field_name)

            # Ekstrak angka saja jika format Flutter terdeteksi sebagai array string "['1']"
            if val is not None:
                val_str = str(val).strip()
                if "['" in val_str or "[" in val_str:
                    angka_saja = re.findall(r'\d+', val_str)
                    val = int(angka_saja[0]) if angka_saja else 1

            if val is None or str(val).strip().lower() in ['null', 'none', '']:
                # Sapu Jagat Data Kosong
                if field_name.endswith('_status') or field_name in [
                    'po_number', 'vendor_name', 'vendor_delivery_order', 
                    'foto_surat_jalan', 'location_tujuan_id', 'id_transaction_reference',
                ]:
                    insert_data[field_name] = ""
                elif field_name in ['is_transferred', 'is_sync', 'status']:
                    insert_data[field_name] = 0
                elif field_name in ['pit_location', 'pit_location_id', 'flowmeter_id']:
                    insert_data[field_name] = "UNKNOWN"
                else:
                    insert_data[field_name] = None
            else:
                insert_data[field_name] = val

        # Handle Files
        request = self.context.get('request')
        if request and request.FILES:
            for file_key in request.FILES:
                if file_key in model_fields:
                    insert_data[file_key] = request.FILES[file_key]

        # Logika Inti: Hindari Duplicate Entry dengan update_or_create
        id_trans = insert_data.get('id_transaction')
        
        # Default kode jika ternyata benar-benar tidak terkirim
        if not id_trans:
            id_trans = f"TRX-MDM-{int(time.time())}"
            insert_data['id_transaction'] = id_trans
            
        trx_code = insert_data.get('transaction_code', '')
        
        # Cek jika string kosong ATAU panjangnya 4 huruf ke bawah (misal hanya berisi 'OFI' atau 'INI')
        if not trx_code or len(str(trx_code).strip()) <= 4:
            # Ambil string pendek tersebut sebagai awalan (prefix). Jika benar-benar kosong, pakai 'TRX'
            prefix = str(trx_code).strip().upper() if trx_code else 'TRX'
            
            # Buat kode unik sesuai jenis transaksinya
            insert_data['transaction_code'] = f"{prefix}-{int(time.time())}"

        defaults_data = {k: v for k, v in insert_data.items() if k != 'id_transaction'}

        try:
            transaction, created = FuelTransaction.objects.update_or_create(
                id_transaction=id_trans,
                defaults=defaults_data
            )
            return transaction
        except Exception as db_err:
            print(f"=== [DB FATAL ERROR SAVING DATA]: {db_err} ===")
            raise db_err