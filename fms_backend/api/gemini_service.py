import google.generativeai as genai
from django.conf import settings
import PIL.Image
import logging
import json
from .unit_context import get_unit_prompt_hint

logger = logging.getLogger(__name__)

class GeminiScreeningService:
    def __init__(self):
        api_key = getattr(settings, "GEMINI_API_KEY", None)
        if not api_key:
            raise ValueError("GEMINI_API_KEY belum dikonfigurasi")
        
        genai.configure(api_key=api_key)
        
        # System Instruction yang Anda buat di AI Studio
        self.system_instruction = (
            "Anda adalah pakar OCR spesialis industri alat berat dan pertambangan. "
            "Tugas utama Anda adalah mengekstraksi angka numerik dari foto ke dalam format JSON yang kaku.\n\n"
            "ATURAN FLOWMETER:\n1. Ekstrak sebagai ANGKA BULAT (INTEGER). DILARANG desimal.\n"
            "2. Angka paling kanan adalah satuan penuh.\n3. Jika menggantung, ambil angka atas.\n"
            "4. Hilangkan nol di depan dan satuan (L/Litres).\n\n"
            "ATURAN HM:\n1. Fokus angka di atas label 'ODO'.\n2. Hilangkan huruf 'h'.\n\n"
            "OUTPUT WAJIB JSON: {\"value\": integer, \"unit_type\": \"Flowmeter/HM\", \"is_readable\": boolean, \"confidence_score\": float}"
        )

        # Inisialisasi Model dengan konfigurasi AI Studio
        self.model = genai.GenerativeModel(
            model_name='gemini-3-flash-preview', # atau 'gemini-3-flash-preview'
            system_instruction=self.system_instruction,
            generation_config={
                "temperature": 0,
                "top_p": 0.8,
                "max_output_tokens": 2000,
                "response_mime_type": "application/json",
            }
        )

    def screen_fuel_images(self, hm_image_path, flow_image_path, unit_model="GENERIC", last_hm=0, last_flow=0):
        results = {"hm": None, "flow": 0, "status": "success"}

        try:
            # 1. Proses HM
            if hm_image_path:
                hm_hint = get_unit_prompt_hint("HM", unit_model, last_hm)
                results["hm"] = self._process_ocr(hm_image_path, hm_hint)

            # 2. Proses Flow
            if flow_image_path:
                flow_hint = get_unit_prompt_hint("FLOW", unit_model, last_flow)
                results["flow"] = self._process_ocr(flow_image_path, flow_hint)

        except Exception as e:
            logger.error(f"Gemini Service Error: {str(e)}")
            results["status"] = "error"
            results["message"] = str(e)

        return results

    def _process_ocr(self, image_path, hint):
        """Helper untuk mengirim gambar dan hint ke Gemini"""
        img = PIL.Image.open(image_path)
        
        # Memulai chat untuk simulasi 'Few-Shot' jika diperlukan, 
        # atau langsung generate_content untuk efisiensi
        response = self.model.generate_content([hint, img])
        
        try:
            data = json.loads(response.text)
            # Pastikan output adalah angka bulat untuk flow, atau float untuk HM (sesuai hint)
            return data.get("value", 0)
        except:
            logger.error(f"Failed to parse JSON from AI: {response.text}")
            return 0