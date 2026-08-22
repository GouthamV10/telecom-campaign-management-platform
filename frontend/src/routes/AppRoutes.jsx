import { lazy } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoutes from "./ProtectedRoutes";
import AppLayout from "../layouts/AppLayout";
import ErrorBoundary from "../components/ErrorBoundary";
import LazyPage from "../components/LazyPage";
import { useAuth } from "../hooks/useAuth";

const Login = lazy(() => import("../pages/Login"));
const Dashboard = lazy(() => import("../pages/Dashboard"));
const Campaigns = lazy(() => import("../pages/Campaigns"));
const Users = lazy(() => import("../pages/Users"));
const Profile = lazy(() => import("../pages/Profile"));
const Unauthorized = lazy(() => import("../pages/Unauthorized"));

function RootRedirect() {
  const { token } = useAuth();

  return <Navigate to={token ? "/dashboard" : "/login"} replace />;
}

function AppRoutes() {
  return (
    <BrowserRouter>
      <ErrorBoundary>
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route
            path="/login"
            element={
              <LazyPage>
                <Login />
              </LazyPage>
            }
          />
          <Route
            path="/unauthorized"
            element={
              <LazyPage>
                <Unauthorized />
              </LazyPage>
            }
          />
          <Route element={<ProtectedRoutes />}>
            <Route element={<AppLayout />}>
              <Route
                path="/dashboard"
                element={
                  <LazyPage>
                    <Dashboard />
                  </LazyPage>
                }
              />
              <Route
                path="/campaigns"
                element={
                  <LazyPage>
                    <Campaigns />
                  </LazyPage>
                }
              />
              <Route
                path="/profile"
                element={
                  <LazyPage>
                    <Profile />
                  </LazyPage>
                }
              />
              <Route
                path="/users"
                element={
                  <LazyPage>
                    <Users />
                  </LazyPage>
                }
              />
            </Route>
          </Route>
        </Routes>
      </ErrorBoundary>
    </BrowserRouter>
  );
}

export default AppRoutes;
