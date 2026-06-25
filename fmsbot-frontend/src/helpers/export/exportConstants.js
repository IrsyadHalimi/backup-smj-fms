export const EXPORT_LAYOUT_KEY = 'fms_export_layout_pref';

// Pasangan field database dengan Header SAP
export const FIELD_LABELS = {
  location_id: 'Lokasi',
  date: 'Date',
  unit: 'Jenis Unit',
  no_unit_sap: 'Unit Group',
  tech_id: 'Unit',
  kode_unit: 'Kode Unit',
  jam_isi: 'Jam Pengisian',
  last_hm: 'Last HM',
  hm_km_unit: 'HM Unit', // Menggantikan pit_location_id secara bersih
  hm_selisih: 'HM Selisih',
  flow_meter_value: 'Flowmeter Reading',
  // Sisa field bawaan yang dipindah ke hidden fields secara otomatis
  consumed_qty: 'Jumlah Pengisian',
  ltr_jam: 'Ltr/Jam',
  screening_status: 'Screening Status',
  date_screening: 'Date Screening',
  created_at: 'Created At'
};

// Kolom default awal yang langsung aktif
export const DEFAULT_ACTIVE_FIELDS = [
  'location_id',
  'date',
  'unit',
  'no_unit_sap',
  'tech_id',
  'kode_unit',
  'jam_isi',
  'last_hm',
  'hm_km_unit', // Diganti menjadi location_id
  'hm_selisih',
  'flow_meter_value',
  'consumed_qty',
  'screening_status',
  'date_screening',
  'created_at'
];

export const AVAILABLE_FIELDS = Object.keys(FIELD_LABELS);