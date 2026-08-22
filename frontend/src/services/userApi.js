import api from "./api";

export const createUser = async (userData) => {
    const response = await api.post("/api/users", userData);
    return response.data;
};

export const getUsers = async (filters = {}) => {
    const response = await api.get("/api/users", {
        params: {
            keyword: filters.keyword || undefined,
            role: filters.role || undefined,
            enabled: filters.enabled !== undefined && filters.enabled !== "" ? filters.enabled : undefined,
            page: filters.page || 0,
            size: filters.size || 10,
        },
    });
    return response.data;
};

export const getCurrentUser = async () => {
    const response = await api.get("/api/users/me");
    return response.data;
};

export const toggleUserEnabled = async (id, enabled) => {
    const response = await api.patch(`/api/users/${id}/enabled`, null, {
        params: { enabled },
    });
    return response.data;
};

export const changePassword = async (id, currentPassword, newPassword) => {
    const response = await api.put(`/api/users/${id}/password`, {
        currentPassword,
        newPassword,
    });
    return response.data;
};