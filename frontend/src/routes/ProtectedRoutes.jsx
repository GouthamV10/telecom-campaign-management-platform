import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

function ProtectedRoutes(){
  const {token} = useAuth();

  if(!token){
    return <Navigate to="/login" replace/>
  }

  return <Outlet />
}

export default ProtectedRoutes;