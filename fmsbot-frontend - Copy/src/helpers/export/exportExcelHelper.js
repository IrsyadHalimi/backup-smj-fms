import * as XLSX from 'xlsx';
import { FIELD_LABELS } from './exportConstants';

// Helper format tanggal (DD.MM.YYYY)
const formatMiningDate = (dateSource) => {
  if (!dateSource) return new Date().toLocaleDateString('id-ID').replace(/\//g, '.');
  
  // Jika dari backend sudah berupa format DD.MM.YYYY, langsung pakai
  if (typeof dateSource === 'string' && dateSource.includes('.')) {
    return dateSource;
  }
  
  const d = new Date(dateSource);
  if (isNaN(d.getTime())) return dateSource;
  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();
  return `${day}.${month}.${year}`;
};

// Helper format waktu pintar (Anti-banting nilai ke default)
const formatMiningTime = (record) => {
  // 1. Ambil properti waktu mentah dari object record
  const rawTime = record.jam_isi || record.time;
  
  // 2. Jika formatnya sudah string jam bersih (ada tanda titik dua ':'), langsung kembalikan aslinya!
  if (rawTime && typeof rawTime === 'string' && rawTime.includes(':')) {
    return rawTime.trim();
  }

  // 3. Fallback jika bersumber dari full ISO timestamp (created_at)
  const dateSource = record.created_at || record.date;
  if (!dateSource) return "00:00:00";
  
  const d = new Date(dateSource);
  if (isNaN(d.getTime())) return "00:00:00"; 
  
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${hours}:${minutes}:${seconds}`;
};

export const exportToExcel = async ({
  records,
  selectedFields,
  fileName, // Ambil parameter nama file dinamis dari App.jsx
  showToast,
  setDbHistory,
  setIsExportModalOpen
}) => {
  
  const dataToProcess = Array.isArray(records) ? records : [];

  if (dataToProcess.length === 0) {
    if (typeof showToast === 'function') {
      showToast("Tidak ada data terfilter yang tersedia untuk di-export!", "exclamation-triangle");
    }
    return;
  }

  try {
    const dataToExport = dataToProcess.map(record => {
      let row = {};
      selectedFields.forEach(field => {
        const headerLabel = FIELD_LABELS[field] || field.toUpperCase();
        let value = record[field];

        if (field === 'gas_station') value = record.gas_station || 'GW01';
        else if (field === 'fluid_type') value = record.fluid_type || 'FUEL-DIESEL';
        else if (field === 'measuring_position') value = record.measuring_position || 'FUEL';
        else if (field === 'header_text') value = record.header_text || 'FUEL_TRX';
        else if (field === 'location_id') value = record.location_id || record.pit_location_id || record.gas_station || 'PIT-55'; // Mengamankan fallback map jika field lama terpanggil
        else if (field === 'date') value = formatMiningDate(record.date || record.created_at);
        
        // Perbaikan vital: Kirim full object record agar fungsi formatMiningTime bisa memeriksa field jam_isi & time
        else if (field === 'jam_isi') value = formatMiningTime(record);
        
        else if (field === 'hm_km') value = record.hm_km_unit || record.hm_km || 0;
        else if (field === 'consumed_qty') {
          const rawQty = Number(record.consumed_qty || record.qty_value || 0);
          value = Math.abs(rawQty);
        }

        row[headerLabel] = value !== null && value !== undefined ? value : "-";
      });
      return row;
    });

    const worksheet = XLSX.utils.json_to_sheet(dataToExport);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "SAP_Ready_Upload");
    
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    
    // Gunakan nama file yang dikirim dari App.jsx, jika kosong gunakan fallback
    const finalFileName = fileName ? `${fileName}.xlsx` : `FMS_SAP_Export_${new Date().toISOString().split('T')[0]}.xlsx`;

    if ('showSaveFilePicker' in window) {
      const handle = await window.showSaveFilePicker({
        suggestName: finalFileName, // Menggunakan nama file dinamis terintegrasi
        types: [{
          description: 'Excel Worksheets',
          accept: { 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'] },
        }],
      });
      const writable = await handle.createWritable();
      await writable.write(blob);
      await writable.close();
    } else {
      XLSX.writeFile(workbook, finalFileName);
    }

    // Set status records menjadi Downloaded pasca unduh berhasil
    if (typeof setDbHistory === 'function') {
      setDbHistory(prev => prev.map(r => {
        const isMatched = dataToProcess.some(p => String(p.id) === String(r.id));
        return isMatched ? { ...r, screening_status: 'Downloaded', validation_status: 'Downloaded' } : r;
      }));
    }

    if (typeof showToast === 'function') showToast("Berhasil generate berkas SAP Excel!", "check-circle");
    if (typeof setIsExportModalOpen === 'function') setIsExportModalOpen(false);

  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error(error);
      if (typeof showToast === 'function') showToast("Gagal memproses export berkas", "times-circle");
    }
  }
};