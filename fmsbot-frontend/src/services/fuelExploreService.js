import {
  BASE_URL
} from "../helpers/utils/apiConfig";

export const getFuelExploreData = async () => {
  try {
    const response = await fetch(
      `${BASE_URL}/api/fuel-explore/`
    );

    if (!response.ok) {
      throw new Error(
        `HTTP ${response.status}`
      );
    }

    return await response.json();

  } catch (error) {
    console.error(
      "Fuel Explore Error:",
      error
    );

    throw error;
  }
};

export const getFuelExploreUnits = async ({
  page = 1,
  limit = 50
}) => {

  try {
    const response = await fetch(
      `${BASE_URL}/api/fuel-explore/units/?page=${page}&limit=${limit}`
    );

    if (!response.ok) {
      throw new Error(
        `HTTP Error: ${response.status}`
      );
    }

    return await response.json();

  } catch (error) {
    console.error(
      "Fuel Explore Error:",
      error
    );

    throw error;
  }
};