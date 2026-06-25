import google.generativeai as genai
from django.conf import settings
import PIL.Image
import logging
from .unit_context import get_unit_prompt_hint

logger = logging.getLogger(__name__)

class GeminiScreeningService:
    def __init__(self):
        # Mengambil API Key dari settings.py
        api_key = getattr(settings, "GEMINI_API_KEY", None)
        if not api_key:
            raise ValueError("GEMINI_API_KEY belum dikonfigurasi di settings.py")
        genai.configure(api_key=api_key)
        self.model = genai.GenerativeModel('gemini-3.1-pro-preview')

    def screen_fuel_images(self, hm_image_path, flow_image_path, unit_model="GENERIC", last_hm=0, last_flow=0):
        """
        Melakukan screening foto HM dan Flow Meter dengan bantuan data historis.
        """
        # Dapatkan instruksi khusus berdasarkan model unit
        hm_hint = get_unit_prompt_hint(unit_model, last_hm)

        # 1. Prompt untuk Hour Meter (HM)
        hm_instruction = f"""
        {hm_hint}
        TASK: Extract the current HM value. 
        Return ONLY the number. No letters, no explanation.
        """

        # 2. Prompt untuk Flow Meter (Litres)
        flow_instruction = f"""
        UNIT: Fuel Dispenser Flow Meter.
        CONTEXT: Last recorded totalizer was {last_flow}.
        INSTRUCTIONS: Extract the current totalizer digits (usually white digits on black background).
        Return ONLY the number. No commas, no text.
        """

        results = {"hm": None, "flow": 0.0, "status": "success"}

        try:
            # Proses Pembacaan HM
            if hm_image_path:
               img_hm = PIL.Image.open(hm_image_path)
               res_hm = self.model.generate_content(
                   [hm_instruction, img_hm]
               )
               results["hm"] = self._parse_to_float(res_hm.text)

            # Proses Pembacaan Flow
            img_flow = PIL.Image.open(flow_image_path)
            res_flow = self.model.generate_content([flow_instruction, img_flow])
            results["flow"] = self._parse_to_float(res_flow.text)

        except Exception as e:
            logger.error(f"Gemini Service Error: {str(e)}")
            results["status"] = "error"
            results["message"] = str(e)

        return results

    def _parse_to_float(self, text):
        """Helper untuk membersihkan teks menjadi angka float"""
        try:
            # Hapus semua karakter kecuali angka dan titik
            clean_val = "".join(c for c in text.strip() if c.isdigit() or c == '.')
            if clean_val.count('.') > 1:
                parts = clean_val.split('.')
                clean_val = f"{parts[0]}.{parts[1]}"
            return float(clean_val) if clean_val else 0.0
        except:
            return 0.0