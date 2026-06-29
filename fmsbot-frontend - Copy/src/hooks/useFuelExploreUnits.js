// hooks/useFuelExploreUnits.js
import { useEffect } from "react";
import { getFuelExploreUnits } from "../services/fuelExploreService";
import { useFuelExploreStore } from "../store/fuelExploreStore";

export const useFuelExploreUnits = (
  page = 1,
  limit = 50,
  startDate = null,
  endDate = null
) => {
  const setUnits = useFuelExploreStore((state) => state.setUnits);
  const setLoadingUnits = useFuelExploreStore((state) => state.setLoadingUnits);

  useEffect(() => {
    const fetchUnits = async () => {
      try {
        setLoadingUnits(true);
        // PERBAIKAN DI SINI: Bungkus parameter dengan { } menjadi sebuah object!
        const response = await getFuelExploreUnits({
          page,
          limit,
          startDate,
          endDate,
        });

        setUnits(response.data, response.pagination);
      } catch (error) {
        console.error("Failed fetch units:", error);
      } finally {
        setLoadingUnits(false);
      }
    };

    fetchUnits();
  }, [
    page,
    limit,
    startDate,
    endDate
  ]); // Hook tetap memantau perubahan page dan limit dari FuelExplorer
};