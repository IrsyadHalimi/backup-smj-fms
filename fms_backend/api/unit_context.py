import re

def get_unit_prompt_hint(target_type, unit_model=None, last_val=0):
    """
    Mengadopsi logika GeminiService ke dalam Python Backend.
    Disesuaikan untuk pembacaan HM Alat Berat dan Flow Meter Digital/Mekanik.
    """
    
    if target_type == "HM":
        # Logika khusus Hour Meter (HM)
        mappings = {
            'HD785': (
                "UNIT: Komatsu HD785/HD465 Digital Dashboard. "
                "CUES: Yellow-green backlit LCD. "
                "TARGET: TOP row followed by 'h'. IGNORE bottom row with 'km'."
            ),
            'HONEYWELL': (
                "UNIT: Standalone Honeywell Hour Meter. "
                "CUES: Rectangular black frame labeled 'HOURS'. "
                "TARGET: All digits before and after decimal point."
            ),
            'GENERIC': (
                "UNIT: Heavy Equipment Dashboard. "
                "TARGET: Search for 'h', 'hours', or hourglass icon."
            )
        }
        
        hint = mappings.get(unit_model, mappings['GENERIC'])
        
        prompt = f"""
        TUGAS: Ekstrak angka Hour Meter (HM).
        {hint}
        
        INSTRUKSI KHUSUS:
        1. Ambil seluruh digit termasuk desimal.
        2. CONTEXTUAL DATA: Nilai terakhir adalah {last_val}. Hasil harus >= {last_val}.
        3. OUTPUT WAJIB JSON: {{"value": number, "unit_type": "HM", "is_readable": boolean, "confidence_score": float}}
        """
        
    else:
        # Logika khusus Flow Meter (Totalizer) - SESUAI SYSTEM INSTRUCTION FEW-SHOT
        prompt = f"""
        TUGAS: Ekstrak angka TOTALIZER dari flow meter bahan bakar.
        INSTRUKSI KHUSUS:
        1. Ekstrak seluruh digit sebagai satu kesatuan ANGKA BULAT (INTEGER).
        2. DILARANG KERAS menggunakan titik (.), koma (,), atau desimal.
        3. Angka paling kanan (paling akhir) adalah SATUAN PENUH, BUKAN desimal.
        4. Abaikan nol di depan (Leading Zeros).
        5. CONTEXTUAL DATA: Nilai terakhir adalah {last_val}.
        6. OUTPUT WAJIB JSON: {{"value": integer, "unit_type": "Flowmeter", "is_readable": boolean, "confidence_score": float}}
        """

    return prompt

def clean_ai_response(raw_response):
    """
    Tetap dipertahankan sebagai fallback jika JSON parsing gagal di service.
    """
    if not raw_response:
        return "TIDAK_TERBACA"
        
    digits = re.sub(r'[^0-9.]', '', raw_response)
    digits = digits.strip('.')
    
    if '.' in digits:
        parts = digits.split('.')
        if len(parts) > 2:
            digits = f"{parts[0]}.{parts[1]}"
            
    return digits if digits else "TIDAK_TERBACA"