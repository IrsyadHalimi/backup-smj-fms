import { create } from "zustand";

export const useFuelExploreStore = create((set) => ({
  dashboard: null,
  units: [],
  pagination: null,
  loadingDashboard: false,
  loadingUnits: false,

  search: "",

  setSearch: (value) =>
    set({ search: value }),

  setDashboard: (payload) =>
    set({
      dashboard: payload
    }),

  setUnits: (
      units,
      pagination
    ) =>
      set({
        units,
        pagination
      }),

  setLoadingDashboard: (value) =>
    set({
      loadingDashboard: value,
    }),
  
  setLoadingUnits: (value) =>
    set({
      loadingUnits: value,
    }),

  updateDashboard: (payload) =>
    set((state) => ({
      dashboard: {
        ...state.dashboard,
        ...payload,
      },
    })),
}));