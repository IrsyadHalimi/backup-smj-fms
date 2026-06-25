import "../../styles/css/PerformanceBadge.css";

export default function PerformanceBadge({
  value
}) {

  let cls = "";

  switch (value) {

    case "Sangat Efisien":
      cls = "super-efficient";
      break;

    case "Efisien":
      cls = "efficient";
      break;

    case "Cukup":
      cls = "fair";
      break;

    case "Boros":
      cls = "waste";
      break;

    default:
      cls = "super-waste";
  }

  return (
    <span className={`badge ${cls}`}>
      {value}
    </span>
  );
}