package com.example.studentManagements.students.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class StudentAvatarStorage {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_BYTES = 2 * 1024 * 1024;

    private final Path avatarsDir;

    public StudentAvatarStorage(@Value("${app.upload.base-dir:uploads}") String baseDir) {
        this.avatarsDir = Path.of(baseDir, "students", "avatars").toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, UUID studentId) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Ảnh đại diện tối đa 2MB.");
        }
        String original = file.getOriginalFilename();
        String ext = extension(original);
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Chỉ chấp nhận JPG, PNG hoặc WEBP.");
        }
        Files.createDirectories(avatarsDir);
        String filename = studentId + "." + ext;
        Path target = avatarsDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/students/avatars/" + filename;
    }

    public void deleteIfExists(String publicPath) {
        if (publicPath == null || publicPath.isBlank() || !publicPath.startsWith("/uploads/students/avatars/")) {
            return;
        }
        String filename = publicPath.substring("/uploads/students/avatars/".length());
        Path p = avatarsDir.resolve(filename).normalize();
        if (!p.startsWith(avatarsDir)) {
            return;
        }
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    private static String extension(String original) {
        if (original == null || !original.contains(".")) {
            return null;
        }
        String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return ext.isEmpty() ? null : ext;
    }
}
