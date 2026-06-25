import "../../styles/css/AnalysisPanel.css";
import { AI_INSIGHT_URL } from "../../helpers/utils/apiConfig";
import useAIAnalysis from "../../hooks/useAIAnalysis";

export default function AnalysisPanel({ 
  mainData, 
  endpoint = AI_INSIGHT_URL,
  title = "AI ANALYSIS",
  insightTitle = "INSIGHT DATA",
  conclusionTitle = "KESIMPULAN",
  recommendationTitle = "REKOMENDASI AI"
}) {
  
  const { analisis, loading, error } = useAIAnalysis(mainData, endpoint);

  // 1. STATE LOADING (Skeleton Mode)
  if (loading) {
    return (
      <div className="analysis-panel skeleton-active">
        <div className="analysis-title skeleton skeleton-title"></div>
        <div className="analysis-section">
          <div className="skeleton skeleton-sub-title"></div>
          <ul>
            <li className="skeleton skeleton-text"></li>
            <li className="skeleton skeleton-text w-75"></li>
          </ul>
        </div>
        <div className="analysis-section danger">
          <div className="skeleton skeleton-sub-title"></div>
          <p className="skeleton skeleton-text-block"></p>
        </div>
      </div>
    );
  }

  // 2. STATE GAGAL / KOSONG (Tetap di dalam Card dengan keterangan singkat)
  if (error || !analisis) {
    return (
      <div className="analysis-panel error-card">
        <div className="analysis-title">{title}</div>
        <div className="error-card-content">
          <span className="error-icon">⚠️</span>
          <p>
            {error 
              ? `Gagal memuat analisis otomatis: ${error}` 
              : "Tidak ada data summary dashboard yang tersedia untuk dianalisis."}
          </p>
        </div>
      </div>
    );
  }

  // 3. STATE SUKSES (Render Data Asli)
  return (
    <div className="analysis-panel">
      <div className="analysis-title">{title}</div>

      {/* Bagian Insights */}
      {analisis.insights && analisis.insights.length > 0 && (
        <div className="analysis-section">
          <h4>{insightTitle}</h4>
          <ul>
            {analisis.insights.map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Bagian Kesimpulan */}
      {analisis.kesimpulan && (
        <div className="analysis-section danger">
          <h4>{conclusionTitle}</h4>
          <p>{analisis.kesimpulan}</p>
        </div>
      )}

      {/* Bagian Rekomendasi */}
      {analisis.rekomendasi && analisis.rekomendasi.length > 0 && (
        <div className="analysis-section">
          <h4>{recommendationTitle}</h4>
          <ul>
            {analisis.rekomendasi.map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}