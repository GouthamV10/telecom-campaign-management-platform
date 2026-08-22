import { useCallback, useEffect, useState } from "react";
import {
  createUser,
  getUsers,
  toggleUserEnabled,
} from "../services/userApi";
import { useToast } from "../components/ui/ToastContext";
import { extractErrorMessage } from "../utils/errorHandler";
import { formatDate } from "../utils/format";
import {
  Button,
  Input,
  Select,
  Modal,
  ConfirmDialog,
  StatusBadge,
  EmptyState,
  Pagination,
  TableSkeleton,
} from "../components/ui";

const INITIAL_FORM = {
  username: "",
  email: "",
  password: "",
  role: "",
};

const INITIAL_FILTERS = {
  keyword: "",
  role: "",
  enabled: "",
};

function Users() {
  const toast = useToast();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [filters, setFilters] = useState(INITIAL_FILTERS);

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    userId: null,
    enabled: false,
    loading: false,
  });

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const response = await getUsers({ ...filters, page, size: 10 });
      setUsers(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err) {
      setError(extractErrorMessage(err, "Failed to fetch users"));
    } finally {
      setLoading(false);
    }
  }, [filters, page]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const validateForm = () => {
    const errs = {};
    if (!formData.username) {
      errs.username = "Username is required";
    } else if (formData.username.length < 3) {
      errs.username = "Username must be at least 3 characters";
    }
    if (!formData.email) {
      errs.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errs.email = "Please enter a valid email";
    }
    if (!formData.password) {
      errs.password = "Password is required";
    } else if (formData.password.length < 8) {
      errs.password = "Password must be at least 8 characters";
    }
    if (!formData.role) {
      errs.role = "Role is required";
    }
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSubmitting(true);
    try {
      const response = await createUser(formData);
      toast.success("User created successfully");
      setFormData(INITIAL_FORM);
      setFormErrors({});
      setShowCreateForm(false);
      fetchUsers();
    } catch (err) {
      const message = extractErrorMessage(err, "Failed to create user");
      if (err?.response?.status === 409) {
        setFormErrors({ email: message });
      } else if (err?.response?.status === 400) {
        setFormErrors({ role: message });
      } else {
        toast.error(message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleEnabled = async () => {
    const { userId, enabled } = confirmDialog;
    setConfirmDialog({ ...confirmDialog, loading: true });
    try {
      await toggleUserEnabled(userId, enabled);
      toast.success(`User ${enabled ? "enabled" : "disabled"} successfully`);
      setConfirmDialog({ open: false, userId: null, enabled: false, loading: false });
      fetchUsers();
    } catch (err) {
      toast.error(extractErrorMessage(err, "Failed to update user status"));
      setConfirmDialog({ ...confirmDialog, loading: false });
    }
  };

  const handleResetFilters = () => {
    setFilters(INITIAL_FILTERS);
    setPage(0);
  };

  const hasActiveFilters =
    filters.keyword || filters.role || filters.enabled !== "";

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Users</h1>
          <p className="mt-1 text-sm text-gray-500">Manage system users</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="secondary"
            onClick={fetchUsers}
            disabled={loading}
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99" />
            </svg>
            Refresh
          </Button>
          <Button onClick={() => setShowCreateForm(true)}>
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            Create User
          </Button>
        </div>
      </div>

      <div className="mb-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <Input
            type="text"
            placeholder="Search by username or email..."
            value={filters.keyword}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, keyword: e.target.value });
            }}
          />
          <Select
            value={filters.role}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, role: e.target.value });
            }}
          >
            <option value="">All Roles</option>
            <option value="ADMIN">ADMIN</option>
            <option value="MANAGER">MANAGER</option>
            <option value="USER">USER</option>
          </Select>
          <Select
            value={filters.enabled}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, enabled: e.target.value });
            }}
          >
            <option value="">All Statuses</option>
            <option value="true">Active</option>
            <option value="false">Disabled</option>
          </Select>
        </div>
        {hasActiveFilters && (
          <div className="mt-3 flex justify-end">
            <Button variant="ghost" size="sm" onClick={handleResetFilters}>
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
              Reset Filters
            </Button>
          </div>
        )}
      </div>

      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        {loading ? (
          <TableSkeleton rows={5} cols={6} />
        ) : error ? (
          <div className="px-6 py-16 text-center">
            <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-full bg-red-50">
              <svg className="h-6 w-6 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <p className="text-sm text-gray-600">{error}</p>
            <Button variant="secondary" size="sm" className="mt-4" onClick={fetchUsers}>
              Try Again
            </Button>
          </div>
        ) : users.length === 0 ? (
          <EmptyState
            title="No users found"
            message={hasActiveFilters ? "Try adjusting your filters" : "Create your first user to get started"}
            action={
              hasActiveFilters ? (
                <Button variant="secondary" size="sm" onClick={handleResetFilters}>
                  Reset Filters
                </Button>
              ) : (
                <Button onClick={() => setShowCreateForm(true)}>
                  Create User
                </Button>
              )
            }
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="border-b border-gray-200 bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Username
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Email
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Role
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Created
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {users.map((user) => (
                    <tr key={user.id} className="transition-colors hover:bg-gray-50">
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-800">
                        {user.username}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {user.email}
                      </td>
                      <td className="px-6 py-4">
                        <span className="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-1 text-xs font-medium text-blue-700">
                          {user.role}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <StatusBadge
                          status={user.enabled ? "ACTIVE" : "DISABLED"}
                          type="user"
                        />
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-600">
                        {formatDate(user.createdAt)}
                      </td>
                      <td className="px-6 py-4">
                        {user.role === "ADMIN" ? (
                          <span className="text-xs text-gray-400">Cannot modify</span>
                        ) : (
                          <Button
                            variant={user.enabled ? "danger" : "success"}
                            size="sm"
                            onClick={() =>
                              setConfirmDialog({
                                open: true,
                                userId: user.id,
                                enabled: !user.enabled,
                                loading: false,
                              })
                            }
                          >
                            {user.enabled ? "Disable" : "Enable"}
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination
              page={page}
              totalPages={totalPages}
              onPageChange={setPage}
            />
          </>
        )}
      </div>

      <Modal
        open={showCreateForm}
        onClose={() => {
          setShowCreateForm(false);
          setFormErrors({});
        }}
        title="Create User"
      >
        <form onSubmit={handleCreateUser} className="space-y-4">
          <Input
            label="Username"
            name="username"
            required
            placeholder="Enter username"
            value={formData.username}
            error={formErrors.username}
            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
          />
          <Input
            label="Email"
            type="email"
            name="email"
            required
            placeholder="user@example.com"
            value={formData.email}
            error={formErrors.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
          />
          <Input
            label="Password"
            type="password"
            name="password"
            required
            placeholder="Minimum 8 characters"
            value={formData.password}
            error={formErrors.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          />
          <Select
            label="Role"
            name="role"
            required
            value={formData.role}
            error={formErrors.role}
            onChange={(e) => setFormData({ ...formData, role: e.target.value })}
          >
            <option value="">Select Role</option>
            <option value="MANAGER">MANAGER</option>
            <option value="USER">USER</option>
          </Select>
          <div className="flex justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowCreateForm(false);
                setFormData(INITIAL_FORM);
                setFormErrors({});
              }}
            >
              Cancel
            </Button>
            <Button type="submit" loading={submitting}>
              Create User
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={confirmDialog.open}
        onClose={() => setConfirmDialog({ open: false, userId: null, enabled: false, loading: false })}
        onConfirm={handleToggleEnabled}
        loading={confirmDialog.loading}
        title={confirmDialog.enabled ? "Enable User" : "Disable User"}
        message={
          confirmDialog.enabled
            ? "Are you sure you want to enable this user? They will be able to log in again."
            : "Are you sure you want to disable this user? They will no longer be able to log in."
        }
        confirmText={confirmDialog.enabled ? "Enable" : "Disable"}
        variant={confirmDialog.enabled ? "success" : "danger"}
      />
    </div>
  );
}

export default Users;
