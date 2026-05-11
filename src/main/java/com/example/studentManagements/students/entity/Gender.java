package com.example.studentManagements.students.entity;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    public String getLabelVi() {
        return switch (this) {
            case MALE -> "Nam";
            case FEMALE -> "Nữ";
            case OTHER -> "Khác";
        };
    }
}
