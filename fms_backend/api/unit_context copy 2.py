import re

def get_unit_prompt_hint(target_type, unit_model=None, last_val=0):
    """
    Mengadopsi logika GeminiService.dart ke dalam Python Backend.
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
        TUGAS: Ekstrak angka Hour Meter (HM) dari foto instrumen alat berat ini.
        {hint}
        
        INSTRUKSI KHUSUS:
        1. Ambil seluruh digit termasuk angka desimal (angka terakhir biasanya warna latar berbeda atau dipisahkan titik).
        2. CONTEXTUAL DATA: Nilai terakhir adalah {last_val}. Hasil harus >= {last_val}.
        3. Jika angka blur, gunakan logika bahwa angka harus lebih tinggi dari {last_val}.
        4. OUTPUT: Hanya angka saja (contoh: 2388.6).
        """
        
    else:
        # Logika khusus Flow Meter (Totalizer)
        prompt = f"""
        TUGAS: Ekstrak angka TOTALIZER dari flow meter bahan bakar.
        INSTRUKSI KHUSUS:
        1. Fokus pada kotak kaca counter mekanik/digital di atas label 'LITRES' atau 'TOTAL'.
        2. Abaikan nol di depan (Leading Zeros). Misal '0001234' menjadi '1234'.
        3. JANGAN ambil angka dari stiker spesifikasi (Max Press, Model, Year).
        4. CONTEXTUAL DATA: Nilai terakhir adalah {last_val}.
        5. OUTPUT: Hanya angka saja tanpa satuan.
        """

    return prompt

def clean_ai_response(raw_response):
    """
    Mengadopsi Regex Cleaning dari gemini_service.dart
    """
    if not raw_response:
        return "TIDAK_TERBACA"
        
    # 1. Hapus semua karakter kecuali angka dan titik
    digits = re.sub(r'[^0-9.]', '', raw_response)
    
    # 2. Hilangkan titik di akhir jika ada
    digits = digits.strip('.')
    
    # 3. Penanganan titik ganda (Double dot prevention)
    if '.' in digits:
        parts = digits.split('.')
        if len(parts) > 2:
            digits = f"{parts[0]}.{parts[1]}"
            
    return digits if digits else "TIDAK_TERBACA"