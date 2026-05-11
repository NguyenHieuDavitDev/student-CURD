package com.example.studentManagements.students.support;

/**
 * Định dạng MSSV tự động: SV + số thứ tự tối thiểu 3 chữ số (SV001, SV002, …, SV1000).
 */
public final class StudentCodeFormatter {

    private static final String PREFIX = "SV";

    private StudentCodeFormatter() {
    }

    public static String format(int sequence) {
        if (sequence < 1) {
            sequence = 1;
        }
        return PREFIX + String.format("%03d", sequence);
    }

    public static boolean matchesPolicy(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return code.trim().matches("^SV\\d{3,}$");
    }
}
