import { applyMiddleware, compose, createStore as _createStore } from "redux";
//import logger from "redux-logger";

import reducer from "./modules/reducer";

export default function createStore(data) {
  const middleware = [];

  if (!process.env.NODE_ENV || process.env.NODE_ENV === "development") {
    // Add any development tools
    //middleware.push(logger);
  }

  const finalCreateStore = compose(applyMiddleware(...middleware))(
    _createStore
  );

  return finalCreateStore(
    reducer,
    data,
    //window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()
  );
}
