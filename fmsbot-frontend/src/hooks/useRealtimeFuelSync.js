import { useEffect } from "react";
import { fetchSingleRecordApi } from "../services/api";

export const useRealtimeFuelSync = ({
  lastJsonMessage,
  BASE_URL,
  API_URL,
  setDbHistory,
  setRecords,
  setIsNewDataIn
}) => {
  useEffect(() => {
    if (!lastJsonMessage) return;

    const syncData = async () => {
      try {
        const id = lastJsonMessage?.id;
        if (!id) return;

        // Fetch data terbaru dari backend
        const freshData = await fetchSingleRecordApi(BASE_URL, API_URL, id);
        if (!freshData) return;

        // DEBUG LOG ORIGINAL DARI SERVER
        console.log("=== WEBSOCKET RAW DATA ===", freshData.id, freshData.screening_status);

        // =================================================================
        // LOGIKA ANTREAN (QUEUE CONTROL) DI FRONTEND + BYPASS VERIFIED
        // =================================================================
        
        // PENTING: Kita manipulasi state records saat ini untuk menghitung slot AI
        setRecords((prevRecords) => {
          let finalizedData = { ...freshData };

          // 🔥 INTERVENSI 1: JIKA STATUSNYA SUDAH VERIFIED (KLIK VALIDATOR), BYPASS LOGIKA AI
          if (finalizedData.screening_status === "Verified") {
            console.log(`[BYPASS REALTIME] ID ${finalizedData.id} terdeteksi Verified. Memaksa pindah ke baris teratas.`);
            
            // Bersihkan duplikasi data lama berdasarkan ID
            const filteredRecords = prevRecords.filter(
              (r) => String(r.id) !== String(finalizedData.id)
            );
            
            // Langsung tempatkan di baris paling atas (Index 0)
            return [finalizedData, ...filteredRecords];
          }

          // -------------------------------------------------------------
          // Logika Bawaan Antrean AI (Hanya berjalan jika status BUKAN Verified)
          // -------------------------------------------------------------
          // 1. Hitung berapa banyak record yang saat ini SEDANG diproses oleh AI
          const currentAiProcessingCount = prevRecords.filter(
            (r) => r.screening_status === "Processing"
          ).length;

          // 2. Cek Kondisi Slot AI (Maksimal 3)
          if (currentAiProcessingCount >= 3) {
            finalizedData.screening_status = "QUEUE";
            console.log(`[QUEUE] ID ${finalizedData.id} masuk antrean karena ${currentAiProcessingCount} unit sedang diproses AI.`);
          } else {
            finalizedData.screening_status = "Processing";
            console.log(`[AI PROCESS] ID ${finalizedData.id} langsung masuk ke pemrosesan AI.`);
          }

          // 3. Bersihkan duplikasi data lama berdasarkan ID
          const filteredRecords = prevRecords.filter(
            (r) => String(r.id) !== String(finalizedData.id)
          );

          // 4. Masukkan data hasil finalisasi ke baris paling atas tabel
          return [finalizedData, ...filteredRecords];
        });

        // Selaraskan juga data pada state DbHistory Anda
        setDbHistory((prevHistory) => {
          let finalizedData = { ...freshData };

          // 🔥 INTERVENSI 2: JIKA STATUSNYA SUDAH VERIFIED, SINKRONKAN DB HISTORY KE PALING ATAS
          if (finalizedData.screening_status === "Verified") {
            const filteredHistory = prevHistory.filter(
              (r) => String(r.id) !== String(finalizedData.id)
            );
            return [finalizedData, ...filteredHistory];
          }

          // -------------------------------------------------------------
          // Logika Bawaan DbHistory (Hanya berjalan jika status BUKAN Verified)
          // -------------------------------------------------------------
          const currentAiProcessingCount = prevHistory.filter(
            (r) => r.screening_status === "Processing"
          ).length;
          
          if (currentAiProcessingCount >= 3) {
            finalizedData.screening_status = "QUEUE";
          } else {
            finalizedData.screening_status = "Processing";
          }

          const filteredHistory = prevHistory.filter(
            (r) => String(r.id) !== String(finalizedData.id)
          );

          return [finalizedData, ...filteredHistory];
        });

        // Trigger animasi atau notifikasi data masuk
        setIsNewDataIn(true);
        setTimeout(() => {
          setIsNewDataIn(false);
        }, 3000);

      } catch (err) {
        console.error("Realtime sync failed:", err);
      }
    };

    syncData();
  }, [
    lastJsonMessage,
    BASE_URL,
    API_URL,
    setDbHistory,
    setRecords,
    setIsNewDataIn
  ]);
};