import "../../styles/css/Header.css";

import {
  Calendar,
  Filter,
  Download
} from "lucide-react";

import DateRangePicker from "../ui/DateRangePicker";

export default function Header({ onFilterChange, currentStart, currentEnd }) {
  return (
    <header className="header">

      <div>

        <h1>
          Fuel Explorer
        </h1>

        <p>
          Performa Unit Berdasarkan
          Konsumsi Fuel
        </p>

      </div>

      <div className="header-actions">
        {/* Reusable DateRangePicker menggantikan button statis lama */}
        <DateRangePicker 
          onApplyFilter={onFilterChange} 
          initialStart={currentStart} 
          initialEnd={currentEnd} 
        />
      </div>

    </header>
  );
}