import { BASE_URL } from "../helpers/utils/apiConfig";

export const getFuelExploreData = async (startDate, endDate) => {
  try {
    // Membangun query string secara dinamis
    const params = new URLSearchParams();
    if (startDate) params.append("start_date", startDate);
    if (endDate) params.append("end_date", endDate);

    const queryString = params.toString() ? `?${params.toString()}` : "";

    const response = await fetch(
      `${BASE_URL}/api/fuel-explore/${queryString}`
    );

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Fuel Explore Error:", error);
    throw error;
  }
};

export const getFuelExploreUnits = async ({ page = 1, limit = 50, startDate, endDate }) => {
  try {
    const params = new URLSearchParams({ page, limit });
    if (startDate) params.append("start_date", startDate);
    if (endDate) params.append("end_date", endDate);

    const response = await fetch(
      `${BASE_URL}/api/fuel-explore/units/?${params.toString()}`
    );

    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Fuel Explore Units Error:", error);
    throw error;
  }
};