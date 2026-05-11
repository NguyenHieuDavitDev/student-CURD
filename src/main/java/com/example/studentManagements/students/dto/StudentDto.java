package com.example.studentManagements.students.dto;

import com.example.studentManagements.students.entity.Gender;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentDto {

    private UUID id;

    /**
     * Khi tạo mới: client có thể bỏ trống — server gán SV001, SV002, …
     * Khi cập nhật: phải trùng MSSV hiện có (server ghi đè từ DB, không cho đổi).
     */
    @Size(max = 16, message = "MSSV tối đa 16 ký tự")
    private String studentCode;

    @NotBlank(message = "Họ và tên không được trống")
    @Size(min = 2, max = 150, message = "Họ tên từ 2 đến 150 ký tự")
    private String fullName;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Gender gender;

    @NotNull(message = "Ngày sinh không được trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Pattern(regexp = "^$|^0[0-9]{9,10}$", message = "Số điện thoại phải là 10–11 số, bắt đầu bằng 0 (VD: 0912345678)")
    private String phone;

    @Size(max = 150, message = "Email tối đa 150 ký tự")
    @Pattern(
            regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email không hợp lệ"
    )
    private String email;

    @Size(max = 50, message = "Mã lớp tối đa 50 ký tự")
    @Pattern(regexp = "^$|^[A-Za-z0-9._-]{1,50}$", message = "Mã lớp chỉ gồm chữ, số và . _ - (tối đa 50 ký tự)")
    private String classCode;

    @Size(max = 150, message = "Ngành học tối đa 150 ký tự")
    private String major;

    @NotNull(message = "Khóa (năm nhập học) không được trống")
    @Min(value = 1990, message = "Khóa phải từ năm 1990")
    @Max(value = 2100, message = "Khóa không vượt quá 2100")
    private Integer cohortYear;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String permanentAddress;

    /** Đường dẫn ảnh (chỉ đọc từ server) */
    private String avatarPath;
}
