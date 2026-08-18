import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

function AppLayout(){

  const {role, logout} = useAuth();
  return(
    <div>
      <nav>
        Campaign Manager
        <button onClick={logout}>
          Logout
        </button>
      </nav>

      <aside>
        <NavLink to="/dashboard">Dashboard</NavLink>
        <NavLink to="/campaigns">Campaigns</NavLink>
        {role == "ADMIN" && (<NavLink to="/users">Users</NavLink>)}
      </aside>

      <main>
        <Outlet />
      </main>
    </div>
  )
}

export default AppLayout;