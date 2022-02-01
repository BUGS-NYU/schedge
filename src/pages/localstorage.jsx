// Load state from localstorage
export const loadState = () => {
  try {
    const serializedState = localStorage.getItem("bobcat-store");
    if (serializedState === null) return undefined;
    return JSON.parse(serializedState);
  } catch (err) {
    return undefined;
  }
};

// Save state to localstorage
export const saveState = (state) => {
  try {
    const serializedState = JSON.stringify(state);
    localStorage.setItem("bobcat-store", serializedState);
  } catch (err) {
    console.error("Could not save state to local storage", err);
  }
};
