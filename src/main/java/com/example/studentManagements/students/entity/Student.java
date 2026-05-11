package com.example.studentManagements.students.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID id;

    /** Mã số sinh viên (MSSV) — duy nhất trong các bản ghi chưa xóa mềm */
    @Column(name = "student_code", length = 32)
    private String studentCode;

    /** Họ và tên (cột legacy: name) */
    @Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 16)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone", columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Column(name = "email", columnDefinition = "NVARCHAR(150)")
    private String email;

    /** Mã lớp hành chính */
    @Column(name = "class_code", columnDefinition = "NVARCHAR(50)")
    private String classCode;

    /** Ngành / chuyên ngành */
    @Column(name = "major", columnDefinition = "NVARCHAR(150)")
    private String major;

    /** Khóa nhập học (năm) */
    @Column(name = "cohort_year")
    private Integer cohortYear;

    @Column(name = "permanent_address", columnDefinition = "NVARCHAR(500)")
    private String permanentAddress;

    /** Đường dẫn công khai ảnh đại diện, ví dụ /uploads/students/avatars/{id}.jpg */
    @Column(name = "avatar_path", columnDefinition = "NVARCHAR(500)")
    private String avatarPath;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at", columnDefinition = "DATETIME2")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
