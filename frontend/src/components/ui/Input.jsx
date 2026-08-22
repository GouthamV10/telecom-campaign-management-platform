import { forwardRef } from "react";
import cn from "../../utils/cn";

const Input = forwardRef(function Input(
  {
    label,
    labelClassName = "",
    error,
    required = false,
    className = "",
    id,
    hint,
    ...props
  },
  ref,
) {
  const inputId = id || props.name;

  return (
    <div className="w-full">
      {label && (
        <label
          htmlFor={inputId}
          className={cn(
            "mb-1.5 block text-sm font-medium text-gray-700",
            labelClassName,
          )}
        >
          {label}
          {required && <span className="ml-0.5 text-red-500">*</span>}
        </label>
      )}
      <input
        ref={ref}
        id={inputId}
        className={cn(
          "w-full rounded-lg border bg-white px-3 py-2.5 text-sm text-gray-900",
          "placeholder:text-gray-400 transition-colors",
          "focus:outline-none focus:ring-2 focus:ring-offset-0",
          error
            ? "border-red-300 focus:border-red-500 focus:ring-red-200"
            : "border-gray-300 focus:border-blue-500 focus:ring-blue-200",
          "disabled:cursor-not-allowed disabled:bg-gray-50 disabled:opacity-60",
          className,
        )}
        {...props}
      />
      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
      {hint && !error && <p className="mt-1 text-sm text-gray-500">{hint}</p>}
    </div>
  );
});

export default Input;
