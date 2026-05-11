# QLSV — Quản lý sinh viên (Spring Boot)

Ứng dụng web quản lý sinh viên đơn giản: REST API + giao diện admin (Thymeleaf), lưu trữ Microsoft SQL Server, tài liệu API bằng Swagger (springdoc).

## Mô tả ngắn

- **Danh sách / tìm kiếm / phân trang** sinh viên.
- **Thêm — sửa — xóa mềm** (bản ghi vẫn trong DB, không hiển thị trong danh sách hoạt động).
- **MSSV tự động** dạng `SV001`, `SV002`, … khi tạo mới; khi sửa không đổi MSSV.
- **Upload ảnh đại diện** (JPG/PNG/WEBP, giới hạn kích thước trong cấu hình).
- **REST** tại `/api/students`; **giao diện** tại `/admin/students` (gọi REST bằng JavaScript).

## Công nghệ

| Thành phần | Phiên bản / ghi chú |
|-------------|---------------------|
| Java | **21** (theo `pom.xml`) |
| Spring Boot | **3.5.x** |
| SQL Server | JDBC `mssql-jdbc` (kết nối tới instance của bạn) |
| Thymeleaf | Giao diện layout + trang admin sinh viên |
| springdoc-openapi | **2.8.x** (Swagger UI) |
| Lombok | Tuỳ chọn cho entity/DTO |

## Yêu cầu môi trường

**Chạy bằng Maven trên máy (không Docker):** cần JDK 21 và (tuỳ chọn) Maven hoặc `./mvnw`.

**Chạy bằng Docker:** chỉ cần **Docker** + **Docker Compose**; JDK/Maven trên máy là không bắt buộc vì build nằm trong `Dockerfile`.

1. **JDK 21** — cài đặt và kiểm tra:
   ```bash
   java -version
   ```
   Dòng `version` phải là 21 (hoặc tương thích dự án).

2. **Microsoft SQL Server** — chạy được trên máy hoặc Docker, bật TCP (mặc định thường là cổng **1433**).

3. **Maven** *(tuỳ chọn)* — nếu không dùng Maven toàn cục, dự án có **`./mvnw`** (Maven Wrapper).

## Cài đặt chi tiết

### 1. Cơ sở dữ liệu SQL Server

1. Mở **SQL Server Management Studio (SSMS)** hoặc `sqlcmd`.
2. Tạo database (tên mặc định trong project là **`QLSV`**, có thể đổi cho khớp `application.properties`):

   ```sql
   CREATE DATABASE QLSV;
   ```

3. Tạo login / dùng sẵn tài khoản **`sa`** (hoặc user khác có quyền `DDL` + `DML` trên `QLSV`).

4. Ghi nhớ: **máy chủ** (ví dụ `localhost` hoặc `127.0.0.1`), **cổng** (thường `1433`), **tên DB**, **user**, **mật khẩu**.

### 2. Cấu hình ứng dụng

Mở file:

`src/main/resources/application.properties`

Sửa cho đúng môi trường của bạn:

| Thuộc tính | Ý nghĩa |
|------------|---------|
| `spring.datasource.url` | Chuỗi JDBC: host, cổng, `databaseName=...`, `encrypt`, `trustServerCertificate` |
| `spring.datasource.username` | Tài khoản SQL Server |
| `spring.datasource.password` | Mật khẩu |
| `spring.jpa.hibernate.ddl-auto` | `update`: Hibernate tự tạo/cập nhật bảng (phù hợp dev; production nên dùng migration có kiểm soát) |
| `server.port` | Cổng HTTP (mặc định **8080**) |
| `app.upload.base-dir` | Thư mục gốc lưu file upload (mặc định `uploads`) |
| `spring.servlet.multipart.max-file-size` | Giới hạn kích thước file upload |

Ví dụ URL chuẩn (một dòng):

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=QLSV;encrypt=true;trustServerCertificate=true
```

### 3. Tải thư viện (Maven)

Trong thư mục gốc dự án:

```bash
./mvnw dependency:go-offline
```

Hoặc:

```bash
./mvnw -q compile
```

Lần đầu cần **Internet** để tải dependency từ Maven Central.

### 4. Thư mục upload (tự tạo khi chạy)

Ứng dụng sẽ tạo thư mục con (ví dụ `uploads/students/avatars/`) khi có upload. Đảm bảo process Java có **quyền ghi** vào `app.upload.base-dir`.

## Chạy dự án

**Lệnh đúng** (có dấu **gạch ngang** giữa `spring` và `boot`):

```bash
./mvnw spring-boot:run
```

Nếu đã cài Maven hệ thống:

```bash
mvn spring-boot:run
```

> **Lưu ý:** không dùng `springboot:run` — Maven không có goal đó.

Sau khi log có dòng tương tự `Started ...Application`, mở trình duyệt:

| Địa chỉ | Mô tả |
|---------|--------|
| http://localhost:8080/admin | Chuyển hướng tới trang quản lý sinh viên |
| http://localhost:8080/admin/students | Giao diện CRUD sinh viên |
| http://localhost:8080/swagger-ui/index.html | Swagger — thử API REST |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |

Cổng thay đổi theo `server.port` trong `application.properties`.

## Docker và Nginx

Khi chạy bằng Docker, bạn **không cần** cài JDK hay Maven trên máy để build image: `Dockerfile` đã dùng image Maven để đóng gói JAR, rồi chạy bằng JRE trong container.

### Stack gồm những gì

| Thành phần | Vai trò |
|------------|---------|
| **sqlserver** | Microsoft SQL Server 2022, lưu dữ liệu (volume `sqlserver-data`). |
| **app** | Spring Boot, profile `docker`, cổng **8080** chỉ trong mạng nội bộ Compose. |
| **nginx** | Reverse proxy: từ cổng **80** trên máy bạn chuyển tiếp tới `app:8080`. |

File liên quan: `docker-compose.yml`, `Dockerfile`, `docker/nginx/default.conf`, `src/main/resources/application-docker.properties`, script tạo DB `docker/sql/init-qlsv.sql`, mẫu biến môi trường `.env.example`.

### Cổng trên máy bạn (host)

| Cổng host | Dịch vụ | Ghi chú |
|-----------|---------|---------|
| **80** | Nginx | Trình duyệt mở `http://localhost/`. Nếu cổng 80 đã bị chiếm (Apache, IIS, …), sửa trong `docker-compose.yml` dòng `ports` của `nginx` thành ví dụ `"8088:80"` rồi truy cập `http://localhost:8088/`. |
| **14333** | SQL Server | Kết nối từ SSMS/`sqlcmd` trên máy: server `localhost,14333`, user `sa`, mật khẩu trong `.env`. Dùng **14333** thay vì 1433 để **tránh trùng** SQL Server cài sẵn trên Windows/macOS (thường dùng 1433). Trong Docker, ứng dụng vẫn kết nối `sqlserver:1433` — bạn không cần đổi JDBC trong compose. |
| **8080** | Spring Boot | **Không** mở ra host; chỉ Nginx gọi được. |

### Bước 1 — Cài Docker

1. Cài **Docker Desktop** (Windows/macOS) hoặc Docker Engine + plugin Compose (Linux).
2. Bật Docker Desktop và đợi biểu tượng “đang chạy”.
3. Kiểm tra trong terminal:

   ```bash
   docker --version
   docker compose version
   ```

   Cả hai lệnh đều in ra phiên bản là được.

### Bước 2 — Vào thư mục dự án

```bash
cd /đường/dẫn/tới/NguyenNgocMinhHieu-ST23A-QLSV
```

(Mọi lệnh `docker compose` trong README đều giả định bạn đang đứng ở thư mục gốc có file `docker-compose.yml`.)

### Bước 3 — Tạo file `.env` (bắt buộc)

Compose đọc biến `MSSQL_SA_PASSWORD` từ file `.env`. Nếu thiếu, lệnh `docker compose up` sẽ báo lỗi.

```bash
cp .env.example .env
```

Mở file `.env` bằng editor và chỉnh dòng:

```properties
MSSQL_SA_PASSWORD=...
```

**Mật khẩu `sa` phải đủ mạnh** theo quy tắc Microsoft (ví dụ: ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt như `!@#`). File `.env` đã nằm trong `.gitignore` — **không** đưa lên Git.

### Bước 4 — Tạo database `QLSV` (chỉ cần làm khi volume SQL còn trống / lần đầu)

Ứng dụng kết nối tới database tên **`QLSV`**. Bạn có **hai cách**:

#### Cách A — Dùng script có sẵn (khuyến nghị)

Chạy lần lượt:

```bash
# 1) Chỉ khởi động SQL Server
docker compose up -d sqlserver

# 2) Đợi khoảng 10–30 giây (lần đầu có thể lâu hơn) để SQL Server sẵn sàng nhận lệnh

# 3) Copy script vào container
docker compose cp docker/sql/init-qlsv.sql sqlserver:/tmp/init-qlsv.sql

# 4) Chạy script (tạo QLSV nếu chưa có)
docker compose exec sqlserver bash -c '/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -i /tmp/init-qlsv.sql'
```

Nếu bước 4 báo **không tìm thấy** `/opt/mssql-tools18/bin/sqlcmd`, thử đường dẫn cũ:

```bash
docker compose exec sqlserver bash -c '/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -i /tmp/init-qlsv.sql'
```

#### Cách B — Dùng SSMS hoặc Azure Data Studio

1. Kết nối tới server: `localhost,14333` (hoặc `127.0.0.1,14333`).
2. Đăng nhập **SQL Server Authentication**, user `sa`, mật khẩu trùng `.env`.
3. Chạy:

   ```sql
   CREATE DATABASE QLSV;
   ```

### Bước 5 — Build image và chạy cả stack

```bash
docker compose up -d --build
```

- `-d`: chạy nền (detached).
- `--build`: build lại image `app` từ source (cần khi bạn sửa code Java/config).

Lần đầu có thể mất vài phút (tải image SQL Server, Nginx, Maven dependency trong bước build). Service `app` có `restart: on-failure` và Hikari chờ tới **120 giây** khi kết nối DB, nên nếu SQL khởi động chậm, container vẫn có thể tự ổn định sau vài lần thử.

### Bước 6 — Kiểm tra đã chạy

```bash
docker compose ps
```

Cột **STATUS** của `sqlserver`, `app`, `nginx` nên là `Up` hoặc `running`.

Xem log ứng dụng Spring Boot:

```bash
docker compose logs -f app
```

Nhấn `Ctrl+C` để thoát xem log (container vẫn chạy).

Thử HTTP qua Nginx:

```bash
curl -I http://127.0.0.1/
```

Dòng đầu nên gần giống `HTTP/1.1 200`.

### Địa chỉ trình duyệt (qua Nginx, cổng 80)

| Địa chỉ | Mô tả |
|---------|--------|
| http://localhost/ | Trang chủ / điều hướng admin |
| http://localhost/admin/students | Giao diện CRUD sinh viên |
| http://localhost/swagger-ui/index.html | Swagger UI |
| http://localhost/v3/api-docs | OpenAPI JSON |

### Bảng lệnh Docker Compose thường dùng

| Mục đích | Lệnh |
|----------|------|
| Chạy stack (đã build trước đó) | `docker compose up -d` |
| Build lại app và chạy | `docker compose up -d --build` |
| Tắt và xóa container (giữ volume dữ liệu) | `docker compose down` |
| Tắt và xóa cả volume (mất DB + upload trong volume) | `docker compose down -v` |
| Xem container đang chạy | `docker compose ps` |
| Xem log (theo dõi liên tục) | `docker compose logs -f` hoặc `docker compose logs -f app` |
| Xem 100 dòng log cuối | `docker compose logs --tail=100 app` |
| Khởi động lại chỉ ứng dụng | `docker compose restart app` |
| Vào shell trong container app *(debug)* | `docker compose exec app sh` |

Sau khi sửa mã Java, `application-docker.properties`, hoặc `Dockerfile`, hãy chạy lại:

```bash
docker compose up -d --build
```

### Gỡ lỗi Docker (tóm tắt)

- **Ứng dụng `app` restart liên tục / log lỗi kết nối DB:** chắc chắn đã tạo database **`QLSV`** (Bước 4); xem `docker compose logs app`.
- **Lỗi bind cổng 1433 / 80:** trên repo này SQL đã map **14333**; nếu **80** bị chiếm, đổi `nginx` → `"8088:80"` như mục “Cổng trên máy bạn”.
- **Đổi mật khẩu `sa` trong `.env` sau khi đã chạy:** cần đồng bộ với SQL Server trong volume (thường phức tạp); đơn giản nhất: `docker compose down -v` rồi làm lại từ Bước 3 (**mất toàn bộ dữ liệu** trong volume).

### Build file chạy độc lập (tuỳ chọn)

```bash
./mvnw -DskipTests package
java -jar target/schoolmanager.jar
```

Tên file JAR cố định là `schoolmanager.jar` nhờ `<finalName>schoolmanager</finalName>` trong `pom.xml` (khớp với `Dockerfile`).

## Gỡ lỗi nhanh

- **Docker:** xem mục **“Gỡ lỗi Docker (tóm tắt)”** và bảng lệnh trong phần **Docker và Nginx** ở trên.
- **Không kết nối được SQL Server:** kiểm tra SQL Server đang chạy, TCP/IP bật, firewall, `databaseName` đã tạo, user/mật khẩu đúng.
- **Lỗi cổng 8080 đã được dùng:** đổi `server.port` trong `application.properties`.
- **Swagger lỗi phiên bản:** dự án đã ghim `springdoc-openapi` tương thích Spring Boot 3.5 trong `pom.xml`; không hạ `springdoc` xuống bản 2.5.x.

## Cấu trúc mã nguồn (rút gọn)

```
src/main/java/com/example/studentManagements/
├── SchoolmanagerApplication.java      # Điểm vào Spring Boot
├── HomeController.java                # / , /admin
├── config/                            # Web MVC, CORS, xử lý lỗi API
└── students/                          # Module sinh viên (entity, repo, service, REST, trang admin)

src/main/resources/
├── application.properties
├── application-docker.properties      # Cấu hình khi SPRING_PROFILES_ACTIVE=docker
├── admin/students/index.html          # UI CRUD (fetch API)
└── layout/                            # Thymeleaf layout chung

docker/
├── nginx/default.conf                 # Reverse proxy Nginx
└── sql/init-qlsv.sql                  # Tạo database QLSV (chạy trong container)
```

---

*Tài liệu này mô tả trạng thái hiện tại của repo; khi chạy local không Docker, cập nhật `application.properties`; khi chạy Docker, xem `.env` và `docker-compose.yml`.*
