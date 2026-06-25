from django.urls import re_path
from . import consumers

websocket_urlpatterns = [
    # Jalur ini akan diakses React: ws://ip-vps:8000/ws/fuel-sync/
    re_path(r'ws/fuel-sync/$', consumers.FuelSyncConsumer.as_asgi()),
]