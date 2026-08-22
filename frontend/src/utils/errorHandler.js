const NETWORK_ERROR_MESSAGES = [
  "Network Error",
  "Network request failed",
  "Failed to fetch",
];

export const extractErrorMessage = (error, fallback = "Something went wrong") => {
  if (!error) return fallback;

  if (error.response) {
    const { status, data } = error.response;
    const message = data?.message;

    switch (status) {
      case 400:
        return message || "Invalid request. Please check your input.";
      case 401:
        return message || "Your session has expired. Please log in again.";
      case 403:
        return message || "You do not have permission to perform this action.";
      case 404:
        return message || "The requested resource was not found.";
      case 409:
        return message || "A conflict occurred with the current state.";
      case 500:
        return "An unexpected server error occurred. Please try again later.";
      default:
        return message || `Request failed with status ${status}`;
    }
  }

  if (error.request) {
    const message = error.message || "";
    if (NETWORK_ERROR_MESSAGES.some((m) => message.includes(m))) {
      return "Unable to connect to the server. Please check your connection.";
    }
    return "No response received from the server. Please try again.";
  }

  return error.message || fallback;
};

export const extractValidationErrors = (error) => {
  if (!error?.response?.data?.data) return null;
  const data = error.response.data.data;
  if (typeof data === "object" && !Array.isArray(data)) {
    return data;
  }
  return null;
};
