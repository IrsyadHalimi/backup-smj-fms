import json
from channels.generic.websocket import AsyncWebsocketConsumer

class FuelSyncConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        self.group_name = "fuel_sync_group"
        await self.channel_layer.group_add(self.group_name, self.channel_name)
        await self.accept()

    async def disconnect(self, close_code):
        await self.channel_layer.group_discard(self.group_name, self.channel_name)

    # 1. Handler Eksisting (Untuk pesan yang dikirim dari Signal)
    async def fuel_message(self, event):
        await self.send(text_data=json.dumps({
            "type": "fuel.message",
            "id": event.get("id"),
            "status": event.get("status")
        }))

    # 2. Handler Baru (SOLUSI: Untuk menangkap pesan dari Celery Worker Anda)
    async def send_new_record(self, event):
        """
        Menangkap tipe 'send_new_record' dari Celery, mengambil payload ID-nya,
        lalu meneruskannya ke useRealtimeFuelSync.js di React.
        """
        await self.send(text_data=json.dumps({
            "type": "send_new_record",
            "id": event.get("id"), # Mengambil ID transaksi baru untuk di-fetch ulang oleh React
            "status": event.get("status")
        }))