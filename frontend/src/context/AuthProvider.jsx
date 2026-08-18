import { useState } from "react";
import { AuthContext } from "./AuthContext";
import { decodeToken } from "../services/jwt";

export function AuthProvider({ children }) {
    const [token, setToken] = useState(() => localStorage.getItem("token"));

    const [role, setRole] = useState(()=>{
        const storedToken = localStorage.getItem("token");

        if(!storedToken){
            return null;
        }

        return decodeToken(storedToken).role;
    });

    const login = (token) => {
        localStorage.setItem("token", token);
        setToken(token);
        const payload = decodeToken(token);
        setRole(payload.role)
    };

    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
        setRole(null);
    };

    return (
        <AuthContext.Provider value={{ token, role, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}