import { createContext, useCallback, useContext, useState } from "react";
import cn from "../../utils/cn";

const ToastContext = createContext(null);

export function useToast() {
  return useContext(ToastContext);
}

const TOAST_STYLES = {
  success: {
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
      </svg>
    ),
    classes: "bg-green-50 border-green-200 text-green-800",
    iconClasses: "text-green-500",
  },
  error: {
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    classes: "bg-red-50 border-red-200 text-red-800",
    iconClasses: "text-red-500",
  },
  warning: {
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M12 2a9 9 0 00-9 9 9 9 0 009 9 9 9 0 009-9 9 9 0 00-9-9z" />
      </svg>
    ),
    classes: "bg-yellow-50 border-yellow-200 text-yellow-800",
    iconClasses: "text-yellow-500",
  },
  info: {
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    classes: "bg-blue-50 border-blue-200 text-blue-800",
    iconClasses: "text-blue-500",
  },
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    (message, type = "info", duration = 4000) => {
      const id = Date.now() + Math.random();
      setToasts((prev) => [...prev, { id, message, type }]);
      if (duration > 0) {
        setTimeout(() => removeToast(id), duration);
      }
    },
    [removeToast],
  );

  const toast = {
    success: (msg, duration) => addToast(msg, "success", duration),
    error: (msg, duration) => addToast(msg, "error", duration),
    warning: (msg, duration) => addToast(msg, "warning", duration),
    info: (msg, duration) => addToast(msg, "info", duration),
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2">
        {toasts.map((t) => {
          const style = TOAST_STYLES[t.type] || TOAST_STYLES.info;
          return (
            <div
              key={t.id}
              className={cn(
                "flex items-start gap-3 rounded-lg border px-4 py-3 shadow-lg",
                "min-w-[300px] max-w-[400px] animate-in slide-in-from-right",
                style.classes,
              )}
            >
              <span className={cn("flex-shrink-0", style.iconClasses)}>
                {style.icon}
              </span>
              <p className="flex-1 text-sm font-medium">{t.message}</p>
              <button
                onClick={() => removeToast(t.id)}
                className="flex-shrink-0 text-current opacity-60 hover:opacity-100"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}
