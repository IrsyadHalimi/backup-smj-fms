import { useEffect } from "react";
import mockData from "../data/mock-dashboard.json";
import { getFuelExploreData } from "../services/fuelExploreService";
import { useFuelExploreStore } from "../store/fuelExploreStore";

export const useFuelExploreData = () => {
  const setDashboard =
    useFuelExploreStore(
      (state) => state.setDashboard
    );

  const setLoadingDashboard =
    useFuelExploreStore(
      (state) => state.setLoadingDashboard
    );

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoadingDashboard(true);

        const data = await getFuelExploreData();

        setDashboard(data);
      } catch (error) {
        console.error(
          "Failed fetch dashboard:",
          error
        );
      } finally {
        setLoadingDashboard(false);
      }
    };

    fetchDashboard();
  }, [setDashboard, setLoadingDashboard]);
};