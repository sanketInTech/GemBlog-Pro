import React, { useState } from 'react'
import { useAppContext } from '../../context/AppContext';
import toast from 'react-hot-toast';

const Login = () => {

  const { axios, setToken } = useAppContext();
  const [isRegister, setIsRegister] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async (e) =>{
    e.preventDefault(); 
    try {
      const { data } = await axios.post('/api/admin/login', {email, password});
      if(data.success){
        setToken(data.token);
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.user));
        // Bearer prefix required by this backend's JwtAuthenticationFilter.
        axios.defaults.headers.common['Authorization'] = `Bearer ${data.token}`;
        toast.success('Login successful!');
      }
      else{
        toast.error(data.message);
      }
    } catch (error) {
      toast.error(error.message);
    }
  }

  const handleRegister = async (e) =>{
    e.preventDefault();
    if(!name || !email || !password) return toast.error('All fields required');
    if(password.length < 6) return toast.error('Password must be 6+ characters');
    
    try {
      const { data } = await axios.post('/api/admin/register', {name, email, password});
      if(data.success){
        toast.success(data.message);
        setIsRegister(false);
        setName('');
        setEmail('');
        setPassword('');
      }
      else{
        toast.error(data.message);
      }
    } catch (error) {
      toast.error(error.message);
    }
  }

  return (
    <div className='flex items-center justify-center h-screen'>
      <div className='w-full max-w-sm p-6 max-md:m-6 border border-blue-700/30 shadow-xl shadow-blue-700/15 rounded-lg'>
        <div className='flex flex-col items-center justify-center'>
          <div className='w-full py-6 text-center'>
            <h1 className='text-3xl font-bold'><span className='text-blue-700'>Admin</span> {isRegister ? 'Register' : 'Login'}</h1>
            <p className='font-light text-sm text-gray-600'>{isRegister ? 'Create new account' : 'Enter credentials to access admin panel'}</p>
          </div>

          <form onSubmit={isRegister ? handleRegister : handleLogin} className='mt-6 w-full text-gray-600'>
            
            {isRegister && (
              <div className='flex flex-col mb-6'>
                <label className='text-sm font-medium mb-1'>Name</label>
                <input onChange={e => setName(e.target.value)} value={name} type="text" required placeholder='Full Name' className='border-b-2 border-gray-300 p-2 outline-none'/>
              </div>
            )}

            <div className='flex flex-col mb-6'>
              <label className='text-sm font-medium mb-1'>Email</label>
              <input onChange={e => setEmail(e.target.value)} value={email} type="email" required placeholder='Email' className='border-b-2 border-gray-300 p-2 outline-none'/>
            </div>

            <div className='flex flex-col mb-6'>
              <label className='text-sm font-medium mb-1'>Password</label>
              <input onChange={e => setPassword(e.target.value)} value={password} type="password" required placeholder='Password' className='border-b-2 border-gray-300 p-2 outline-none'/>
            </div>

            <button type='submit' className='w-full py-3 font-medium bg-blue-700 text-white rounded cursor-pointer hover:bg-blue-700/90 transition-all'>
              {isRegister ? 'Register' : 'Login'}
            </button>
          </form>

          <p className='mt-6 text-sm text-gray-600'>
            {isRegister ? 'Have account? ' : "Don't have account? "}
            <button onClick={() => {setIsRegister(!isRegister); setName(''); setEmail(''); setPassword('')}} className='text-blue-700 font-semibold hover:text-blue-800'>
              {isRegister ? 'Login' : 'Register'}
            </button>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login