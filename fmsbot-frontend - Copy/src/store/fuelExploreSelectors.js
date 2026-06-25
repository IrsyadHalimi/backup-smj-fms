import { useFuelExploreStore } from "./fuelExploreStore";

export const useDashboardSummary = () =>
  useFuelExploreStore(
    (state) => state.dashboard?.summary
  );

export const useFuelTrend = () =>
  useFuelExploreStore(
    (state) => state.dashboard?.fuelTrend
  );

export const useModelRates = () =>
  useFuelExploreStore(
    (state) => state.dashboard?.modelRates
  );

export const useSearch = () =>
  useFuelExploreStore(
    (state) => state.search
  );

export const useUnits =
  () =>
    useFuelExploreStore(
      state => state.units
    );

export const usePagination =
  () =>
    useFuelExploreStore(
      state =>
        state.pagination
    );