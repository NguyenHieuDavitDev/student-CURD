package com.example.studentManagements.students.dto;

import com.example.studentManagements.students.entity.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentListDto {

    private UUID id;
    private String studentCode;
    private String fullName;
    private Gender gender;
    private String phone;
    private String email;
    private String classCode;
    private String major;
    private Integer cohortYear;
    private String avatarPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
