import { Calendar, Filter } from "lucide-react";
import "../../styles/css/DateRangePicker.css";
import React, {
  useState,
  useEffect
} from "react";

export default function DateRangePicker({ onApplyFilter, initialStart = "", initialEnd = "" }) {
  const [startDate, setStartDate] = useState(initialStart);
  const [endDate, setEndDate] = useState(initialEnd);
  const [isOpen, setIsOpen] = useState(false);

  const handleApply = (e) => {
    e.preventDefault();
    if (startDate && endDate) {
      if (startDate > endDate) {
          alert("Tanggal mulai tidak boleh melebihi tanggal selesai");
          return;
      }
      onApplyFilter(startDate, endDate);
      setIsOpen(false);
    }
  };

  useEffect(() => {
    setStartDate(initialStart);
  }, [initialStart]);

  useEffect(() => {
    setEndDate(initialEnd);
  }, [initialEnd]);

  return (
    <div className="date-picker-wrapper">
      <button className="date-picker-trigger-btn" onClick={() => setIsOpen(!isOpen)}>
        <Calendar size={16} />
        <span>
          {startDate && endDate 
            ? `${startDate} s/d ${endDate}` 
            : "Filter Tanggal Kerja"}
        </span>
      </button>

      {isOpen && (
        <div className="date-picker-dropdown-card">
          <form onSubmit={handleApply} className="date-picker-form">
            <div className="date-input-group">
              <label>Tanggal Mulai</label>
              <input 
                type="date" 
                value={startDate} 
                onChange={(e) => setStartDate(e.target.value)} 
                required
              />
            </div>

            <div className="date-input-group">
              <label>Tanggal Selesai</label>
              <input 
                type="date" 
                value={endDate} 
                min={startDate}
                onChange={(e) => setEndDate(e.target.value)} 
                required
              />
            </div>

            <div className="date-picker-actions">
              <button type="button" className="btn-cancel" onClick={() => setIsOpen(false)}>
                Batal
              </button>
              <button type="submit" disabled={!startDate || !endDate} className="btn-apply">
                <Filter size={14} /> Terapkan
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}