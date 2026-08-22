import { useEffect, useState } from "react";
import { login as loginApi } from "../services/authApi";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import { Button, Input } from "../components/ui";
import { extractErrorMessage } from "../utils/errorHandler";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [authError, setAuthError] = useState("");

  const { token, login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (token) {
      navigate("/dashboard", { replace: true });
    }
  }, [token, navigate]);

  const validate = () => {
    const errs = {};
    if (!email) {
      errs.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errs.email = "Please enter a valid email";
    }
    if (!password) {
      errs.password = "Password is required";
    } else if (password.length < 8) {
      errs.password = "Password must be at least 8 characters";
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setAuthError("");

    if (!validate()) return;

    setSubmitting(true);
    try {
      const response = await loginApi(email, password);
      login(response.data.token);
      navigate("/dashboard");
    } catch (error) {
      setAuthError(extractErrorMessage(error, "Login failed"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 px-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-blue-600 shadow-lg shadow-blue-600/30">
            <svg
              className="h-7 w-7 text-white"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={1.5}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M3 7.5A2.5 2.5 0 016 5h12a2.5 2.5 0 010 5H6a2.5 2.5 0 01-3-2.5zM3 12a2.5 2.5 0 002.5 2.5h9a2.5 2.5 0 000-5h-9A2.5 2.5 0 003 12zM3 16.5a2.5 2.5 0 002.5 2.5h6a2.5 2.5 0 000-5h-6a2.5 2.5 0 00-2.5 2.5z"
              />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-white">
            Campaign Manager
          </h1>
          <p className="mt-1 text-sm text-slate-400">
            Sign in to your account
          </p>
        </div>

        <div className="rounded-xl border border-slate-700 bg-slate-800/50 p-6 shadow-xl backdrop-blur-sm">
          {authError && (
            <div className="mb-4 rounded-lg border border-red-800 bg-red-900/30 px-4 py-3 text-sm text-red-300">
              {authError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Email"
              labelClassName="text-slate-300"
              type="email"
              name="email"
              placeholder="you@example.com"
              required
              value={email}
              error={errors.email}
              onChange={(e) => setEmail(e.target.value)}
              className="bg-slate-900/50 border-slate-600 text-white placeholder:text-slate-500 focus:border-blue-500 focus:ring-blue-500/20"
            />
            <Input
              label="Password"
              labelClassName="text-slate-300"
              type="password"
              name="password"
              placeholder="Enter your password"
              required
              value={password}
              error={errors.password}
              onChange={(e) => setPassword(e.target.value)}
              className="bg-slate-900/50 border-slate-600 text-white placeholder:text-slate-500 focus:border-blue-500 focus:ring-blue-500/20"
            />
            <Button
              type="submit"
              loading={submitting}
              className="w-full"
              size="lg"
            >
              Sign In
            </Button>
          </form>
        </div>

        <p className="mt-6 text-center text-xs text-slate-500">
          Telecom Campaign Management Platform
        </p>
      </div>
    </div>
  );
}

export default Login;