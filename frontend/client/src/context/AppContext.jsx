// App context to share global states in different components.
import {createContext, useContext, useEffect, useState} from 'react'
import {useNavigate} from 'react-router-dom';
import axios from 'axios'
import toast from 'react-hot-toast';
    
axios.defaults.baseURL = import.meta.env.VITE_BASE_URL;


const AppContext = createContext();
// for appcontext create provider function.

export const AppProvider = ({ children })=>{

    const navigate = useNavigate();
    
    // token for user Authentication
    const [token, setToken] = useState(null);
    const [blogs, setBlogs] = useState([]); 
    // using input we can filter the blogs
    const [input, setInput] = useState("");

    // a function to get blog data
    const fetchBlogs = async()=>{
        try {
            const { data } = await axios.get('/api/blog/all');
            data.success ? setBlogs(data.blogs) : toast.error(data.message); 
        } catch (error) {
            toast.error(error.message);
        }
    }
    // functionality for user authentication

    // To excecute the function whenever we'll open the application.
    useEffect(()=>{
        fetchBlogs();
        const token = localStorage.getItem('token'); // 'token' key
        if(token){
            setToken(token);
            // NOTE: this backend's JwtAuthenticationFilter expects the standard
            // "Bearer <token>" scheme (unlike the original raw-token convention),
            // so the prefix must be included on every request.
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            // This token will be added in all the API call whenever token is available.
            //i.e whenver admin is logged in
        }
    }, [])

    const value = {
        axios, navigate, token, setToken, blogs, setBlogs, input, setInput
    };

    return (
        <AppContext.Provider value={value}>
            { children }
        </AppContext.Provider>
    )
}

export const useAppContext = () =>{
    return useContext(AppContext);
}

  