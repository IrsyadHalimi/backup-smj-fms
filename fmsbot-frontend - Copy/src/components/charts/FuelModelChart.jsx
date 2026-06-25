import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip
}
from "recharts";

export default function FuelModelChart({
  data
}) {

  return (
    <ResponsiveContainer
      width="100%"
      height={320}
    >

      <BarChart
        layout="vertical"
        data={data}
      >

        <XAxis
          type="number"
        />

        <YAxis
          type="category"
          dataKey="model"
        />

        <Tooltip />

        <Bar
          dataKey="rate"
          radius={[0,6,6,0]}
          fill="#00d084"
        />

      </BarChart>

    </ResponsiveContainer>
  );
}