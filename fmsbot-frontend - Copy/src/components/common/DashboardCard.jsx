import "../../styles/css/DashboardCard.css";

export default function DashboardCard({
  title,
  children,
  action
}) {
  return (
    <div className="dashboard-card">

      <div className="dashboard-card-header">

        <h3>{title}</h3>

        {action}

      </div>

      <div className="dashboard-card-body">
        {children}
      </div>

    </div>
  );
}