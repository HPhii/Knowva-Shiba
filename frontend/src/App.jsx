import React, { useEffect } from 'react'
import { Navigate, RouterProvider, createBrowserRouter } from 'react-router-dom'

import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import SignUpPage from './pages/SignUpPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import EmailVerificationPage from './pages/EmailVerificationPage'
// import Dashboard from './components/dashboard'

import { Toaster } from 'react-hot-toast'

import LoadingSpinner from './components/LoadingSpinner'
import FloatingShape from './components/FloatingShape'
import { useSelector } from 'react-redux'

function App() {
  const user = useSelector(store => store.user)

  if (isCheckingAuth) return <LoadingSpinner />

  const ProtectedRoute = ({ children }) => {
    if (!user?.token) {
      return <Navigate to="/login" replace />
    }
    return children
  }

  const RedirectAuthenticatedUser = ({ children }) => {
    if (user?.token) {
      return <Navigate to="/" replace />
    }
    return children
  }

  const router = createBrowserRouter([
    {
      path: '/',
      element: (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-green-900 to-emerald-900 flex items-center justify-center relative overflow-hidden">
          <FloatingShape
            color="bg-green-500"
            size="w-64 h-64"
            top="-5%"
            left="10%"
            delay={0}
          />
          <FloatingShape
            color="bg-emerald-500"
            size="w-48 h-48"
            top="70%"
            left="80%"
            delay={5}
          />
          <FloatingShape
            color="bg-lime-500"
            size="w-32 h-32"
            top="40%"
            left="-10%"
            delay={2}
          />
          <Layout />
          <Toaster />
        </div>
      ),
      children: [
        {
          path: '',
          element: (
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          )
        },
        {
          path: 'register',
          element: (
            <RedirectAuthenticatedUser>
              <SignUpPage />
            </RedirectAuthenticatedUser>
          )
        },
        {
          path: 'login',
          element: (
            <RedirectAuthenticatedUser>
              <LoginPage />
            </RedirectAuthenticatedUser>
          )
        },
        {
          path: 'verify-email',
          element: <EmailVerificationPage />
        },
        {
          path: 'forgot-password',
          element: (
            <RedirectAuthenticatedUser>
              <ForgotPasswordPage />
            </RedirectAuthenticatedUser>
          )
        }
      ]
    },
    // {
    //   path: '/dashboard',
    //   element: (
    //     <ProtectedRoute>
    //       <Dashboard />
    //     </ProtectedRoute>
    //   )
    // },
    {
      path: '*',
      element: <Navigate to="/" replace />
    }
  ])

  return <RouterProvider router={router} />
}

export default App
