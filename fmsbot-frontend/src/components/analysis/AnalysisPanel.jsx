import "../../styles/css/AnalysisPanel.css";

export default function AnalysisPanel() {

  return (
    <div className="analysis-panel">

      <div className="analysis-title">
        AI ANALYSIS
      </div>

      <div className="analysis-section">

        <h4>PERFORMA TERBAIK</h4>

        <p>
          PC1250-8 memiliki fuel rate
          terbaik sebesar 38.21 L/H.
        </p>

      </div>

      <div className="analysis-section danger">

        <h4>PERFORMA TERENDAH</h4>

        <p>
          HD785-7R memiliki konsumsi
          tertinggi 101.87 L/H.
        </p>

      </div>

      <div className="analysis-section">

        <h4>REKOMENDASI</h4>

        <ul>
          <li>
            Evaluasi kondisi engine.
          </li>

          <li>
            Kurangi idle time.
          </li>

          <li>
            Monitoring operator.
          </li>
        </ul>

      </div>

    </div>
  );
}