import { showToastHelper } from "../../utils/toastHelper";

import { submitValidationService }
from "./validationService";

import { reScreeningService }
from "./reScreeningService";



export const handleOpenValidationHandler = ({
  record,
  setSelectedRecord,
  setFinalHM,
  setFinalFlow,
  setIsModalOpen,
  setActiveMenuId,
  showToast
}) => {

  if (record.screening_status === 'Verified') {

    showToast(
      `Data unit ${record.no_unit_sap} sudah Verified by System.`,
      "shield-check"
    );

    return;
  }

  const isHMMatch =
    Number(record.hm_km_unit) ===
    Number(record.ai_hm_read);

  const isFlowMatch =
    Number(record.flow_meter_value) ===
    Number(record.ai_flow_read);

  setSelectedRecord(record);

  setFinalHM(
    isHMMatch
      ? record.ai_hm_read
      : ''
  );

  setFinalFlow(
    isFlowMatch
      ? record.ai_flow_read
      : ''
  );

  setIsModalOpen(true);

  setActiveMenuId(null);
};



export const handleSubmitValidationHandler = async ({
  BASE_URL,
  selectedRecord,
  finalHM,
  finalFlow,

  setDbHistory,
  setRecords,

  setIsModalOpen,
  setSelectedRecord,
  setFinalHM,
  setFinalFlow,

  fetchAllData,

  showToast
}) => {

  if (!selectedRecord) return;

  // 🚀 LANGKAH 1: LANGSUNG TUTUP MODAL DI UI (USER MERASA INSTAN & SELESAI)
  setIsModalOpen(false);
  
  // Tampilkan toast awal agar user tahu data sedang dikirim ke VPS
  showToast(
    `Menyimpan validasi unit ${selectedRecord.no_unit_sap}...`,
    "spinner"
  );

  try {
    // 🚀 LANGKAH 2: JALANKAN REQUEST KE BACKEND VPS DI LATAR BELAKANG
    const response = await submitValidationService({
      BASE_URL,
      selectedRecord,
      finalHM,
      finalFlow
    });

    if (response.ok) {
      const updatedData = await response.json();

      // Buat objek record baru yang statusnya sudah 'Verified' berdasarkan data ter-update dari Django
      const verifiedRecord = {
        ...selectedRecord,
        ...updatedData,
        screening_status: 'Verified'
      };

      // 🚀 LANGKAH 3: UPDATE DATA TABEL SECARA REAL-TIME (STRATEGI PINDAH KE ATAS)
      
      // Catatan Logika: 
      // 1. Ambil record yang sedang diproses keluar dari posisinya saat ini (.filter)
      // 2. Taruh record yang sudah diverifikasi tsb di index [0] atau paling atas (... array)
      const moveRecordToTop = (prev) => {
        const remainingRecords = prev.filter(item => item.id !== selectedRecord.id);
        return [verifiedRecord, ...remainingRecords];
      };

      if (typeof setRecords === 'function') setRecords(moveRecordToTop);
      if (typeof setDbHistory === 'function') setDbHistory(moveRecordToTop);

      setSelectedRecord(null);
      setFinalHM('');
      setFinalFlow('');

      showToast(
        `Unit ${selectedRecord.no_unit_sap} berhasil divalidasi & dipindahkan ke baris teratas`,
        "check-circle"
      );

      // SINKRONISASI AKHIR
      if (typeof fetchAllData === 'function') {
        setTimeout(() => {
          fetchAllData();
        }, 1500); // Setel ke 1.5 detik agar urutan database sinkron sempurna dengan VPS
      }

    } else {
      const errorData = await response.json();

      // JIKA GAGAL: Buka kembali modalnya agar data tidak hilang dan user bisa koreksi
      setIsModalOpen(true);

      showToast(
        `Gagal simpan: ${errorData.message || 'Cek input'}`,
        "exclamation-triangle"
      );
    }

  } catch (error) {
    console.error("Error Validation:", error);

    // JIKA KONEKSI DROP: Buka kembali modalnya
    setIsModalOpen(true);

    showToast(
      "Gagal menghubungi server, silakan coba lagi",
      "wifi"
    );
  }
};

export const handleReScreeningHandler = async ({
  BASE_URL,
  API_URL,
  record,

  setRecords,
  setDbHistory,
  setActiveMenuId,

  showToast
}) => {

  if (!record?.id) return;

  showToast(
    `Memulai AI Re-Screening: ${record.no_unit_sap}...`,
    "robot"
  );

  setActiveMenuId(null);

  try {

    const response =
      await reScreeningService({
        BASE_URL,
        API_URL,
        record
      });

    if (response.ok) {

      const result =
        await response.json();

      const sync = prev =>
        prev.map(item =>
          item.id === record.id
            ? {
                ...item,
                ...result
              }
            : item
        );

      setRecords(sync);

      setDbHistory(sync);

      showToast(
        "AI sedang bekerja",
        "cog"
      );

    } else {

      showToast(
        "Server menolak request",
        "exclamation-triangle"
      );
    }

  } catch (error) {

    console.error(
      "Network Error:",
      error
    );

    showToast(
      "Koneksi gagal",
      "wifi"
    );
  }
};