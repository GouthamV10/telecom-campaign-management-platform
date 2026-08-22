import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { getDashboardStats } from "../services/dashboardApi";
import { getCampaigns } from "../services/campaignApi";
import { extractErrorMessage } from "../utils/errorHandler";
import { formatDateTime } from "../utils/format";
import {
  CardSkeleton,
  StatusBadge,
  EmptyState,
  TableSkeleton,
} from "../components/ui";

const STAT_CARDS = [
  { key: "totalCampaigns", label: "Total Campaigns", color: "text-gray-800", bg: "bg-gray-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 7.5A2.5 2.5 0 016 5h12a2.5 2.5 0 010 5H6a2.5 2.5 0 01-3-2.5zM3 12a2.5 2.5 0 002.5 2.5h9a2.5 2.5 0 000-5h-9A2.5 2.5 0 003 12zM3 16.5a2.5 2.5 0 002.5 2.5h6a2.5 2.5 0 000-5h-6a2.5 2.5 0 00-2.5 2.5z" />
    </svg>
  )},
  { key: "activeCampaigns", label: "Active", color: "text-green-600", bg: "bg-green-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )},
  { key: "draftCampaigns", label: "Draft", color: "text-gray-600", bg: "bg-gray-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
    </svg>
  )},
  { key: "pausedCampaigns", label: "Paused", color: "text-yellow-600", bg: "bg-yellow-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M14.25 9v6m-4.5 0V9M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )},
  { key: "completedCampaigns", label: "Completed", color: "text-blue-600", bg: "bg-blue-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  )},
  { key: "cancelledCampaigns", label: "Cancelled", color: "text-red-600", bg: "bg-red-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
    </svg>
  )},
  { key: "totalUsers", label: "Total Users", color: "text-gray-800", bg: "bg-gray-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.389-.234m0 0A11.255 11.255 0 012 18.575a11.261 11.261 0 015.291-4.482m5.291 0a4.125 4.125 0 00-7.533-2.493M9.75 7.5a4.125 4.125 0 108.25 0 4.125 4.125 0 00-8.25 0z" />
    </svg>
  )},
  { key: "activeUsers", label: "Active Users", color: "text-green-600", bg: "bg-green-50", icon: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
    </svg>
  )},
];

function Dashboard() {
  const { role } = useAuth();

  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);
  const [statsError, setStatsError] = useState("");

  const [recentCampaigns, setRecentCampaigns] = useState([]);
  const [campaignsLoading, setCampaignsLoading] = useState(true);

  const fetchStats = useCallback(async () => {
    setStatsLoading(true);
    try {
      const response = await getDashboardStats();
      setStats(response.data);
      setStatsError("");
    } catch (err) {
      setStatsError(extractErrorMessage(err, "Failed to load statistics"));
    } finally {
      setStatsLoading(false);
    }
  }, []);

  const fetchRecentCampaigns = useCallback(async () => {
    setCampaignsLoading(true);
    try {
      const response = await getCampaigns({ page: 0, size: 5 });
      setRecentCampaigns(response.data.content);
    } catch {
      setRecentCampaigns([]);
    } finally {
      setCampaignsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
    fetchRecentCampaigns();
  }, [fetchStats, fetchRecentCampaigns]);

  const roleLabel = role ? role.charAt(0) + role.slice(1).toLowerCase() : "User";

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
        <p className="mt-1 text-sm text-gray-500">Welcome back, {roleLabel}</p>
      </div>

      {statsLoading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <CardSkeleton key={i} />
          ))}
        </div>
      ) : statsError ? (
        <div className="mb-6 rounded-xl border border-red-200 bg-red-50 px-6 py-4 text-sm text-red-700">
          {statsError}
        </div>
      ) : stats ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {STAT_CARDS.map((card) => (
            <div
              key={card.key}
              className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-gray-500">{card.label}</p>
                <div className={`flex h-9 w-9 items-center justify-center rounded-lg ${card.bg} ${card.color}`}>
                  {card.icon}
                </div>
              </div>
              <h2 className="mt-3 text-3xl font-bold text-gray-800">
                {stats[card.key]}
              </h2>
            </div>
          ))}
        </div>
      ) : null}

      <div className="mt-6 rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="border-b border-gray-200 px-6 py-4">
          <h2 className="text-lg font-semibold text-gray-800">Recent Campaigns</h2>
        </div>

        {campaignsLoading ? (
          <TableSkeleton rows={5} cols={4} />
        ) : recentCampaigns.length === 0 ? (
          <EmptyState
            title="No campaigns yet"
            message="Campaigns will appear here once created"
          />
        ) : (
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
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {recentCampaigns.map((campaign) => (
                  <tr key={campaign.id} className="transition-colors hover:bg-gray-50">
                    <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-800">
                      {campaign.name}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {campaign.managerName}
                    </td>
                    <td className="px-6 py-4">
                      <StatusBadge status={campaign.status} />
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-600">
                      {formatDateTime(campaign.startDate)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default Dashboard;
