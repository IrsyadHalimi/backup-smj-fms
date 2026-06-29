import { useEffect } from "react";
import { getFuelExploreData } from "../services/fuelExploreService";
import { useFuelExploreStore } from "../store/fuelExploreStore";

export const useFuelExploreData = (startDate, endDate) => {
  const setDashboard = useFuelExploreStore((state) => state.setDashboard);
  const setLoadingDashboard = useFuelExploreStore((state) => state.setLoadingDashboard);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoadingDashboard(true);

        // Teruskan tanggal dari state komponen ke fungsi API service
        const data = await getFuelExploreData(startDate, endDate);

        setDashboard(data);
      } catch (error) {
        console.error("Failed fetch dashboard:", error);
      } finally {
        setLoadingDashboard(false);
      }
    };

    fetchDashboard();
    
    // Efek ini akan berjalan ulang secara otomatis begitu isi datepicker diganti
  }, [
    startDate,
    endDate
  ]);
};