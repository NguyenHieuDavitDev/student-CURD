package com.example.studentManagements.students.mapper;

import org.springframework.stereotype.Component;

import com.example.studentManagements.students.dto.StudentDto;
import com.example.studentManagements.students.dto.StudentListDto;
import com.example.studentManagements.students.entity.Student;

@Component
public class StudentMapper {

    public Student toNewEntity(StudentDto dto) {
        if (dto == null) {
            return null;
        }
        return Student.builder()
                .studentCode(trim(dto.getStudentCode()))
                .fullName(trim(dto.getFullName()))
                .gender(dto.getGender())
                .dateOfBirth(dto.getDateOfBirth())
                .phone(blankToNull(dto.getPhone()))
                .email(blankToNull(dto.getEmail()))
                .classCode(blankToNull(dto.getClassCode()))
                .major(blankToNull(dto.getMajor()))
                .cohortYear(dto.getCohortYear())
                .permanentAddress(blankToNull(dto.getPermanentAddress()))
                .deleted(false)
                .deletedAt(null)
                .build();
    }

    public void updateEntityFromDto(StudentDto dto, Student entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setStudentCode(trim(dto.getStudentCode()));
        entity.setFullName(trim(dto.getFullName()));
        entity.setGender(dto.getGender());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setPhone(blankToNull(dto.getPhone()));
        entity.setEmail(blankToNull(dto.getEmail()));
        entity.setClassCode(blankToNull(dto.getClassCode()));
        entity.setMajor(blankToNull(dto.getMajor()));
        entity.setCohortYear(dto.getCohortYear());
        entity.setPermanentAddress(blankToNull(dto.getPermanentAddress()));
    }

    public StudentDto toDto(Student entity) {
        if (entity == null) {
            return null;
        }
        return StudentDto.builder()
                .id(entity.getId())
                .studentCode(entity.getStudentCode())
                .fullName(entity.getFullName())
                .gender(entity.getGender())
                .dateOfBirth(entity.getDateOfBirth())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .classCode(entity.getClassCode())
                .major(entity.getMajor())
                .cohortYear(entity.getCohortYear())
                .permanentAddress(entity.getPermanentAddress())
                .avatarPath(entity.getAvatarPath())
                .build();
    }

    public StudentListDto toListDto(Student entity) {
        if (entity == null) {
            return null;
        }
        return StudentListDto.builder()
                .id(entity.getId())
                .studentCode(entity.getStudentCode())
                .fullName(entity.getFullName())
                .gender(entity.getGender())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .classCode(entity.getClassCode())
                .major(entity.getMajor())
                .cohortYear(entity.getCohortYear())
                .avatarPath(entity.getAvatarPath())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
