export const EXPORT_LAYOUT_KEY = 'fms_export_layout_pref';

// Pasangan field database dengan Header SAP
export const FIELD_LABELS = {
  no_unit_sap: 'No Unit SAP',
  gas_station: 'Gas Station',
  date: 'Date',
  jam_isi: 'Jam Isi',
  hm_km: 'HM/KM UNIT',
  consumed_qty: 'Consumed Qty',
  fluid_type: 'Fluid Type',
  measuring_position: 'Measuring Position',
  location_id: 'Location ID', // Menggantikan pit_location_id secara bersih
  header_text: 'Header Text',
  flow_meter_value: 'Flow Meter_Value',
  // Sisa field bawaan yang dipindah ke hidden fields secara otomatis
  id: 'ID',
  tech_id: 'Tech ID',
  entry_type: 'Entry Type',
  ai_hm_read: 'AI HM Read',
  ai_flow_read: 'AI Flow Read',
  screening_status: 'Screening Status',
  date_screening: 'Date Screening',
  created_at: 'Created At'
};

// Kolom default awal yang langsung aktif
export const DEFAULT_ACTIVE_FIELDS = [
  'no_unit_sap',
  'gas_station',
  'date',
  'jam_isi',
  'hm_km',
  'consumed_qty',
  'fluid_type',
  'measuring_position',
  'location_id', // Diganti menjadi location_id
  'header_text',
  'flow_meter_value'
];

export const AVAILABLE_FIELDS = Object.keys(FIELD_LABELS);