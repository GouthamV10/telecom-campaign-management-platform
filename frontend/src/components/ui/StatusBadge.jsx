import cn from "../../utils/cn";

const STATUS_CONFIG = {
  DRAFT: { classes: "bg-gray-100 text-gray-700 border-gray-200", dot: "bg-gray-400" },
  ACTIVE: { classes: "bg-green-50 text-green-700 border-green-200", dot: "bg-green-500" },
  PAUSED: { classes: "bg-yellow-50 text-yellow-700 border-yellow-200", dot: "bg-yellow-500" },
  COMPLETED: { classes: "bg-blue-50 text-blue-700 border-blue-200", dot: "bg-blue-500" },
  CANCELLED: { classes: "bg-red-50 text-red-700 border-red-200", dot: "bg-red-500" },
  ACTIVE_USER: { classes: "bg-green-50 text-green-700 border-green-200", dot: "bg-green-500" },
  DISABLED: { classes: "bg-gray-100 text-gray-500 border-gray-200", dot: "bg-gray-400" },
};

function StatusBadge({ status, type = "campaign" }) {
  const key = type === "user" && status === "ACTIVE" ? "ACTIVE_USER" : status;
  const config = STATUS_CONFIG[key] || STATUS_CONFIG.DRAFT;

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium",
        config.classes,
      )}
    >
      <span className={cn("h-1.5 w-1.5 rounded-full", config.dot)} />
      {status}
    </span>
  );
}

export default StatusBadge;
