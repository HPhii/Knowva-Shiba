import { create } from "zustand";
import axios from "axios";
import api from "../config/axios";

const API_URL = import.meta.env.MODE === "development" ? "http://localhost:8080/api" : "/api";

axios.defaults.withCredentials = true;

export const useAuthStore = create((set) => ({
	user: null,
	isAuthenticated: false,
	error: null,
	isLoading: false,
	isCheckingAuth: true,
	message: null,

	signup: async (email, password, name) => {
		set({ isLoading: true, error: null });
		try {
			const response = await axios.post(`${API_URL}/signup`, { email, password, name });
			set({ user: response.data.user, isAuthenticated: true, isLoading: false });
		} catch (error) {
			set({ error: error.response.data.message || "Error signing up", isLoading: false });
			throw error;
		}
	},
	// login: async (email, password) => {
	// 	set({ isLoading: true, error: null });
	// 	try {
	// 		const response = await axios.post(`${API_URL}/login`, { email, password });
	// 		set({
	// 			isAuthenticated: true,
	// 			user: response.data.user,
	// 			error: null,
	// 			isLoading: false,
	// 		});
	// 	} catch (error) {
	// 		set({ error: error.response?.data?.message || "Error logging in", isLoading: false });
	// 		throw error;
	// 	}
	// },

	logout: async () => {
		set({ isLoading: true, error: null });
		try {
			await axios.post(`${API_URL}/logout`);
			set({ user: null, isAuthenticated: false, error: null, isLoading: false });
		} catch (error) {
			set({ error: "Error logging out", isLoading: false });
			throw error;
		}
	},
	verifyEmail: async ({ code, email }) => {
	set({ isLoading: true, error: null });

	try {
		const response = await api.post(`${API_URL}/verify-email?email=${encodeURIComponent(email)}&otp=${code}`);		
		set({ user: response.data.user, isAuthenticated: true, isLoading: false });
		return response.data;
	} catch (error) {
		set({ error: error.response?.data?.message || "Error verifying email", isLoading: false });
		throw error;
	}
},
	checkAuth: async () => {
		set({ isCheckingAuth: true, error: null });
		try {
			const response = await axios.get(`${API_URL}/check-auth`);
			set({ user: response.data.user, isAuthenticated: true, isCheckingAuth: false });
		} catch (error) {
			set({ error: null, isCheckingAuth: false, isAuthenticated: false });
		}
	},
	forgotPassword: async (email) => {
	set({ isLoading: true, error: null });
	try {
		const response = await api.post(`${API_URL}/send-reset-otp?email=${encodeURIComponent(email)}`);
		set({ message: response.data.message, isLoading: false });
	} catch (error) {
		set({
			isLoading: false,
			error: error.response?.data?.message || "Error sending reset OTP",
		});
		throw error; // để handle trong component
	}
},
	resetPassword: async (token, password) => {
		set({ isLoading: true, error: null });
		try {
			const response = await axios.post(`${API_URL}/reset-password/${token}`, { password });
			set({ message: response.data.message, isLoading: false });
		} catch (error) {
			set({
				isLoading: false,
				error: error.response.data.message || "Error resetting password",
			});
			throw error;
		}
	},
}));
