<div align="center">
  <a href="README.md"><b>README</b></a> | 
  <a href="THACA_FRAMEWORK_GUIDE.md"><b>FRAMEWORK GUIDE</b></a> | 
  <a href="CONTRIBUTING.md"><b>CONTRIBUTING</b></a>
</div>

# Thaca Framework Developer Guide 🚀

Chào mừng bạn đến với bộ khung phát triển **Thaca Framework**. Tài liệu này hướng dẫn cách sử dụng các tính năng cốt lõi của Framework để xây dựng Microservices chuẩn Enterprise.

---

## 1. Cấu trúc Request/Response chuẩn (Standard API)

Hệ thống sử dụng cơ chế bóc tách tự động, giúp mã nguồn tại Controller cực kỳ gọn nhẹ.

> [!IMPORTANT]
> **QUY TẮC VÀNG:** Hệ thống **KHÔNG SỬ DỤNG** phương thức `GET`. Tất cả các API (kể cả tìm kiếm, lấy chi tiết) đều phải sử dụng phương thức `POST` để truyền được `ApiPayload`.

### Cấu trúc Payload gửi từ Frontend:

```json
{
  "header": {
    "channel": "CMS",
    "transId": "unique-uuid-123",
    "timestamp": "2026-04-24T14:38:00Z"
  },
  "body": {
    "transId": "unique-uuid-123",
    "data": {
      "id": "12345",
      ...
    }
  }
}
```

---

## 2. Bảo mật & Phân quyền (Security & Permissions)

### @CheckPermission

Cho phép kiểm tra một hoặc nhiều quyền cùng lúc bằng cách truyền vào một mảng.

```java
// Yêu cầu user có ít nhất một trong các quyền này
@CheckPermission({"USER_VIEW", "USER_MANAGE"})
@PostMapping("/search")
public ResponseEntity<ApiPayload<UserDTO>> search(SearchReq request) { ... }
```

---

## 3. Cơ chế Xử lý & Validation (@FwMode)

Đây là tính năng cốt lõi giúp tách biệt hoàn toàn Logic Nghiệp vụ và Logic Kiểm tra dữ liệu. Framework sẽ tự động tìm kiếm một method khác trong cùng Class có cùng `name` nhưng mang `ModeType.VALIDATE` để thực thi trước.

### Ví dụ triển khai:

```java
@FwMode(name = "CreateUser", type = ModeType.HANDLE)
@PostMapping("/create")
public ResponseEntity<ApiPayload<Void>> create(CreateUserReq request) {
  return ResponseEntity.ok(ApiPayload.success(null));
}

@FwMode(name = "CreateUser", type = ModeType.VALIDATE)
public void validateCreateUser(CreateUserReq request) {
  if (request.getUsername() == null) {
    // Cách ném lỗi chuẩn Framework
    throw new FwException(CommonErrorMessage.BAD_REQUEST);
  }
}
```

---

## 4. Xử lý lỗi (Error Handling)

Framework sử dụng cơ chế quản lý lỗi tập trung thông qua `FwException` và `ErrorMessageRule`.

### Quy tắc ném lỗi:

Bạn không được ném Exception thô mà phải sử dụng `FwException` kèm theo một mã lỗi đã được định nghĩa.

```java
throw new FwException(CommonErrorMessage.USER_NOT_FOUND);
```

### Cách định nghĩa mã lỗi mới:

Mã lỗi phải triển khai interface `ErrorMessageRule`. Thông thường bạn sẽ thêm vào Enum `ErrorMessage` của Service:

```java
public enum ErrorMessage implements ErrorMessageRule {
  USER_EXISTED("USER.EXISTED", "Lỗi", "Người dùng đã tồn tại", "Error", "User already existed"),

  // Implement các method của ErrorMessageRule...
}
```

---

## 5. Tự động Validation & Context (@FwRequest)

Sử dụng `@FwRequest` kết hợp với `@FwMode` để kích hoạt chuỗi kiểm tra. Sau khi qua Resolver, bạn có thể truy xuất thông tin Header (như `transId`, `channel`) ở bất kỳ tầng nào:

```java
ApiHeader header = FwContextHeader.get();

String transId = header.getTransId();
```

---

## 6. Chống trùng lặp Request (Idempotency)

Framework sử dụng `transId` trong Header để ngăn chặn việc thực hiện cùng một giao dịch nhiều lần. Frontend **BẮT BUỘC** phải sinh ra `transId` duy nhất cho mỗi hành động bấm nút.

---

> [!IMPORTANT]
>
> - **KHÔNG dùng GET**: 100% API là POST.
> - **Luôn dùng FwException**: Để hệ thống tự động format response lỗi.
> - **transId trong Body**: Thường dùng để đối soát sâu hoặc lưu vết nghiệp vụ.
