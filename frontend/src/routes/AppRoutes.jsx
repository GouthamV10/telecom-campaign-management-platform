import { BrowserRouter, Route, Routes } from "react-router-dom";
import Login from "../pages/Login";
import ProtectedRoutes from "./ProtectedRoutes";

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />}/>
        <Route element={<ProtectedRoutes />}>
          <Route path="/dashboard" element={<h1>Dashboard</h1>} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;