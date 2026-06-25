import {
  useMemo
} from "react";

import {
  useFuelExploreStore
}

from "../../store/fuelExploreStore";

import Sparkline
from "../charts/Sparkline";

import PerformanceBadge
from "./PerformanceBadge";

import "../../styles/css/PerformanceBadge.css";
import { width } from "highcharts";


export default function PerformanceTable() {
  const units =
    useFuelExploreStore(
      state => state.units
    );

  const search =
    useFuelExploreStore(
      state => state.search
    );

  const rows =
    useMemo(() => {

      return units.filter(
        item =>
          item.model
            .toLowerCase()
            .includes(
              search.toLowerCase()
            )
      );

    }, [
      units,
      search
    ]);

  return (

    <div className="performance-table">

      <table style={{width:"100%"}}>

        <thead>
          <tr>
            <th>No</th>
            <th>Unit</th>
            <th>Fuel</th>
            <th>Hour</th>
            <th>Rate</th>
            <th>VS AVG</th>
            <th>Trend</th>
            <th>Status</th>
          </tr>
        </thead>

        <tbody>
          {
            rows.map(row => (

              <tr key={row.id}>

                <td>{row.id}</td>

                <td>{row.model}</td>

                <td>
                  {row.fuel.toLocaleString()}
                </td>

                <td>{row.hour}</td>

                <td>
                  {row.fuelRate}
                </td>

                <td>

                  <span
                    className={
                      row.vsAverage > 0
                        ? "up"
                        : "down"
                    }
                  >
                    {row.vsAverage}%
                  </span>

                </td>

                <td>

                  <Sparkline
                    data={row.trend}
                  />

                </td>

                <td>

                  <PerformanceBadge
                    value={
                      row.performance
                    }
                  />

                </td>

              </tr>

            ))
          }

        </tbody>

      </table>

    </div>

  );
}