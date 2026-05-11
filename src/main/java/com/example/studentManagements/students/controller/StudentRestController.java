package com.example.studentManagements.students.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.studentManagements.students.dto.PageResponse;
import com.example.studentManagements.students.dto.StudentDto;
import com.example.studentManagements.students.dto.StudentListDto;
import com.example.studentManagements.students.entity.Student;
import com.example.studentManagements.students.mapper.StudentMapper;
import com.example.studentManagements.students.service.StudentService;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "REST API quản lý sinh viên (phân trang, tìm kiếm, xóa mềm, upload ảnh)")
public class StudentRestController {

    private static final int MIN_PAGE_SIZE = 5;
    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "studentCode", "fullName", "createdAt", "updatedAt", "cohortYear", "email");

    private final StudentService studentService;
    private final StudentMapper studentMapper;

    @GetMapping
    @Operation(summary = "Danh sách có phân trang và lọc theo từ khóa (LIKE nhiều trường)")
    public PageResponse<StudentListDto> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        int pageSize = Math.clamp(size, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
        String property = ALLOWED_SORT_PROPERTIES.contains(sortBy) ? sortBy : "studentCode";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, property));
        Page<StudentListDto> result = studentService.search(keyword, pageable);
        return PageResponse.of(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết sinh viên (chỉ bản ghi chưa xóa mềm)")
    public ResponseEntity<StudentDto> getById(@PathVariable UUID id) {
        return studentService.findByIdAsDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Tạo mới (JSON). MSSV bỏ qua từ client — server cấp SV001, SV002, …")
    public ResponseEntity<StudentDto> createJson(@Valid @RequestBody StudentDto dto) throws IOException {
        dto.setId(null);
        dto.setStudentCode(null);
        Student saved = studentService.save(dto, null);
        return createdResponse(saved);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo mới (multipart). MSSV do server cấp (SV001, …)")
    public ResponseEntity<StudentDto> createMultipart(
            @Valid @RequestPart("student") StudentDto dto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        dto.setId(null);
        dto.setStudentCode(null);
        Student saved = studentService.save(dto, avatar);
        return createdResponse(saved);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cập nhật toàn phần (JSON, giữ ảnh cũ nếu không gửi file)")
    public StudentDto updateJson(@PathVariable UUID id, @Valid @RequestBody StudentDto dto) throws IOException {
        dto.setId(id);
        Student saved = studentService.save(dto, null);
        return studentMapper.toDto(saved);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật (multipart: part \"student\" = JSON, part \"avatar\" = file tùy chọn)")
    public StudentDto updateMultipart(
            @PathVariable UUID id,
            @Valid @RequestPart("student") StudentDto dto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        dto.setId(id);
        Student saved = studentService.save(dto, avatar);
        return studentMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!studentService.softDeleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<StudentDto> createdResponse(Student saved) {
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/students/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(studentMapper.toDto(saved));
    }
}
