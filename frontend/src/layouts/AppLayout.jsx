import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

function AppLayout() {
  const { role, logout } = useAuth();
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Top Navbar */}
      <nav className="h-16 bg-white border-b flex items-center justify-between px-6">
        <h1 className="text-xl font-bold text-gray-800">Campaign Manager</h1>

        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-600">{role}</span>
          <button
            onClick={logout}
            className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700"
          >
            Logout
          </button>
        </div>
      </nav>

      {/* Sidebar */}
      <div className="flex">
        <aside className="w-64 min-h-[calc(100vh-4rem)] bg-white border-r p-4">
          <div className="flex flex-col gap-2">
            <NavLink
              to="/dashboard"
              className={({ isActive }) =>
                `px-4 py-3 rounded-log ${isActive ? "bg-blue-600 text-white" : "text-gray-700 hover:bg-gray-100"}`
              }
            >
              Dashboard
            </NavLink>
            <NavLink
              to="/campaigns"
              className={({ isActive }) =>
                `px-4 py-3 rounded-log ${isActive ? "bg-blue-600 text-white" : "text-gray-700 hover:bg-gray-100"}`
              }
            >
              Campaigns
            </NavLink>
            {role == "ADMIN" && (
              <NavLink
                to="/users"
                className={({ isActive }) =>
                  `px-4 py-3 rounded-log ${isActive ? "bg-blue-600 text-white" : "text-gray-700 hover:bg-gray-100"}`
                }
              >
                Users
              </NavLink>
            )}
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
