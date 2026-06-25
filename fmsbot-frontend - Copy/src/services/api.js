import {
  BASE_URL,
  API_URL
}
from "../helpers/utils/apiConfig";


// =========================================================
// FETCH HISTORY
// =========================================================

export const fetchHistoryApi = async () => {
  const start = performance.now();

  try {
    // 1. Tunggu sampai network fetch selesai sepenuhnya
    const response = await fetch(API_URL);

    // Ambil waktu tepat setelah network response kembali
    const networkDone = performance.now();

    // Validasi HTTP status (pencatatan jika backend Django error 500/404)
    if (!response.ok) {
      throw new Error(`HTTP Error! Status: ${response.status}`);
    }

    // 2. Tunggu sampai parsing JSON selesai sepenuhnya
    const data = await response.json();

    // Ambil waktu tepat setelah data object JSON terbentuk di memory
    const end = performance.now();

    console.log("👉 ISI DATA MENTAH:", data); // <--- Tambahkan ini sementara

    const rowCount = Array.isArray(data)
      ? data.length
      : data?.results?.length || data?.data?.length || 0;

    const sizeMB = (
      JSON.stringify(data).length /
      1024 /
      1024
    ).toFixed(2);

    // 3. Console log ditaruh di paling akhir setelah semua 'await' berhasil lulus
    console.log(`
      === HISTORY API TRACE ===
      Status       : ${response.status}
      Rows         : ${rowCount}
      Payload Size : ${sizeMB} MB
      Network Time : ${(networkDone - start).toFixed(2)} ms
      JSON Parse   : ${(end - networkDone).toFixed(2)} ms
      Total Time   : ${(end - start).toFixed(2)} ms
      =========================
    `);

    return data;
  } catch (error) {
    // Jika ada kegagalan di tengah jalan (Network error / JSON corrupt), log ini yang muncul
    console.error("=== HISTORY API TRACE (FAILED) ===");
    console.error("Error Detail:", error.message);
    throw error;
  }
};


// =========================================================
// FETCH SINGLE RECORD
// =========================================================

export const fetchSingleRecordApi = async (baseUrl, apiUrl, id) => {
  // Jika hooks mengirim 3 parameter, pakai apiUrl. Jika hooks kirim langsung 1 parameter, pke fallback global.
  const targetUrl = id ? `${apiUrl}${id}/` : `${API_URL}${baseUrl}/`;

  const response = await fetch(targetUrl);

  if (!response.ok) {
    throw new Error('Failed fetch detail record via websocket sync');
  }

  return await response.json();
};


// =========================================================
// VALIDATE RECORD
// =========================================================

export const validateRecordApi = async ({
  selectedRecord,
  hmValue,
  flowValue
}) => {

  const requestUrl =
    `${API_URL}${selectedRecord.id}/validate/`;

  const response = await fetch(
    requestUrl,
    {

      method: 'PATCH',

      headers: {
        'Content-Type':
          'application/json',

        'Accept':
          'application/json'
      },

      body: JSON.stringify({

        final_hm_value:
          String(hmValue),

        final_flow_value:
          String(flowValue),

        screening_status:
          'Verified'

      })

    }
  );

  return response;

};


// =========================================================
// RE-SCREENING
// =========================================================

export const reScreeningApi = async (
  id
) => {

  const response = await fetch(

    `${API_URL}${id}/re_screening/`,

    {
      method: 'POST',

      headers: {
        'Content-Type':
          'application/json',
      }
    }

  );

  return response;

};

export const getOFITFO = async () => {
  const targetUrl = id ? `${apiUrl}${id}/` : `${API_URL}${baseUrl}/`;

  const response = await fetch(targetUrl);

  if (!response.ok) {
    throw new Error('Failed fetch detail record via websocket sync');
  }

  return await response.json();
};