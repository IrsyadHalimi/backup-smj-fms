import {
  LineChart,
  Line,
  ResponsiveContainer
}
from "recharts";

export default function Sparkline({
  data
}) {

  const chartData =
    data.map(value => ({
      value
    }));

  return (
    <ResponsiveContainer
      width={100}
      height={40}
    >

      <LineChart
        data={chartData}
      >

        <Line
          dataKey="value"
          stroke="#00d084"
          dot={false}
          strokeWidth={2}
        />

      </LineChart>

    </ResponsiveContainer>
  );
}