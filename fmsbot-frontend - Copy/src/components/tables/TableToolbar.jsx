import {
  Search
} from "lucide-react";

import {
  useFuelExploreStore
}
from "../../store/fuelExploreStore";

import "../../styles/css/TableToolbar.css";

export default function TableToolbar() {

  const setSearch =
    useFuelExploreStore(
      state => state.setSearch
    );

  return (

    <div className="table-toolbar">

        <div className="search-box">
            <Search size={16} />
            <input
            placeholder="Search model or unit..."
            onChange={(e) => setSearch(e.target.value)}
            />
        </div>

        <div className="toolbar-actions">
            <button className="toolbar-btn">
            Filter
            </button>

            <button className="toolbar-btn">
            Export
            </button>
        </div>

    </div>
  );
}