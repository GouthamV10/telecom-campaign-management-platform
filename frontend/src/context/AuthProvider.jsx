import { useState } from "react";
import { AuthContext } from "./AuthContext";
import { decodeToken, isTokenExpired } from "../services/jwt";

export function AuthProvider({ children }) {
    const [token, setToken] = useState(() => {
        const storedToken = localStorage.getItem("token");
        if (storedToken && isTokenExpired(storedToken)) {
            localStorage.removeItem("token");
            return null;
        }
        return storedToken;
    });

    const [role, setRole] = useState(() => {
        const storedToken = localStorage.getItem("token");
        if (!storedToken || isTokenExpired(storedToken)) {
            return null;
        }
        const decoded = decodeToken(storedToken);
        return decoded ? decoded.role : null;
    });

    const login = (newToken) => {
        localStorage.setItem("token", newToken);
        setToken(newToken);
        const payload = decodeToken(newToken);
        setRole(payload ? payload.role : null);
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