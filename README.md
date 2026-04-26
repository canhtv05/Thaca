<div align="center">
  <a href="README.md"><b>README</b></a> | 
  <a href="THACA_FRAMEWORK_GUIDE.md"><b>FRAMEWORK GUIDE</b></a>
</div>

# 🌟 Thaca Project - Hệ Sinh Thái SaaS & Multi-Tenant Hiện Đại

Chào mừng bạn đến với **Thaca**, một nền tảng SaaS (Software as a Service) mạnh mẽ được xây dựng trên kiến trúc Multi-Tenant, tập trung vào tính bảo mật, khả năng mở rộng và hiệu năng cao cấp.

---

## 🏗️ Kiến Trúc Hệ Thống (Module)

Dự án được tổ chức theo cấu trúc multi-module chuyên sâu:

- 📂 **`thaca-framework`**: Nhân lõi (Core Engine) cung cấp các giải pháp AOP Security, Dynamic Tenant Filtering, Auditing và Centralized Exception Handling.
- 📂 **`thaca-auth`**: Dịch vụ Identity & Access Management (IAM) trung tâm, xử lý xác thực đa kênh và quản lý Tenant/Role/Permission.
- 📂 **`thaca-cms`**: Backend dành riêng cho quản trị viên, tích hợp hệ thống handler tự động (FwModeRegistry) để tương tác với các microservices khác.
- 📂 **`thaca-gateway`**: Cửa ngõ duy nhất của hệ thống, xử lý định tuyến, bảo mật biên và điều phối request.
- 📂 **`thaca-web-cms`**: Giao diện quản trị Single Page Application (SPA) hiện đại, flicker-free.
- 📂 **`thaca-common-object`**: Thư viện chứa các DTO và Domain Object dùng chung để đảm bảo tính nhất quán dữ liệu.

---

## 🚀 Tính Năng Nổi Bật

- **Multi-Tenant SaaS**: Hỗ trợ nhiều khách hàng (Tenant) trên cùng một database, phân tách dữ liệu hoàn toàn bằng Hibernate Filters tự động.
- **Dynamic API Dispatching**: Giảm thiểu boilerplate code bằng cách sử dụng `FwApiProcess` để điều phối logic nghiệp vụ thông qua metadata.
- **Comprehensive Audit**: Theo dõi mọi thay đổi dữ liệu (`created_at`, `updated_at`, `created_by`, `updated_by`) và lịch sử đăng nhập chi tiết.
- **Stateless Security**: Bảo mật dựa trên JWT với cơ chế quản lý session thông minh qua Redis, hỗ trợ Force Logout và Token Revocation.
- **Automated Migration**: Sử dụng Liquibase để quản lý phiên bản database đồng bộ trên mọi môi trường.

---

## 💻 Công Nghệ Sử Dụng

- **Backend**: Java 24+, Spring Boot 4.0+, Hibernate/JPA.
- **Database**: PostgreSQL 16+, Redis (Caching & Session).
- **Security**: Spring Security, JWT, BCrypt, AOP-based Permission Control.
- **Monitoring**: Spring Boot Actuator, Micrometer.
- **Migration**: Liquibase.

---

## 🔐 Thông Tin Tài Khoản Thử Nghiệm

Hệ thống được thiết lập theo phân cấp: **SuperAdmin -> Tenant -> TenantAdmin -> User**.
Mật khẩu mặc định cho tất cả tài khoản: `Thaca@2026`

### 1. Quản Trị Hệ Thống (SuperAdmin)

Dùng để quản lý các Tenant và cấu hình toàn cục.

- **Username**: `superadmin`

### 2. Quản Trị Tenant (TenantAdmin)

Quản lý người dùng và dữ liệu trong phạm vi một Tenant (ví dụ: Apple Tenant).

- **Username**: `admin` (Tenant: Apple)

### 3. Người Dùng Cuối (User)

- **Username**: `user`

---

## 🛠️ Hướng Dẫn Chạy Hệ Thống

1. **Yêu cầu**: JDK 24, PostgreSQL, Redis.
2. **Khởi động Database**: Liquibase sẽ tự động tạo schema `auth` và các bảng cần thiết khi chạy `thaca-auth`.
3. **Thứ tự khởi động**:
   - `thaca-auth` (Cung cấp API xác thực & Tenant).
   - `thaca-cms` (Giao diện API quản trị).
   - `thaca-gateway` (Routing).

---

## 📂 Tài Liệu API

- **Swagger UI**: Truy cập tại `http://localhost:1001/swagger-ui.html`
- **Postman**: Import file `Thaca.postman_collection.json` tại thư mục gốc.

---

_Phát triển bởi đội ngũ Thaca - 2026_
