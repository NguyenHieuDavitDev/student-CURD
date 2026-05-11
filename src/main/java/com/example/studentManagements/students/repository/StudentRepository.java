package com.example.studentManagements.students.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.studentManagements.students.entity.Student;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    @Query("""
           SELECT s FROM Student s
           WHERE s.deleted = false AND s.id = :id
           """)
    Optional<Student> findActiveById(@Param("id") UUID id);

    @Query("""
           SELECT s FROM Student s
           WHERE s.deleted = false
             AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(s.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(s.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(s.classCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(s.major, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(s.permanentAddress, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
             )
           """)
    Page<Student> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByDeletedFalseAndEmailIgnoreCase(String email);

    boolean existsByDeletedFalseAndStudentCodeIgnoreCase(String studentCode);

    boolean existsByDeletedFalseAndEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByDeletedFalseAndStudentCodeIgnoreCaseAndIdNot(String studentCode, UUID id);

    /**
     * Suffix số lớn nhất sau tiền tố SV (chỉ bản ghi chưa xóa mềm), phục vụ cấp SV001, SV002, …
     */
    @Query(value = """
            SELECT MAX(TRY_CAST(SUBSTRING(student_code, 3, 50) AS INT))
            FROM students
            WHERE deleted = 0
              AND student_code LIKE 'SV%'
              AND LEN(student_code) >= 4
              AND TRY_CAST(SUBSTRING(student_code, 3, 50) AS INT) IS NOT NULL
            """, nativeQuery = true)
    Integer findMaxNumericSuffixAfterSvPrefix();
}
