import "../../styles/css/Header.css";

import {
  Calendar,
  Filter,
  Download
} from "lucide-react";

export default function Header() {
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

        <button>
          <Calendar size={16}/>
          May 2024
        </button>

        <button>
          <Filter size={16}/>
          Filter
        </button>

        <button>
          <Download size={16}/>
          Export
        </button>

      </div>

    </header>
  );
}