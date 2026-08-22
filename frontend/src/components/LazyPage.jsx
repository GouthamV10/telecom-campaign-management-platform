import { Suspense } from "react";
import LoadingSpinner from "./ui/LoadingSpinner";

export default function LazyPage({ children }) {
  return (
    <Suspense
      fallback={
        <div className="flex h-96 items-center justify-center">
          <LoadingSpinner size="lg" />
        </div>
      }
    >
      {children}
    </Suspense>
  );
}
