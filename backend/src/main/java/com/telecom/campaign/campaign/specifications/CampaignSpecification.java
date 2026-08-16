package com.telecom.campaign.campaign.specifications;

import com.telecom.campaign.campaign.entity.Campaign;
import com.telecom.campaign.common.enums.CampaignStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class CampaignSpecification {

    public static Specification<Campaign> hasStatus(CampaignStatus status){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
    }

    public static Specification<Campaign> hasKeyword(String keyword){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%"+keyword.toLowerCase()+"%"
                ),
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%"+keyword.toLowerCase()+"%"
                )
        ));
    }

    public static Specification<Campaign> hasStartDateAfterOrEqual(LocalDateTime startDate){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"),startDate));
    }

    public static Specification<Campaign> hasEndDateBeforeOrEqual(LocalDateTime endDate){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("endDate"),endDate));
    }
}
