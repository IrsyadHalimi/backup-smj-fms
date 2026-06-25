export const EXPORT_LAYOUT_KEY = 'fms_export_layout_pref';

export const AVAILABLE_FIELDS = [
  'id',
  'no_unit_sap',
  'tech_id',
  'entry_type',
  'hm_km_unit',
  'ai_hm_read',
  'flow_meter_value',
  'ai_flow_read',
  'screening_status',
  'date_screening',
  'created_at'
];

export const menuButtonStyle = {
  background: 'none',
  border: 'none',
  color: 'white',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  fontSize: '12px',
  cursor: 'pointer',
  fontWeight: '600',
  padding: '4px 8px'
};

export const activeModeStyle = {
  backgroundColor: '#22d3ee',
  color: '#0f172a',
  border: 'none',
  padding: '6px 15px',
  borderRadius: '4px',
  fontSize: '10px',
  fontWeight: 'bold',
  cursor: 'pointer'
};

export const inactiveModeStyle = {
  backgroundColor: 'rgba(255,255,255,0.05)',
  color: '#64748b',
  border: 'none',
  padding: '6px 15px',
  borderRadius: '4px',
  fontSize: '10px',
  fontWeight: 'bold',
  cursor: 'pointer'
};