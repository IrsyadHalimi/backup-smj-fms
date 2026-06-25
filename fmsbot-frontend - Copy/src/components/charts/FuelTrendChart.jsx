import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid
}
from "recharts";

export default function FuelTrendChart({
  data
}) {

  return (
    <ResponsiveContainer
      width="100%"
      height={320}
    >

      <LineChart data={data}>

        <CartesianGrid
          stroke="#16263d"
        />

        <XAxis
          dataKey="day"
          stroke="#7f8ea3"
        />

        <YAxis
          stroke="#7f8ea3"
        />

        <Tooltip />

        <Line
          type="monotone"
          dataKey="rate"
          stroke="#1d6fff"
          strokeWidth={3}
          dot={{
            r: 4
          }}
        />

      </LineChart>

    </ResponsiveContainer>
  );
}