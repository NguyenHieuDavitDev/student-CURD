package com.example.studentManagements.students.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.studentManagements.students.dto.StudentDto;
import com.example.studentManagements.students.dto.StudentListDto;
import com.example.studentManagements.students.entity.Student;
import com.example.studentManagements.students.exception.ConflictException;
import com.example.studentManagements.students.exception.NotFoundException;
import com.example.studentManagements.students.mapper.StudentMapper;
import com.example.studentManagements.students.repository.StudentRepository;
import com.example.studentManagements.students.storage.StudentAvatarStorage;
import com.example.studentManagements.students.support.StudentCodeFormatter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final int MAX_SV_SEQUENCE = 999_999;

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final StudentAvatarStorage avatarStorage;

    public Page<StudentListDto> search(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        String param = kw.isEmpty() ? null : kw;
        Page<Student> page = studentRepository.search(param, pageable);
        List<StudentListDto> dtos = page.getContent().stream()
                .map(studentMapper::toListDto)
                .toList();
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    public Optional<StudentDto> findByIdAsDto(UUID id) {
        return studentRepository.findActiveById(id).map(studentMapper::toDto);
    }

    public boolean existsActiveEmailForOther(String email, UUID excludeId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String e = email.trim();
        if (excludeId == null) {
            return studentRepository.existsByDeletedFalseAndEmailIgnoreCase(e);
        }
        return studentRepository.existsByDeletedFalseAndEmailIgnoreCaseAndIdNot(e, excludeId);
    }

    public boolean existsActiveStudentCodeForOther(String studentCode, UUID excludeId) {
        if (studentCode == null || studentCode.isBlank()) {
            return false;
        }
        String c = studentCode.trim();
        if (excludeId == null) {
            return studentRepository.existsByDeletedFalseAndStudentCodeIgnoreCase(c);
        }
        return studentRepository.existsByDeletedFalseAndStudentCodeIgnoreCaseAndIdNot(c, excludeId);
    }

    @Transactional
    public Student save(StudentDto dto, MultipartFile avatarFile) throws IOException {
        Student existing = null;
        if (dto.getId() != null) {
            existing = studentRepository.findById(dto.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sinh viên."));
            if (existing.isDeleted()) {
                throw new NotFoundException("Sinh viên đã bị xóa.");
            }
            dto.setStudentCode(existing.getStudentCode());
        } else {
            dto.setStudentCode(generateNextStudentCode());
        }

        if (!StudentCodeFormatter.matchesPolicy(dto.getStudentCode())) {
            throw new IllegalArgumentException("Mã sinh viên không đúng định dạng SV001, SV002, …");
        }

        assertUniqueActiveConstraints(dto);

        if (dto.getId() == null) {
            Student entity = studentMapper.toNewEntity(dto);
            Student saved = studentRepository.save(entity);
            if (avatarFile != null && !avatarFile.isEmpty()) {
                applyAvatar(saved, avatarFile);
                return studentRepository.save(saved);
            }
            return saved;
        }

        String previousAvatar = existing.getAvatarPath();
        studentMapper.updateEntityFromDto(dto, existing);
        Student saved = studentRepository.save(existing);
        if (avatarFile != null && !avatarFile.isEmpty()) {
            avatarStorage.deleteIfExists(previousAvatar);
            applyAvatar(saved, avatarFile);
            saved = studentRepository.save(saved);
        }
        return saved;
    }

    /**
     * MSSV tiếp theo: max(suffix của SVxxx trong bản ghi chưa xóa mềm) + 1.
     */
    private String generateNextStudentCode() {
        Integer max = studentRepository.findMaxNumericSuffixAfterSvPrefix();
        int next = (max == null ? 0 : max) + 1;
        if (next > MAX_SV_SEQUENCE) {
            throw new IllegalStateException("Đã vượt giới hạn dải mã SV (tối đa " + MAX_SV_SEQUENCE + ").");
        }
        String candidate = StudentCodeFormatter.format(next);
        if (existsActiveStudentCodeForOther(candidate, null)) {
            throw new IllegalStateException("Mã sinh viên tự động bị trùng, vui lòng thử lại.");
        }
        return candidate;
    }

    private void assertUniqueActiveConstraints(StudentDto dto) {
        UUID excludeId = dto.getId();
        String email = dto.getEmail() != null ? dto.getEmail().trim() : "";
        if (!email.isEmpty() && existsActiveEmailForOther(email, excludeId)) {
            throw new ConflictException("email", "Email đã được sinh viên khác sử dụng.");
        }
        String code = dto.getStudentCode() != null ? dto.getStudentCode().trim() : "";
        if (!code.isEmpty() && existsActiveStudentCodeForOther(code, excludeId)) {
            throw new ConflictException("studentCode", "Mã số sinh viên đã tồn tại.");
        }
    }

    private void applyAvatar(Student saved, MultipartFile avatarFile) throws IOException {
        String path = avatarStorage.store(avatarFile, saved.getId());
        saved.setAvatarPath(path);
    }

    /**
     * @return {@code true} nếu tồn tại bản ghi với id (kể cả đã xóa mềm trước đó)
     */
    @Transactional
    public boolean softDeleteById(UUID id) {
        return studentRepository.findById(id).map(s -> {
            if (!s.isDeleted()) {
                s.setDeleted(true);
                s.setDeletedAt(LocalDateTime.now());
                studentRepository.save(s);
            }
            return true;
        }).orElse(false);
    }
}
