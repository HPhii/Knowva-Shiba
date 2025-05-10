import { createSlice } from "@reduxjs/toolkit";

const userSlide = createSlice({
    name:"user",
    initialState: null,
    reducers: {
        login:(state, actions) => {
            state = actions.payload;
            return state;
        },
        logout:() => {
            return null;
        },
    },
});

export const { login, logout } = userSlide.actions;
export default userSlide.reducer;