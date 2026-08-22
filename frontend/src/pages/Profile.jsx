import { useEffect, useState } from "react";
import { getCurrentUser, changePassword } from "../services/userApi";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../components/ui/ToastContext";
import { extractErrorMessage } from "../utils/errorHandler";
import { formatDate } from "../utils/format";
import { Button, Input, LoadingSpinner, StatusBadge } from "../components/ui";

function Profile() {
  const { token, role, logout } = useAuth();
  const toast = useToast();

  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [passwordErrors, setPasswordErrors] = useState({});
  const [changingPassword, setChangingPassword] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const response = await getCurrentUser();
        setUser(response.data);
      } catch (error) {
        toast.error(extractErrorMessage(error, "Failed to load profile"));
      } finally {
        setLoading(false);
      }
    };
    fetchUser();
  }, []);

  const validatePasswordForm = () => {
    const errors = {};

    if (!passwordForm.currentPassword) {
      errors.currentPassword = "Current password is required";
    }

    if (!passwordForm.newPassword) {
      errors.newPassword = "New password is required";
    } else if (passwordForm.newPassword.length < 8) {
      errors.newPassword = "Password must be at least 8 characters";
    }

    if (!passwordForm.confirmPassword) {
      errors.confirmPassword = "Please confirm your new password";
    } else if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      errors.confirmPassword = "Passwords do not match";
    }

    setPasswordErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();

    if (!validatePasswordForm()) return;

    setChangingPassword(true);
    try {
      await changePassword(user.id, passwordForm.currentPassword, passwordForm.newPassword);
      toast.success("Password changed successfully");
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setPasswordErrors({});
    } catch (error) {
      const message = extractErrorMessage(error, "Failed to change password");
      if (error?.response?.status === 403 && message.includes("current password")) {
        setPasswordErrors({ currentPassword: message });
      } else if (error?.response?.status === 400) {
        setPasswordErrors({ newPassword: message });
      } else {
        toast.error(message);
      }
    } finally {
      setChangingPassword(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!user) {
    return (
      <div className="flex h-96 items-center justify-center text-gray-500">
        Unable to load profile.
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Profile</h1>
        <p className="mt-1 text-sm text-gray-500">
          View your account information and change your password
        </p>
      </div>

      <div className="mb-6 rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-gray-800">
          Account Information
        </h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <p className="text-xs font-medium uppercase text-gray-500">Username</p>
            <p className="mt-1 text-sm font-medium text-gray-800">{user.username}</p>
          </div>
          <div>
            <p className="text-xs font-medium uppercase text-gray-500">Email</p>
            <p className="mt-1 text-sm font-medium text-gray-800">{user.email}</p>
          </div>
          <div>
            <p className="text-xs font-medium uppercase text-gray-500">Role</p>
            <p className="mt-1">
              <span className="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-1 text-xs font-medium text-blue-700">
                {user.role}
              </span>
            </p>
          </div>
          <div>
            <p className="text-xs font-medium uppercase text-gray-500">Status</p>
            <p className="mt-1">
              <StatusBadge
                status={user.enabled ? "ACTIVE" : "DISABLED"}
                type="user"
              />
            </p>
          </div>
          <div>
            <p className="text-xs font-medium uppercase text-gray-500">Created</p>
            <p className="mt-1 text-sm text-gray-600">{formatDate(user.createdAt)}</p>
          </div>
          <div>
            <p className="text-xs font-medium uppercase text-gray-500">Last Updated</p>
            <p className="mt-1 text-sm text-gray-600">{formatDate(user.updatedAt)}</p>
          </div>
        </div>
      </div>

      <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-gray-800">Change Password</h2>
        <form onSubmit={handleChangePassword} className="space-y-4">
          <Input
            label="Current Password"
            type="password"
            name="currentPassword"
            required
            value={passwordForm.currentPassword}
            error={passwordErrors.currentPassword}
            onChange={(e) =>
              setPasswordForm({ ...passwordForm, currentPassword: e.target.value })
            }
          />
          <Input
            label="New Password"
            type="password"
            name="newPassword"
            required
            value={passwordForm.newPassword}
            error={passwordErrors.newPassword}
            hint="Minimum 8 characters"
            onChange={(e) =>
              setPasswordForm({ ...passwordForm, newPassword: e.target.value })
            }
          />
          <Input
            label="Confirm New Password"
            type="password"
            name="confirmPassword"
            required
            value={passwordForm.confirmPassword}
            error={passwordErrors.confirmPassword}
            onChange={(e) =>
              setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })
            }
          />
          <div className="flex gap-3">
            <Button type="submit" loading={changingPassword}>
              Change Password
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setPasswordForm({
                  currentPassword: "",
                  newPassword: "",
                  confirmPassword: "",
                });
                setPasswordErrors({});
              }}
            >
              Clear
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Profile;
