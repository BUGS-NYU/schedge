import create from "zustand";

export const usePageState = create((set) => {
  return {
    year: "2021",
    semester: "sp",

    update: set,
  };
});
