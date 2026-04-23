package com.settlement.manager.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    ROLE_CREATOR,
    ROLE_OPERATOR,
    ROLE_ADMIN;

    @JsonCreator
    public static Role from(String value) {
        if (value.startsWith("ROLE_")) {
            return Role.valueOf(value);
        }
        return Role.valueOf("ROLE_" + value);
    }
}