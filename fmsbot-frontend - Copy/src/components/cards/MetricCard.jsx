import "../../styles/css/MetricCard.css";

export default function MetricCard({
  icon,
  title,
  value,
  unit,
  change,
  iconColor
}) {
  const positive = change > 0;

  return (
    <div className="metric-card">

      <div
        className="metric-icon"
        style={{
          background: iconColor
        }}
      >
        {icon}
      </div>

      <div className="metric-content">

        <p className="metric-title">
          {title}
        </p>

        <h2 className="metric-value">
          {value}
          <span>{unit}</span>
        </h2>

        <p
          className={
            positive
              ? "metric-positive"
              : "metric-negative"
          }
        >
          {positive ? "↑" : "↓"}{" "}
          {Math.abs(change)}%
        </p>

      </div>

    </div>
  );
}