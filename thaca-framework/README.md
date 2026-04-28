# Thaca Framework

Framework nội bộ hỗ trợ xây dựng microservices cho hệ thống Thaca.

## Cấu trúc

```
thaca-framework/
├── framework-core/                  # Core logic, annotations, AOP
├── framework-blocking-starter/      # Blocking (Spring MVC) starter
└── framework-reactive-starter/      # Reactive (WebFlux) starter
```

## Modules

### framework-core

Chứa annotations, aspects, services, DTOs dùng chung giữa tất cả microservices.

### framework-blocking-starter

Starter cho các service dùng Spring MVC (blocking). Bao gồm:

- `RestTemplate` config với JDK HttpClient (HTTP/2)
- `InternalApiClient` để gọi API nội bộ
- `InternalApiProxyFactory` để tạo declarative client
- JPA audit config, Redis config, JWT filter

---

## Annotations

### `@FwRequest`

Gắn lên **Controller method** để khai báo loại request và tên service.

```java
@PostMapping("/search")
@FwRequest(name = "cms.searchTenants", type = RequestType.PROTECTED)
public ResponseEntity<SearchResponse<TenantDTO>> search(SearchRequest<TenantDTO> request) {
  return ResponseEntity.ok(process.process(request));
}
```

| Thuộc tính | Mô tả                                                             |
| ---------- | ----------------------------------------------------------------- |
| `name`     | Tên service method, dùng để lookup handler trong `FwModeRegistry` |
| `type`     | `PUBLIC` / `PROTECTED` / `INTERNAL`                               |

- `PUBLIC` — không cần đăng nhập
- `PROTECTED` — cần JWT token hợp lệ
- `INTERNAL` — cần API key (giao tiếp giữa các service)

### `@FwMode`

Gắn lên **Service method** để đăng ký handler xử lý business logic.

```java
@FwMode(name = "internal.cmsSearchTenants", type = ModeType.HANDLE)
public SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> request) {
  // logic
}

@FwMode(name = "internal.cmsSearchTenants", type = ModeType.VALIDATE)
public void validateSearchTenants(SearchRequest<TenantDTO> request) {
  // validate
}
```

| Thuộc tính | Mô tả                                                          |
| ---------- | -------------------------------------------------------------- |
| `name`     | Tên service method, phải khớp với `@FwRequest.name`            |
| `type`     | `HANDLE` — xử lý chính, `VALIDATE` — validate trước khi handle |

### `@FwSecurity`

Gắn lên Controller method để kiểm tra quyền đặc biệt.

```java
@FwSecurity(isSuperAdmin = true)
public ResponseEntity<TenantDTO> create(TenantDTO request) { ... }
```

### `@CheckPermission`

Gắn lên Controller method để kiểm tra quyền chi tiết.

```java
@CheckPermission(value = {"TENANT_CREATE", "TENANT_UPDATE"}, allMatched = false)
public ResponseEntity<TenantDTO> save(TenantDTO request) { ... }
```

| Thuộc tính   | Mô tả                                              |
| ------------ | -------------------------------------------------- |
| `value`      | Danh sách permission codes cần kiểm tra            |
| `allMatched` | `true` = cần tất cả quyền, `false` = cần ít nhất 1 |

### `@FwInternalApi`

Gắn lên **method trong interface** để khai báo endpoint nội bộ. Framework sẽ dựa vào các method này để thực hiện gọi API và tự động đăng ký handler vào `FwModeRegistry`.

```java
@FwInternalApi(path = "/cms/tenants/search", name = ServiceMethod.CMS_SEARCH_TENANTS)
SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> search);
```

| Thuộc tính | Mô tả                                                    |
| ---------- | -------------------------------------------------------- |
| `path`     | Đường dẫn API, nối vào sau base URL của service          |
| `name`     | Tên service method, tự động đăng ký vào `FwModeRegistry` |

---

## Luồng xử lý Request

```
Client (Browser/App)
    │
    ▼
Controller (@FwRequest)              ← xác định service name + auth check
    │
    ▼
FwApiProcess.process(request)        ← lookup handler theo service name
    │
    ▼
FwModeRegistry                       ← map: service name → handler function
    │
    ▼
Service (@FwMode HANDLE)             ← thực thi business logic
```

## Luồng gọi Internal API (giữa microservices)

```
thaca-cms                                    thaca-auth
─────────                                    ──────────
Controller (@FwRequest)
    │
    ▼
FwApiProcess.process()
    │
    ▼
FwModeRegistry
    │
    ▼
AuthClient proxy (@FwInternalApi)
    │
    ▼
InternalApiClient.post()
    │  HTTP/2
    ▼
                                    Controller (@FwRequest INTERNAL)
                                        │
                                        ▼
                                    FwApiProcess.process()
                                        │
                                        ▼
                                    Service (@FwMode HANDLE)
```

---

## Declarative Internal API Client

Thay vì viết boilerplate class cho mỗi service call, chỉ cần khai báo interface:

```java
public interface AuthClient {
  @FwInternalApi(path = "/cms/tenants/search", name = ServiceMethod.CMS_SEARCH_TENANTS)
  SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> search);

  @FwInternalApi(path = "/cms/tenants/export", name = ServiceMethod.CMS_EXPORT_TENANT)
  byte[] exportTenants(SearchRequest<TenantDTO> request);
}
```

Đăng ký bean trong `@Configuration`:

```java
@Configuration
@RequiredArgsConstructor
public class InternalClientConfig {

  private final InternalApiProxyFactory proxyFactory;
  private final FrameworkProperties frameworkProperties;

  @Bean
  public AuthClient authClient() {
    // Truyền interface và baseUrl (đã bao gồm context path)
    return proxyFactory.create(
      AuthClient.class,
      frameworkProperties.getRoutes().getAuthService() + "/internal"
    );
  }
}
```

Framework tự động:

- Tạo proxy implementation cho interface.
- Ghép URL: `baseUrl + path`.
- Resolve `ParameterizedTypeReference` từ return type.
- Route `byte[]` return sang binary download.
- Đăng ký tất cả methods vào `FwModeRegistry` để có thể gọi ngược lại qua `fwApiProcess` nếu cần.

### Thêm API mới

Chỉ cần thêm 2 dòng vào interface:

```java
@FwInternalApi(path = "/cms/new-endpoint", name = ServiceMethod.CMS_NEW_METHOD)
NewDTO newMethod(NewRequest request);
```

### Thêm service mới

1. Thêm cấu hình URL vào `FrameworkProperties.RoutesConfig`.
2. Tạo interface client với các method gắn `@FwInternalApi`.
3. Đăng ký Bean trong `@Configuration` bằng `proxyFactory.create(Interface.class, url)`.

---

## Response Format

Tất cả API đều trả về format thống nhất `ApiPayload<T>`:

```json
{
    "header": {
        "username": "superadmin",
        "language": "vi",
        "channel": "WEB",
        "deviceId": "xxx",
        "timestamp": 1777351305212
    },
    "body": {
        "transId": "afdaaa08-6e51-50bd-1777351305212",
        "status": "OK",
        "data": { ... }
    }
}
```

Khi lỗi:

```json
{
  "body": {
    "status": "FAILED",
    "data": {
      "code": "REQUEST.INVALID.PARAMS",
      "titleVi": "Dữ liệu request không hợp lệ",
      "titleEn": "Request invalid params"
    }
  }
}
```

---

## Cấu hình

```yaml
application:
  security:
    base64-secret: '...'
    valid-duration-in-seconds: 3600
    cms-valid-duration-in-seconds: 28800
    refresh-duration-in-seconds: 86400
    cookie-domain: 'localhost'

  redis:
    client-name: 'thaca'
    address: 'redis://localhost:6379'
    password: ''
    minimum-idle: 5
    max-pool-size: 20

  http-client:
    api-key: 'your-internal-api-key'
    connect-timeout: 5000
    read-timeout: 5000

  routes:
    auth-service: 'http://localhost:8081'
```

---

## HTTP Client

Sử dụng JDK HttpClient (Java 11+) với HTTP/2:

- Connection pooling tự động
- HTTP/2 multiplexing (nhiều request trên 1 TCP connection)
- Keep-alive tự động
- Không cần thêm dependency (Apache HttpClient, OkHttp, ...)

---

## Quy tắc API

> **100% API sử dụng POST.** Không dùng GET, kể cả tìm kiếm và lấy chi tiết, để truyền được `ApiPayload`.

### Payload chuẩn gửi từ Frontend

```json
{
  "header": {
    "channel": "WEB", //WEB | MOBILE
    "timestamp": "2026-04-24T14:38:00Z"
  },
  "body": {
    "transId": "unique-uuid-123",
    "data": {
      "id": "12345"
    }
  }
}
```

### Truy xuất Header Context

Sau khi qua Resolver, truy xuất thông tin Header ở bất kỳ tầng nào:

```java
ApiHeader header = FwContextHeader.get();

String transId = header.getTransId();
```

---

## Xử lý lỗi

Không ném Exception thô. Luôn sử dụng `FwException` kèm mã lỗi đã định nghĩa:

```java
throw new FwException(CommonErrorMessage.USER_NOT_FOUND);
```

### Định nghĩa mã lỗi mới

Triển khai interface `ErrorMessageRule`, thêm vào Enum `ErrorMessage` của Service:

```java
public enum ErrorMessage implements ErrorMessageRule {
  USER_EXISTED("USER.EXISTED", "Lỗi", "Người dùng đã tồn tại", "Error", "User already existed"),
}
```

---

## Chống trùng lặp Request (Idempotency)

Framework sử dụng `transId` trong Header để ngăn thực hiện cùng một giao dịch nhiều lần. Frontend **bắt buộc** phải sinh `transId` duy nhất cho mỗi hành động.
