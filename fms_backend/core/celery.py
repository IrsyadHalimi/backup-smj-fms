import os
from celery import Celery

# Set default Django settings module
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'core.settings')

app = Celery('fms_project')

# Paksa broker URL di sini jika settings.py tidak terbaca
app.conf.update(
    broker_url='redis://127.0.0.1:6379/0',
    result_backend='redis://127.0.0.1:6379/0',
    broker_connection_retry_on_startup=True,
)

# Mengambil konfigurasi dari settings.py dengan prefix CELERY_
app.config_from_object('django.conf:settings', namespace='CELERY')

# Auto-discover tasks di semua app (api/tasks.py)
app.autodiscover_tasks()