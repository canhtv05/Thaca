<div align="center">
  <a href="README.md"><b>README</b></a> | 
  <a href="thaca-framework/README.md"><b>FRAMEWORK GUIDE</b></a>
</div>

# 🌟 Thaca Project — Modern SaaS & Multi-Tenant Ecosystem

Welcome to **Thaca**, a powerful SaaS (Software as a Service) platform built on a Multi-Tenant architecture, focused on security, scalability, and high-grade performance.

---

## 🏗️ System Architecture (Modules)

The project is organized into a deep multi-module structure:

- 📂 **`thaca-framework`**: The Core Engine providing AOP Security, Dynamic Tenant Filtering, Auditing, and Centralized Exception Handling.
- 📂 **`thaca-auth`**: Central Identity & Access Management (IAM) service handling multi-channel authentication and Tenant/Role/Permission management.
- 📂 **`thaca-cms`**: Admin-facing backend, integrated with an automatic handler system (`FwModeRegistry`) for interacting with other microservices.
- 📂 **`thaca-gateway`**: The system's single entry point, handling routing, edge security, and request orchestration.
- 📂 **`thaca-web-cms`**: A modern, flicker-free Single Page Application (SPA) admin interface.
- 📂 **`thaca-common-object`**: Shared library containing DTOs and Domain Objects to ensure data consistency across services.

---

## 🚀 Key Features

- **Multi-Tenant SaaS**: Supports multiple customers (Tenants) on a single database with full data isolation via automatic Hibernate Filters.
- **Dynamic API Dispatching**: Minimizes boilerplate code by using `FwApiProcess` to dispatch business logic through metadata-driven routing.
- **Comprehensive Audit**: Tracks all data changes (`created_at`, `updated_at`, `created_by`, `updated_by`) and detailed login history.
- **Stateless Security**: JWT-based security with intelligent session management via Redis, supporting Force Logout and Token Revocation.
- **Automated Migration**: Uses Liquibase for synchronized database versioning across all environments.

---

## 💻 Tech Stack

- **Backend**: Java 24+, Spring Boot 4.0+, Hibernate/JPA
- **Database**: PostgreSQL 16+, Redis (Caching & Session)
- **Security**: Spring Security, JWT, BCrypt, AOP-based Permission Control
- **Monitoring**: Spring Boot Actuator, Micrometer
- **Migration**: Liquibase

---

## 🔐 Test Accounts

The system follows a hierarchy: **SuperAdmin → Tenant → TenantAdmin → User**.  
Default password for all accounts: `Thaca@2026`

### 1. System Administrator (SuperAdmin)

Used to manage Tenants and global configuration.

- **Username**: `superadmin`

### 2. Tenant Administrator (TenantAdmin)

Manages users and data within a specific Tenant (e.g., Apple Tenant).

- **Username**: `admin` (Tenant: Apple)

### 3. End User

- **Username**: `user`

---

## 🛠️ Getting Started

1. **Requirements**: JDK 24, PostgreSQL, Redis
2. **Database Setup**: Liquibase will automatically create the `auth` schema and required tables when `thaca-auth` starts.
3. **Startup Order**:
   - `thaca-auth` — Authentication & Tenant APIs
   - `thaca-cms` — Admin API layer
   - `thaca-gateway` — Routing

---

## 📂 API Documentation

- **Swagger UI**: `http://localhost:1001/swagger-ui.html`
- **Postman**: Import `Thaca.postman_collection.json` from the root directory

---

_Developed by the Thaca Team — 2026_

---

---

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

- **Backend**: Java 24+, Spring Boot 4.0+, Hibernate/JPA
- **Database**: PostgreSQL 16+, Redis (Caching & Session)
- **Security**: Spring Security, JWT, BCrypt, AOP-based Permission Control
- **Monitoring**: Spring Boot Actuator, Micrometer
- **Migration**: Liquibase

---

## 🔐 Thông Tin Tài Khoản Thử Nghiệm

Hệ thống được thiết lập theo phân cấp: **SuperAdmin → Tenant → TenantAdmin → User**.  
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

1. **Yêu cầu**: JDK 24, PostgreSQL, Redis
2. **Khởi động Database**: Liquibase sẽ tự động tạo schema `auth` và các bảng cần thiết khi chạy `thaca-auth`.
3. **Thứ tự khởi động**:
   - `thaca-auth` — API xác thực & Tenant
   - `thaca-cms` — API quản trị
   - `thaca-gateway` — Routing

---

## 📂 Tài Liệu

- **Debezium**:

```bash
cd .devcontainer
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-connector.json
```

- **Swagger UI**: `http://localhost:1001/swagger-ui.html`
- **Postman**: Import file `Thaca.postman_collection.json` tại thư mục gốc

---

_Phát triển bởi đội ngũ Thaca — 2026_
