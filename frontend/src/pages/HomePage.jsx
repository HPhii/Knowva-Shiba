import { motion } from "framer-motion";
import { Link } from "react-router-dom";

const HomePage = () => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className='w-full h-screen bg-gray-800 bg-opacity-50 backdrop-filter backdrop-blur-xl flex flex-col items-center justify-center'
    >
      <div className='text-center text-white px-8'>
        <h1 className='text-4xl font-bold mb-4 bg-gradient-to-r from-green-400 to-emerald-500 text-transparent bg-clip-text'>
          Welcome to Our Platform
        </h1>
        <p className='text-lg mb-6'>
          Join us to experience the best services and features we have to offer.
        </p>

        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          className='py-3 px-6 bg-gradient-to-r from-green-500 to-emerald-600 text-white font-bold rounded-lg shadow-lg hover:from-green-600 hover:to-emerald-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:ring-offset-gray-900 transition duration-200'
        >
          <Link to='/signup'>Get Started</Link>
        </motion.button>

        <div className='mt-6'>
          <p className='text-sm text-gray-400'>
            Already have an account?{" "}
            <Link to='/login' className='text-green-400 hover:underline'>
              Log in
            </Link>
          </p>
        </div>
      </div>

      <div className='absolute bottom-10 text-center w-full'>
        <p className='text-sm text-gray-400'>
          &copy; 2025 Our Platform. All rights reserved.
        </p>
      </div>
    </motion.div>
  );
};

export default HomePage;
