class localStorageContainer {
  constructor() {
    this.key = "bobcat-store";
    const serializedState = localStorage.getItem(this.key);
    if (serializedState == null) {
      this.state = undefined;
    } else {
      this.serializedState = JSON.parse(serializedState);
      this.localStorage = localStorage;
    }
  }

  saveState(state) {
    const serializedState = JSON.stringify(state);
    this.localStorage.setItem(this.key, serializedState);
  }

  getState(key) {
    return this.serializedState[key] ?? [];
  }
}

export default localStorageContainer;
