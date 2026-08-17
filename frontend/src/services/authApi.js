import api from "./api";

export const login = async (email, password) => {
  const response = await api.post("/api/auth/login", {
    email,
    password,
  });

  return response.data;
};

export const adminTest = async () => {
    const response = await api.get("/api/users/admin-test");

    return response.data;
};
