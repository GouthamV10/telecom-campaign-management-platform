import api from "./api"

export const getCampaigns = async (filters = {}) => {
    const response = await api.get("/api/campaigns", {
        params: {
            keyword: filters.keyword || undefined,
            status: filters.status || undefined,
            startDate: filters.startDate || undefined,
            endDate: filters.endDate || undefined,
            page: filters.page || 0,
            size: filters.size || 10
        }
    });

    return response.data;
};


export const createCampaign = async (campaignData) => {
  const response = await api.post("/api/campaigns", campaignData);

  return response.data;
}

export const updateCampaign = async (id, campaignData) => {
    const response = await api.put(`/api/campaigns/${id}`, campaignData);

    return response.data;
};

export const deleteCampaign = async (id) => {
    const response = await api.delete(`/api/campaigns/${id}`);

    return response.data;
};

export const updateCampaignStatus = async (id, status) => {
    const response = await api.patch(
        `/api/campaigns/${id}/status`,
        {
            status: status
        }
    );

    return response.data;
};