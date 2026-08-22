import { Link } from "react-router-dom";
import Button from "../components/ui/Button";

function Unauthorized() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 px-4">
      <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-red-900/30">
        <svg
          className="h-10 w-10 text-red-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={1.5}
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      </div>
      <h1 className="text-2xl font-bold text-white">Access Denied</h1>
      <p className="mt-2 text-sm text-slate-400">
        You do not have permission to access this page.
      </p>
      <Link to="/dashboard" className="mt-6">
        <Button variant="primary">Back to Dashboard</Button>
      </Link>
    </div>
  );
}

export default Unauthorized;
