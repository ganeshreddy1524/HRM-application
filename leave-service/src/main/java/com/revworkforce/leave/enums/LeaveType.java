package com.revworkforce.leave.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeaveType {
    SICK,
    CASUAL,
    PAID;

    @JsonCreator
    public static LeaveType from(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        if (normalized.isEmpty()) return null;
        return LeaveType.valueOf(normalized);
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
