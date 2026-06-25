from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver
from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from django.db.models.signals import pre_save
from django.utils import timezone
from django.core.exceptions import ValidationError

class FuelTransaction(models.Model):
    # Identitas Unit & Lokasi
    no_unit_sap = models.CharField(max_length=50)
    tech_id = models.CharField(max_length=50)
    gas_station = models.CharField(max_length=50, default='GW01')
    pit_location_id = models.CharField(max_length=50)
    flowmeter_id = models.CharField(max_length=50, blank=True, null=True)
    location_id = models.CharField(max_length=50, blank=True) # ID Fuel Truck/Storage
    filling_activity = models.CharField(max_length=50, blank=True, null=True, default='OFI')

    id_transaction = models.CharField(max_length=100, unique=True, blank=True, null=True)
    transaction_code = models.CharField(max_length=100, unique=True, blank=True, null=True)
    qty_value = models.FloatField(default=0.0)

    # Waktu Operasional
    date = models.CharField(max_length=20) # Format string dari Flutter
    jam_isi = models.CharField(max_length=20)
    
    # Data Metering (Angka)
    hm_km_unit = models.FloatField()
    last_hm = models.FloatField()
    consumed_qty = models.FloatField()
    flow_meter_value = models.FloatField()
    
    # Jenis Cairan & Posisi
    fluid_type = models.CharField(max_length=50, default='FUEL-DIESEL')
    measuring_position = models.CharField(max_length=50, default='FUEL')
    header_text = models.CharField(max_length=100, default='FUEL_TRX')
    entry_type = models.CharField(max_length=20, default='NORMAL') # INITIAL atau NORMAL
    
    # Data Hasil OCR AI
    ai_hm_read = models.FloatField(null=True, blank=True)
    ai_flow_read = models.FloatField(null=True, blank=True)
    final_hm_value = models.FloatField(null=True, blank=True)
    final_flow_value = models.FloatField(null=True, blank=True)
    
    # Field Foto (Menampung Base64 atau Path)
    # Gunakan ImageField karena Flutter mengirim http.MultipartFile (Binary/File)
    hm_foto = models.ImageField(upload_to='fuel/hm/', null=True, blank=True)
    flow_meter_foto = models.ImageField(upload_to='fuel/flow/', null=True, blank=True)
    hm_foto_full = models.ImageField(upload_to='fuel/full/', null=True, blank=True)
    flow_meter_foto_full = models.ImageField(upload_to='fuel/full/', null=True, blank=True)
    foto_unit = models.ImageField(upload_to='fuel/unit/', null=True, blank=True)
    foto_surat_jalan = models.ImageField(upload_to='fuel/full/', null=True, blank=True)  

    # Status Spesifik
    hm_status = models.CharField(max_length=20, default='N/A')         # Status HM saja
    flow_status = models.CharField(max_length=20, default='N/A')
    screening_status = models.CharField(max_length=20, default='Pending')
    is_transferred = models.IntegerField(default=1) # Di server otomatis 1
    date_transferred = models.DateField(null=True, blank=True)
    date_screening = models.DateField(null=True, blank=True)
    sync_status = models.CharField(max_length=20, default='PENDING')

    # GR
    po_number = models.CharField(max_length=50, blank=True, null=True)
    vendor_name = models.CharField(max_length=100, blank=True, null=True)
    vendor_delivery_order = models.CharField(max_length=100, blank=True, null=True)
    location_tujuan_id = models.CharField(max_length=50, blank=True, null=True)
    id_transaction_reference = models.CharField(max_length=100, blank=True, null=True)

    created_at = models.DateTimeField(auto_now_add=True, null=True, blank=True)

    def __str__(self):
        return f"{self.no_unit_sap} - {self.date} {self.jam_isi}"

class ModelUnit(models.Model):
    unit_model = models.CharField(max_length=100, unique=True)
    desc = models.TextField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True, null=True, blank=True)

    def __str__(self):
        return self.unit_model

class MasterBudgetUnit(models.Model):
    unit_model = models.ForeignKey(ModelUnit, on_delete=models.CASCADE, related_name='budgets')
    
    start_date = models.DateField(default=timezone.now, help_text="Start of the budget period")
    end_date = models.DateField(default=timezone.now, help_text="End of the budget period")
    
    target_unit_qty = models.IntegerField(default=0, help_text="Target number of units")
    total_budget = models.DecimalField(max_digits=15, decimal_places=2, help_text="Total budget amount")
    
    desc = models.TextField(blank=True, null=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ('unit_model', 'start_date', 'end_date')
        verbose_name_plural = "Master Budget Units"

    def clean(self):
        if self.start_date and self.end_date:
            if self.end_date < self.start_date:
                raise ValidationError("The end date cannot be earlier than the start date.")

    def __str__(self):
        return f"Budget {self.unit_model.unit_model} ({self.start_date} to {self.end_date})"


@receiver(post_save, sender=FuelTransaction)
def notify_sync(sender, instance, created, **kwargs):
    from api.tasks import task_process_ai_screening
    from channels.layers import get_channel_layer
    from asgiref.sync import async_to_sync

    if created:
        # 1. Kirim notifikasi ke React via WebSocket
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            "fuel_sync_group",  
            {
                "type": "send_new_record",
                "data": {
                    "id": instance.id,
                    "no_unit_sap": instance.no_unit_sap,
                    "tech_id": instance.tech_id,
                    "date": str(instance.date),
                    # Tambahkan flag ini agar React bisa memfilter
                    "is_transferred": instance.is_transferred,
                    "date_transferred": str(instance.date_transferred) if instance.date_transferred else str(timezone.now().date()),
                    "screening_status": instance.screening_status,
                }
            }
        )

        # 2. Trigger AI Screening (Celery)
        from .tasks import task_process_ai_screening
        task_process_ai_screening.delay(instance.id)