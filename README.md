<div align="center">
  <a href="README.md"><b>README</b></a> | 
  <a href="THACA_FRAMEWORK_GUIDE.md"><b>FRAMEWORK GUIDE</b></a> | 
  <a href="CONTRIBUTING.md"><b>CONTRIBUTING</b></a>
</div>

# 🌟 Thaca Project - Hệ Thống Quản Trị & Xác Thực Tập Trung

Chào mừng bạn đến với **Thaca**, một hệ sinh thái được xây dựng trên nền tảng Spring Boot hiện đại, tập trung vào tính bảo mật, khả năng mở rộng và trải nghiệm người dùng cao cấp.

---

## 🏗️ Kiến Trúc Hệ Thống (Module)

Dự án được tổ chức theo cấu trúc multi-module giúp tối ưu hóa việc tái sử dụng code và quản lý:

- 📂 **`thaca-framework`**: Nhân lõi của hệ thống, cung cấp các cấu hình Security, Audit, và các tiện ích dùng chung.
- 📂 **`thaca-auth`**: Dịch vụ xác thực trung tâm (OIDC-like), quản lý đa kênh (WEB/MOBILE) và đa đối tượng (Người dùng & Quản trị viên).
- 📂 **`thaca-web-cms`**: Giao diện quản trị (Web CMS) được xây dựng trên công nghệ hiện đại.
- 📂 **`thaca-common-object`**: Chứa các DTO, Domain dùng chung giữa các module.

---

## 🚀 Công Nghệ Sử Dụng

- **Backend**: Java 21+, Spring Boot 3.4+, Hibernate/JPA.
- **Database**: PostgreSQL, Liquibase (Migration).
- **Security**: Spring Security, JWT (Stateless), BCrypt.
- **Caching**: Redis (Token storage, Session).
- **Build Tool**: Maven/Gradle.

---

## 🔐 Thông Tin Tài Khoản Thử Nghiệm

Hệ thống đã được thiết lập sẵn các tài khoản với các vai trò khác nhau để phục vụ việc kiểm thử. **Mật khẩu mặc định cho tất cả tài khoản hệ thống là: `Thaca@2026`**

### 1. Tài Khoản Quản Trị (CMS - Login tại `/api/cms/sign-in`)

| Vai trò         | Username     | Đặc quyền                                               |
| :-------------- | :----------- | :------------------------------------------------------ |
| **Super Admin** | `superadmin` | Toàn quyền hệ thống, không bị chặn bởi bất kỳ rule nào. |
| **Admin**       | `admin`      | Quản lý người dùng và cấu hình CMS.                     |
| **Manager**     | `manager`    | Quản lý dữ liệu nghiệp vụ.                              |

### 2. Tài Khoản Người Dùng (Client - Login tại `/api/auth/sign-in`)

| Vai trò  | Username | Mật khẩu     |
| :------- | :------- | :----------- |
| **User** | `user`   | `Thaca@2026` |

---

## 🛠️ Hướng Dẫn Cài Đặt

1. **Yêu cầu hệ thống**:
   - Docker (để chạy Postgres & Redis nhanh chóng).
   - JDK 21.

2. **Khởi động Database**:

   ```bash
   # Sử dụng docker-compose hoặc cài đặt database thủ công
   # Cấu hình tại các file application.yml trong từng module
   ```

3. **Chạy ứng dụng Auth**:
   ```bash
   cd thaca-auth
   mvn spring-boot:run
   ```

---

## 📂 Tài Liệu API & Công Cụ

- **Postman Collection**: Nằm tại thư mục `docs/Thaca.postman_collection.json`.
- **Swagger UI**: Truy cập tại `http://localhost:1001/swagger-ui.html` (sau khi chạy Auth service).

---

_Phát triển bởi đội ngũ Thaca - 2026_
