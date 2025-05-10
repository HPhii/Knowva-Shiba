import { motion } from 'framer-motion'
import { useState } from 'react'
import Input from '../components/Input'
import { ArrowLeft, Loader, Mail, Lock } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import api from '../config/axios'

const ForgotPasswordPage = () => {
  const [step, setStep] = useState(1) // Step 1: Email input, Step 2: OTP + new password
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const navigate = useNavigate()

  const handleSendOtp = async e => {
    e.preventDefault()
    setIsLoading(true)

    try {
      await api.post(`/send-reset-otp?email=${encodeURIComponent(email)}`)
      toast.success('OTP sent to your email')
      setStep(2)
    } catch (err) {
      toast.error(err.response?.data || 'Failed to send OTP')
    } finally {
      setIsLoading(false)
    }
  }

  const handleResetPassword = async e => {
    e.preventDefault()
    if (password !== confirmPassword) {
      toast.error('Passwords do not match')
      return
    }

    setIsLoading(true)
    try {
      await api.post('/reset-password', {
        email,
        otp,
        newPassword: password
      })

      toast.success('Password reset successfully. Redirecting to login...')
      setTimeout(() => navigate('/login'), 1500)
    } catch (err) {
      toast.error(err.response?.data || 'Failed to reset password')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="max-w-md w-full bg-gray-800 bg-opacity-50 backdrop-filter backdrop-blur-xl rounded-2xl shadow-xl overflow-hidden"
    >
      <div className="p-8">
        <h2 className="text-3xl font-bold mb-6 text-center bg-gradient-to-r from-green-400 to-emerald-500 text-transparent bg-clip-text">
          Forgot Password
        </h2>

        {step === 1 ? (
          <form onSubmit={handleSendOtp}>
            <p className="text-gray-300 mb-6 text-center">
              Enter your email address and we’ll send you a 6-digit OTP to reset
              your password.
            </p>
            <Input
              icon={Mail}
              type="email"
              placeholder="Email Address"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="w-full py-3 px-4 bg-gradient-to-r from-green-500 to-emerald-600 text-white font-bold rounded-lg shadow-lg hover:from-green-600 hover:to-emerald-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:ring-offset-gray-900 transition duration-200"
              type="submit"
              disabled={isLoading}
            >
              {isLoading ? (
                <Loader className="size-6 animate-spin mx-auto" />
              ) : (
                'Send OTP'
              )}
            </motion.button>
          </form>
        ) : (
          <form onSubmit={handleResetPassword}>
            <p className="text-gray-300 mb-6 text-center">
              Enter the 6-digit OTP sent to {email} and choose a new password.
            </p>
            <Input
              icon={Mail}
              type="text"
              placeholder="OTP Code"
              value={otp}
              onChange={e => setOtp(e.target.value)}
              required
            />
            <Input
              icon={Lock}
              type="password"
              placeholder="New Password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
            <Input
              icon={Lock}
              type="password"
              placeholder="Confirm Password"
              value={confirmPassword}
              onChange={e => setConfirmPassword(e.target.value)}
              required
            />
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="w-full py-3 px-4 bg-gradient-to-r from-green-500 to-emerald-600 text-white font-bold rounded-lg shadow-lg hover:from-green-600 hover:to-emerald-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:ring-offset-gray-900 transition duration-200"
              type="submit"
              disabled={isLoading}
            >
              {isLoading ? 'Resetting...' : 'Reset Password'}
            </motion.button>
          </form>
        )}
      </div>

      <div className="px-8 py-4 bg-gray-900 bg-opacity-50 flex justify-center">
        <Link
          to="/login"
          className="text-sm text-green-400 hover:underline flex items-center"
        >
          <ArrowLeft className="h-4 w-4 mr-2" /> Back to Login
        </Link>
      </div>
    </motion.div>
  )
}

export default ForgotPasswordPage
