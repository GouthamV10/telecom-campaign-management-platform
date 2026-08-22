import { useCallback, useEffect, useState } from "react";
import {
  createCampaign,
  deleteCampaign,
  getCampaigns,
  updateCampaign,
  updateCampaignStatus,
} from "../services/campaignApi";
import { useToast } from "../components/ui/ToastContext";
import { extractErrorMessage } from "../utils/errorHandler";
import { formatDateTime, toLocalDateTimeInput } from "../utils/format";
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
  name: "",
  description: "",
  startDate: "",
  endDate: "",
};

const INITIAL_FILTERS = {
  keyword: "",
  status: "",
  startDate: "",
  endDate: "",
};

const STATUS_TRANSITIONS = {
  DRAFT: ["ACTIVE", "CANCELLED"],
  ACTIVE: ["PAUSED", "COMPLETED", "CANCELLED"],
  PAUSED: ["ACTIVE", "CANCELLED"],
  COMPLETED: [],
  CANCELLED: [],
};

function Campaigns() {
  const toast = useToast();

  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [filters, setFilters] = useState(INITIAL_FILTERS);

  const [showForm, setShowForm] = useState(false);
  const [editingCampaign, setEditingCampaign] = useState(null);
  const [formData, setFormData] = useState(INITIAL_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const [deleteDialog, setDeleteDialog] = useState({
    open: false,
    id: null,
    loading: false,
  });

  const fetchCampaigns = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const response = await getCampaigns({ ...filters, page, size: 10 });
      setCampaigns(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err) {
      setError(extractErrorMessage(err, "Failed to fetch campaigns"));
    } finally {
      setLoading(false);
    }
  }, [filters, page]);

  useEffect(() => {
    fetchCampaigns();
  }, [fetchCampaigns]);

  const validateForm = () => {
    const errs = {};
    if (!formData.name) {
      errs.name = "Campaign name is required";
    } else if (formData.name.length < 3) {
      errs.name = "Name must be at least 3 characters";
    }
    if (!formData.description) {
      errs.description = "Description is required";
    }
    if (!formData.startDate) {
      errs.startDate = "Start date is required";
    }
    if (!formData.endDate) {
      errs.endDate = "End date is required";
    }
    if (formData.startDate && formData.endDate) {
      if (new Date(formData.endDate) < new Date(formData.startDate)) {
        errs.endDate = "End date must be after start date";
      }
    }
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSubmitting(true);
    try {
      if (editingCampaign) {
        await updateCampaign(editingCampaign.id, formData);
        toast.success("Campaign updated successfully");
      } else {
        await createCampaign(formData);
        toast.success("Campaign created successfully");
      }
      setFormData(INITIAL_FORM);
      setFormErrors({});
      setEditingCampaign(null);
      setShowForm(false);
      fetchCampaigns();
    } catch (err) {
      toast.error(extractErrorMessage(err, "Failed to save campaign"));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    setDeleteDialog({ ...deleteDialog, loading: true });
    try {
      await deleteCampaign(deleteDialog.id);
      toast.success("Campaign deleted successfully");
      setDeleteDialog({ open: false, id: null, loading: false });
      fetchCampaigns();
    } catch (err) {
      toast.error(extractErrorMessage(err, "Failed to delete campaign"));
      setDeleteDialog({ ...deleteDialog, loading: false });
    }
  };

  const handleStatusChange = async (id, status) => {
    try {
      await updateCampaignStatus(id, status);
      toast.success(`Campaign status changed to ${status}`);
      fetchCampaigns();
    } catch (err) {
      toast.error(extractErrorMessage(err, "Failed to update status"));
    }
  };

  const handleEdit = (campaign) => {
    setEditingCampaign(campaign);
    setFormData({
      name: campaign.name,
      description: campaign.description,
      startDate: toLocalDateTimeInput(campaign.startDate),
      endDate: toLocalDateTimeInput(campaign.endDate),
    });
    setFormErrors({});
    setShowForm(true);
  };

  const handleCreate = () => {
    setEditingCampaign(null);
    setFormData(INITIAL_FORM);
    setFormErrors({});
    setShowForm(true);
  };

  const handleResetFilters = () => {
    setFilters(INITIAL_FILTERS);
    setPage(0);
  };

  const hasActiveFilters =
    filters.keyword || filters.status || filters.startDate || filters.endDate;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Campaigns</h1>
          <p className="mt-1 text-sm text-gray-500">Manage your campaigns</p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={fetchCampaigns} disabled={loading}>
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99" />
            </svg>
            Refresh
          </Button>
          <Button onClick={handleCreate}>
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            Create Campaign
          </Button>
        </div>
      </div>

      <div className="mb-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Input
            type="text"
            placeholder="Search campaigns..."
            value={filters.keyword}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, keyword: e.target.value });
            }}
          />
          <Select
            value={filters.status}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, status: e.target.value });
            }}
          >
            <option value="">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="PAUSED">Paused</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </Select>
          <Input
            type="datetime-local"
            value={filters.startDate}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, startDate: e.target.value });
            }}
          />
          <Input
            type="datetime-local"
            value={filters.endDate}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, endDate: e.target.value });
            }}
          />
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
            <Button variant="secondary" size="sm" className="mt-4" onClick={fetchCampaigns}>
              Try Again
            </Button>
          </div>
        ) : campaigns.length === 0 ? (
          <EmptyState
            title="No campaigns found"
            message={hasActiveFilters ? "Try adjusting your filters" : "Create your first campaign to get started"}
            action={
              hasActiveFilters ? (
                <Button variant="secondary" size="sm" onClick={handleResetFilters}>
                  Reset Filters
                </Button>
              ) : (
                <Button onClick={handleCreate}>Create Campaign</Button>
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
                      Name
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Manager
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Start Date
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      End Date
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {campaigns.map((campaign) => {
                    const availableStatuses = STATUS_TRANSITIONS[campaign.status] || [];
                    return (
                      <tr key={campaign.id} className="transition-colors hover:bg-gray-50">
                        <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-800">
                          {campaign.name}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-600">
                          {campaign.managerName}
                        </td>
                        <td className="px-6 py-4">
                          {availableStatuses.length === 0 ? (
                            <StatusBadge status={campaign.status} />
                          ) : (
                            <div className="flex items-center gap-2">
                              <StatusBadge status={campaign.status} />
                              <select
                                value=""
                                onChange={(e) => {
                                  if (e.target.value) {
                                    handleStatusChange(campaign.id, e.target.value);
                                    e.target.value = "";
                                  }
                                }}
                                className="rounded-md border border-gray-300 px-2 py-1 text-xs text-gray-600 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-200"
                                defaultValue=""
                              >
                                <option value="" disabled>
                                  Change...
                                </option>
                                {availableStatuses.map((status) => (
                                  <option key={status} value={status}>
                                    {status}
                                  </option>
                                ))}
                              </select>
                            </div>
                          )}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-600">
                          {formatDateTime(campaign.startDate)}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-600">
                          {formatDateTime(campaign.endDate)}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleEdit(campaign)}
                              className="rounded-lg p-1.5 text-gray-500 hover:bg-blue-50 hover:text-blue-600"
                              title="Edit"
                            >
                              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931z" />
                              </svg>
                            </button>
                            <button
                              onClick={() => setDeleteDialog({ open: true, id: campaign.id, loading: false })}
                              className="rounded-lg p-1.5 text-gray-500 hover:bg-red-50 hover:text-red-600"
                              title="Delete"
                            >
                              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
                              </svg>
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
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
        open={showForm}
        onClose={() => {
          setShowForm(false);
          setFormErrors({});
        }}
        title={editingCampaign ? "Edit Campaign" : "Create Campaign"}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Campaign Name"
            name="name"
            required
            placeholder="Enter campaign name"
            value={formData.name}
            error={formErrors.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          />
          <div>
            <label className="mb-1.5 block text-sm font-medium text-gray-700">
              Description
              <span className="ml-0.5 text-red-500">*</span>
            </label>
            <textarea
              name="description"
              required
              placeholder="Enter campaign description"
              value={formData.description}
              rows={4}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className={`w-full rounded-lg border bg-white px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-0 ${
                formErrors.description
                  ? "border-red-300 focus:border-red-500 focus:ring-red-200"
                  : "border-gray-300 focus:border-blue-500 focus:ring-blue-200"
              }`}
            />
            {formErrors.description && (
              <p className="mt-1 text-sm text-red-600">{formErrors.description}</p>
            )}
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Start Date"
              type="datetime-local"
              name="startDate"
              required
              value={formData.startDate}
              error={formErrors.startDate}
              onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
            />
            <Input
              label="End Date"
              type="datetime-local"
              name="endDate"
              required
              value={formData.endDate}
              error={formErrors.endDate}
              onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
            />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowForm(false);
                setFormData(INITIAL_FORM);
                setFormErrors({});
                setEditingCampaign(null);
              }}
            >
              Cancel
            </Button>
            <Button type="submit" loading={submitting}>
              {editingCampaign ? "Update Campaign" : "Create Campaign"}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, id: null, loading: false })}
        onConfirm={handleDelete}
        loading={deleteDialog.loading}
        title="Delete Campaign"
        message="Are you sure you want to delete this campaign? This action cannot be undone."
        confirmText="Delete"
        variant="danger"
      />
    </div>
  );
}

export default Campaigns;
