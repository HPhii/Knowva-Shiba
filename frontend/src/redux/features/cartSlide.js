import { createSlice } from "@reduxjs/toolkit";

const cartSlide = createSlice({
    name: "cart",
    initialState: [],
    reducers: {
        addProduct: (state, action) => {
            const product = action.payload;
            state.push(product);
        },
    },
});

export const {addProduct} = cartSlide.actions;
export default cartSlide.reducer;