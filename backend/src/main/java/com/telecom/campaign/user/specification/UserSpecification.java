package com.telecom.campaign.user.specification;

import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + keyword.toLowerCase() + "%"
                ),
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + keyword.toLowerCase() + "%"
                )
        );
    }

    public static Specification<User> hasRole(Role role) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("enabled"), enabled);
    }
}
