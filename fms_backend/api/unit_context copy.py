def get_unit_prompt_hint(unit_model, last_hm=0):
    """
    Menyediakan instruksi spesifik berdasarkan model unit untuk membantu Gemini.
    """
    
    # Dictionary yang menyimpan karakteristik visual tiap model
    mappings = {
        'HD785': (
            "UNIT IDENTIFICATION: Komatsu HD785 and HD465 Digital Dashboard. "
            "VISUAL CUES: Look for a yellow-green backlit LCD screen. It typically shows two rows of numbers. "
            "TARGET: The HOUR METER (HM) is the TOP row, followed by a small 'h'. "
            "RESTRICTION: Ignore the bottom row (ODOMETER/KM) which is followed by 'km'."
        ),
        'HONEYWELL': (
            "UNIT IDENTIFICATION: Standalone Honeywell Hour Meter. "
            "VISUAL CUES: A rectangular black frame with a clear digital display labeled 'HOURS'. "
            "TARGET: Extract all 5-6 digits before and after the decimal point."
        ),
        'GENERIC': (
            "UNIT IDENTIFICATION: Heavy Equipment Dashboard. "
            "TARGET: Look for a counter labeled with 'h', 'hours', or an hourglass icon. "
            "Extract the numerical value clearly."
        )
    }

    # Ambil mapping berdasarkan model, jika tidak ada pakai GENERIC
    hint = mappings.get(unit_model, mappings['GENERIC'])
    
    # Tambahkan context HM terakhir ke dalam prompt
    full_prompt = f"""
    {hint}
    
    CONTEXTUAL DATA:
    - The last recorded value for this unit was: {last_hm}.
    - The current value MUST be equal to or greater than {last_hm}.
    - If a digit is blurry, use the context that it must be logically higher than the previous record.
    """
    
    return full_prompt