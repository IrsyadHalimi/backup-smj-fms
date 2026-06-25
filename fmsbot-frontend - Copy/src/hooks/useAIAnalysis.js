import { useState, useEffect } from "react";
import { AI_INSIGHT_URL } from "../helpers/utils/apiConfig"; // Sesuaikan relative path-nya

export default function useAIAnalysis(mainData, endpoint = AI_INSIGHT_URL) {
  const [analisis, setAnalisis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!mainData) {
      setAnalisis(null);
      return;
    }

    const fetchAnalysis = async () => {
      setLoading(true);
      setError(null);
      try {
        // const response = await fetch(endpoint, {
        //   method: "POST",
        //   headers: {
        //     "Content-Type": "application/json",
        //   },
        //   body: JSON.stringify({ data: mainData }),
        // });

        // const result = await response.json();

        // if (result.status === "success") {
        //   setAnalisis(result.analysis);
        // } else {
        //   setError(result.message || "Gagal memuat analisis");
        // }
      } catch (err) {
        setError("Gagal terhubung ke server backend.");
      } finally {
        setLoading(false);
      }
    };

    fetchAnalysis();
  }, [mainData, endpoint]);

  // Kembalikan objek berisi state yang dibutuhkan oleh komponen luar
  return { analisis, loading, error };
}