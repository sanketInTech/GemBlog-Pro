// setting up context API & creating auth flow
import { createContext, useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const AuthContext = createContext({});

const client = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL
})
export default AuthContext;

export const AuthProvider = ({ children }) =>{
    const authContext = useContext(AuthContext);
    const [userData, setUserData] = useState(authContext);
    const router = useNavigate();
    
    const handleRegister = async (name, email, password) =>{
        try {
            let request = await client.post('/register', {
                name: name,
                email: email,
                password: password
            })

            if(request.status === 201){
                return request.data.message;
            }
        } catch (error) {
            throw error;
        }
    }

    // handle login here
    const handleLogin = async (email, password) =>{
        try {
            let request = await client.post('/login', {
                email: email,
                password: password
            })

            if(request.status === 200){
                localStorage.setItem("token", request.data.token);
                router("/home");
            }
        } catch (error) {
            throw error;
        }
    }

    const data = {
        userData, setUserData, handleRegister, handleLogin
    }

    return (
        <AuthContext.Provider value={data}>
            {children}
        </AuthContext.Provider>
    )
}
