import { combineReducers } from '@reduxjs/toolkit';
import counterReducer from '../features/counterSlice';
import  useReducer  from '../features/userSlide';
import cartReducer from '../features/cartSlide';

export const rootReducer = combineReducers({
      counter: counterReducer,
      user: useReducer,
      cart: cartReducer,
  })