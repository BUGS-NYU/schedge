import { combineReducers } from "redux";

import wishlist from "./wishlist";
import scheduled from "./courseSelect";

export default combineReducers({ wishlist, scheduled });
