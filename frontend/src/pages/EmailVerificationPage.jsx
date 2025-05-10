import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import toast from 'react-hot-toast'
import api from '../config/axios'

const EmailVerificationPage = () => {
  const [code, setCode] = useState(['', '', '', '', '', ''])
  const [isLoading, setIsLoading] = useState(false)
  const inputRefs = useRef([])
  const navigate = useNavigate()

  const email = localStorage.getItem('email')

  const handleChange = (index, value) => {
    const newCode = [...code]

    if (value.length > 1) {
      const pasted = value.slice(0, 6).split('')
      for (let i = 0; i < 6; i++) {
        newCode[i] = pasted[i] || ''
      }
      setCode(newCode)
      const lastIndex = newCode.findLastIndex(d => d !== '')
      const focusIndex = lastIndex < 5 ? lastIndex + 1 : 5
      inputRefs.current[focusIndex]?.focus()
    } else {
      newCode[index] = value
      setCode(newCode)
      if (value && index < 5) {
        inputRefs.current[index + 1]?.focus()
      }
    }
  }

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !code[index] && index > 0) {
      inputRefs.current[index - 1]?.focus()
    }
  }

  const handleSubmit = async e => {
    e.preventDefault()
    if (!email) {
      toast.error('Missing email for verification')
      return
    }

    const otp = code.join('')
    if (otp.length !== 6) {
      toast.error('Invalid code')
      return
    }

    setIsLoading(true)
    try {
      await api.post(
        `/verify-email?email=${encodeURIComponent(email)}&otp=${otp}`
      )
      toast.success('Email verified successfully')
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data || 'Verification failed')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (code.every(d => d !== '')) {
      handleSubmit(new Event('submit'))
    }
  }, [code])

  return (
    <div className="max-w-md w-full bg-gray-800 bg-opacity-50 backdrop-filter backdrop-blur-xl rounded-2xl shadow-xl overflow-hidden">
      <motion.div
        initial={{ opacity: 0, y: -50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="p-8 w-full max-w-md"
      >
        <h2 className="text-3xl font-bold mb-6 text-center bg-gradient-to-r from-green-400 to-emerald-500 text-transparent bg-clip-text">
          Verify Your Email
        </h2>
        <p className="text-center text-gray-300 mb-6">
          Enter the 6-digit code sent to {email}
        </p>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="flex justify-between">
            {code.map((digit, index) => (
              <input
                key={index}
                ref={el => (inputRefs.current[index] = el)}
                type="text"
                maxLength="1"
                value={digit}
                onChange={e => handleChange(index, e.target.value)}
                onKeyDown={e => handleKeyDown(index, e)}
                className="w-12 h-12 text-center text-2xl font-bold bg-gray-700 text-white border-2 border-gray-600 rounded-lg focus:border-green-500 focus:outline-none"
              />
            ))}
          </div>

          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            type="submit"
            disabled={isLoading || code.some(d => !d)}
            className="w-full bg-gradient-to-r from-green-500 to-emerald-600 text-white font-bold py-3 px-4 rounded-lg shadow-lg hover:from-green-600 hover:to-emerald-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50 disabled:opacity-50"
          >
            {isLoading ? 'Verifying...' : 'Verify Email'}
          </motion.button>
        </form>
      </motion.div>
    </div>
  )
}

export default EmailVerificationPage
