import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';

export interface IFlowStep {
  label: string;
  detail: string;
  done?: boolean;
}

export interface IFeature {
  name: string;
  icon: string;
  priority: 'high' | 'medium' | 'low';
  status: 'done' | 'in_progress' | 'pending';
  description: string;
  flows: IFlowStep[];
}

export interface IFeatureGroup {
  title: string;
  icon: string;
  color: string;
  features: IFeature[];
}

@Component({
  selector: 'app-project-plan',
  standalone: true,
  imports: [CommonModule, TranslateModule, BreadcrumbComponent],
  templateUrl: './project-plan.component.html',
  styleUrl: './project-plan.component.scss',
})
export class ProjectPlanComponent {
  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-map', label: 'menu.project_plan' },
  ];

  expandedFeature = signal<string | null>(null);

  toggleFeature(id: string) {
    this.expandedFeature.set(this.expandedFeature() === id ? null : id);
  }

  groups: IFeatureGroup[] = [
    {
      title: 'Bảo mật & Ổn định',
      icon: 'pi pi-shield',
      color: '#ef4444',
      features: [
        {
          name: 'Implement roleGuard cho frontend',
          icon: 'pi pi-lock',
          priority: 'high',
          status: 'pending',
          description:
            'Bảo vệ route theo role người dùng. Hiện tại roleGuard luôn return true — bất kỳ ai cũng truy cập được mọi trang.',
          flows: [
            {
              label: 'Bước 1: Backend trả role trong JWT claims',
              detail:
                'Sửa AuthService — khi generate JWT, thêm trường `roles` (List<String>) và `isSuperAdmin` vào claims. Token response trả về kèm roles.',
            },
            {
              label: 'Bước 2: Frontend decode JWT lấy roles',
              detail:
                'Sửa AuthService ở frontend — decode JWT payload để lấy roles. Lưu vào `currentUser` signal.',
            },
            {
              label: 'Bước 3: Implement roleGuard thật sự',
              detail:
                'Sửa `role.guard.ts` — inject AuthService, so sánh route.data.requiredRoles với currentUser.roles. Nếu không match → redirect /403.',
            },
            {
              label: 'Bước 4: Gán requiredRoles cho route',
              detail:
                'Thêm `data: { requiredRoles: ["SUPER_ADMIN"] }` vào các route cần bảo vệ trong `system.route.ts`.',
            },
            {
              label: 'Bước 5: Test',
              detail:
                'Đăng nhập bằng user thường → verify không truy cập được trang admin. Đăng nhập super admin → verify truy cập bình thường.',
            },
          ],
        },
        {
          name: 'JWT Secret quản lý qua Secrets Manager',
          icon: 'pi pi-key',
          priority: 'high',
          status: 'pending',
          description:
            'Hiện tại JWT secret nằm trong YAML config. Nếu leak config là mất toàn bộ token. Cần chuyển sang secrets manager.',
          flows: [
            {
              label: 'Bước 1: Tách secret khỏi application.yml',
              detail:
                'Đổi `base64-secret` thành đọc từ env var: `${JWT_SECRET}`. Xóa giá trị hardcode trong YAML.',
            },
            {
              label: 'Bước 2: Tạo .env.local cho dev',
              detail:
                'Tạo file `.env.local` (gitignore) chứa JWT_SECRET dev. Dùng `springboot4-dotenv` đã có sẵn.',
            },
            {
              label: 'Bước 3: Production dùng Vault/AWS SM',
              detail:
                'Tạo VaultConfig hoặc AWSSecretsManagerConfig đọc secret khi app khởi động. Inject vào FrameworkProperties.',
            },
            {
              label: 'Bước 4: Rotate secret an toàn',
              detail:
                'Implement dual-key support — accept cả old và new secret trong thời gian transition. Sau 24h thì chỉ accept new.',
            },
          ],
        },
        {
          name: 'Database indexes cho query thường xuyên',
          icon: 'pi pi-database',
          priority: 'medium',
          status: 'pending',
          description:
            'Các bảng users, tenants, login_history chưa có index trên column query thường xuyên. Khi data lớn sẽ chậm.',
          flows: [
            {
              label: 'Bước 1: Audit query patterns',
              detail:
                'Xem log `FwFilter` để tìm URI query phổ biến. Map ra column nào đang WHERE/ORDER BY mà chưa có index.',
            },
            {
              label: 'Bước 2: Tạo Liquibase changelog',
              detail:
                'Tạo `003-indexes.xml` trong `db/changelog/changes/`. Thêm index cho: `users.username`, `users.email`, `tenants.code`, `tenants.status`, `login_history.login_time`, `login_history.user_id`.',
            },
            {
              label: 'Bước 3: Test performance',
              detail:
                'Dùng `EXPLAIN ANALYZE` trước và sau khi thêm index. Verify query plan sử dụng index.',
            },
          ],
        },
      ],
    },
    {
      title: 'Kiểm thử',
      icon: 'pi pi-check-circle',
      color: '#f59e0b',
      features: [
        {
          name: 'Unit test cho JWT & Authentication',
          icon: 'pi pi-verified',
          priority: 'high',
          status: 'pending',
          description:
            'Hiện tại không có test nào. JWT là core security — nếu có bug sẽ ảnh hưởng toàn hệ thống.',
          flows: [
            {
              label: 'Bước 1: Setup test infrastructure',
              detail:
                'Thêm `spring-boot-starter-test`, `H2` (in-memory DB cho test). Tạo `application-test.yml` với config test.',
            },
            {
              label: 'Bước 2: Unit test TokenProvider',
              detail:
                'Test generate/validate/extract token. Test expired token, invalid signature, malformed token. Mock Redis.',
            },
            {
              label: 'Bước 3: Unit test AuthService.authenticate',
              detail:
                'Test login thành công, sai password (lockout sau 5 lần), account bị khóa, account inactive. Mock UserRepository.',
            },
            {
              label: 'Bước 4: Integration test full auth flow',
              detail:
                'Test toàn bộ flow: register → login → get profile → refresh token → logout. Dùng MockMvc + H2.',
            },
          ],
        },
        {
          name: 'Unit test cho Permission & Multi-tenant',
          icon: 'pi pi-shield',
          priority: 'high',
          status: 'pending',
          description:
            'CheckPermissionAspect và Hibernate @Filter là 2 thành phần quan trọng nhất. Nếu có bug → lỗ hổng bảo mật nghiêm trọng.',
          flows: [
            {
              label: 'Bước 1: Test CheckPermissionAspect',
              detail:
                'Tạo test gọi method có @CheckPermission với user có quyền → pass. User không có quyền → throw AccessDeniedException.',
            },
            {
              label: 'Bước 2: Test Hibernate @Filter tenant isolation',
              detail:
                'Tạo 2 tenant A, B mỗi tenant có data riêng. Query với tenant A context → chỉ thấy data A. Switch sang B → chỉ thấy data B.',
            },
            {
              label: 'Bước 3: Test TenantFilterAspect',
              detail:
                'Verify @EnableTenantFilter tự động set Hibernate filter dựa trên JWT tenant code. Test khi không có tenant → query all.',
            },
          ],
        },
        {
          name: 'Frontend unit test (Vitest)',
          icon: 'pi pi-code',
          priority: 'medium',
          status: 'pending',
          description:
            'Vitest đã cài trong package.json nhưng chưa có test nào. Cần test các service và guard quan trọng.',
          flows: [
            {
              label: 'Bước 1: Setup test environment',
              detail:
                'Tạo `vitest.config.ts`. Setup TestBed cho Angular component testing. Tạo mock cho HttpClient, TranslateService.',
            },
            {
              label: 'Bước 2: Test AuthService',
              detail:
                'Test login/logout, token storage, currentUser signal update. Mock HTTP responses.',
            },
            {
              label: 'Bước 3: Test Guards',
              detail:
                'Test AuthGuard (redirect khi chưa login), GuestGuard (redirect khi đã login), RoleGuard (redirect khi sai role).',
            },
            {
              label: 'Bước 4: Test Shared components',
              detail:
                'Test DataTableComponent (load data, pagination, sort), ThacaModalComponent (show/hide), ThacaDropdownComponent.',
            },
          ],
        },
      ],
    },
    {
      title: 'Mở rộng tính năng',
      icon: 'pi pi-plus-circle',
      color: '#3b82f6',
      features: [
        {
          name: 'Quản lý Nội dung (Content Moderation)',
          icon: 'pi pi-clone',
          priority: 'high',
          status: 'pending',
          description:
            'Module quản lý bài viết, bình luận, media. CMS admin duyệt/xóa nội dung vi phạm.',
          flows: [
            {
              label: 'Bước 1: Tạo domain models',
              detail:
                'Tạo `Post` entity (id, title, content, authorId, tenantId, status, createdAt). Tạo `Comment` entity (id, postId, content, authorId, status). Tạo `Media` entity (id, url, type, size, uploaderId).',
            },
            {
              label: 'Bước 2: Tạo Repository & Service',
              detail:
                'Tạo PostRepository, CommentRepository, MediaRepository (extends JpaRepository). Tạo PostService, CommentService, MediaService với CRUD + search.',
            },
            {
              label: 'Bước 3: Tạo Internal API trong Auth',
              detail:
                'Thêm `internal.cmsSearchPosts`, `internal.cmsApprovePost`, `internal.cmsDeletePost` vào Auth microservice. CMS gọi qua InternalApiClient.',
            },
            {
              label: 'Bước 4: Tạo CMS Controller',
              detail:
                'Tạo PostController, CommentController, MediaController trong thaca-cms. Dùng @FwRequest, @FwMode pattern.',
            },
            {
              label: 'Bước 5: Tạo Frontend pages',
              detail:
                'Tạo `post-list.component` (DataTable + search + actions), `comment-list.component`, `media-library.component`. Thêm route vào `moderation.route.ts`.',
            },
            {
              label: 'Bước 6: i18n + Menu',
              detail:
                'Tạo `post.json`, `comment.json`, `media.json` cho en/vi. Thêm menu items trong MenuService.',
            },
          ],
        },
        {
          name: 'Hệ thống Thông báo Real-time (WebSocket)',
          icon: 'pi pi-bell',
          priority: 'medium',
          status: 'pending',
          description:
            'Gửi thông báo real-time cho admin khi có sự kiện: user đăng ký mới, bài viết bị report, tenant hết hạn...',
          flows: [
            {
              label: 'Bước 1: Setup WebSocket trong Gateway',
              detail:
                'Thêm `spring-boot-starter-websocket` vào thaca-gateway. Tạo WebSocketConfig với STOMP endpoint `/ws/notifications`.',
            },
            {
              label: 'Bước 2: Tạo Notification domain',
              detail:
                'Tạo Notification entity (id, type, title, message, targetType, targetId, read, createdAt). Tạo NotificationRepository.',
            },
            {
              label: 'Bước 3: Tạo NotificationService',
              detail:
                'Implement `sendNotification(userId, notification)` — lưu DB + push qua WebSocket. Implement `markAsRead`, `getUnreadCount`.',
            },
            {
              label: 'Bước 4: Frontend WebSocket client',
              detail:
                'Tạo NotificationService dùng RxJS StompJS. Subscribe `/user/queue/notifications`. Hiển thị badge count trên header bell icon.',
            },
            {
              label: 'Bước 5: Trigger notifications từ các service',
              detail:
                'Khi user đăng ký → notify admin. Khi có report → notify moderator. Khi tenant sắp hết hạn → notify super admin.',
            },
          ],
        },
        {
          name: 'Dashboard Phân tích (Analytics)',
          icon: 'pi pi-chart-bar',
          priority: 'medium',
          status: 'pending',
          description:
            'Trang dashboard tổng quan: số user active, tenant growth, login frequency, top features sử dụng.',
          flows: [
            {
              label: 'Bước 1: Tạo Analytics API trong Auth',
              detail:
                'Thêm các internal APIs: `cmsGetDashboardStats` (total users, tenants, logins today), `cmsGetLoginTrend` (logins 30 ngày), `cmsGetTenantGrowth` (tenants theo tháng).',
            },
            {
              label: 'Bước 2: Implement aggregation queries',
              detail:
                'Tạo DashboardRepository với native queries: COUNT users, COUNT tenants, GROUP BY login_history theo ngày. Cache kết quả 5 phút bằng Redis.',
            },
            {
              label: 'Bước 3: Tạo Dashboard component',
              detail:
                'Tạo `home.component` với 4 stat cards (users, tenants, logins, plans) + 2 charts (login trend, tenant growth). Dùng PrimeNG Chart hoặc Chart.js.',
            },
            {
              label: 'Bước 4: Real-time update',
              detail:
                'Sử dụng WebSocket (nếu đã có) hoặc polling 30 giây để cập nhật stats real-time.',
            },
          ],
        },
        {
          name: 'Quản lý Người dùng cuối (End Users)',
          icon: 'pi pi-users',
          priority: 'medium',
          status: 'pending',
          description:
            'Module quản lý người dùng cuối (social users) — khác với System Users (admin). Xem profile, khóa/mở khóa, verify.',
          flows: [
            {
              label: 'Bước 1: Tạo User domain',
              detail:
                'Tạo User entity (id, username, email, displayName, avatarUrl, status, verified, tenantId). Khác với SystemCredential (admin login).',
            },
            {
              label: 'Bước 2: Tạo CRUD service',
              detail:
                'Tạo UserService với search, getById, lock/unlock, verify. Dùng DataTable pattern giống Tenant/SystemUser.',
            },
            {
              label: 'Bước 3: Tạo Frontend pages',
              detail:
                'Tạo `user-list.component` với DataTable (search, lock/unlock, view detail). Tạo `user-detail.component` hiển thị profile + activity.',
            },
          ],
        },
      ],
    },
    {
      title: 'Production Ready',
      icon: 'pi pi-rocket',
      color: '#8b5cf6',
      features: [
        {
          name: 'Docker Compose cho tất cả Services',
          icon: 'pi pi-box',
          priority: 'high',
          status: 'pending',
          description:
            'Đóng gói toàn bộ microservices + dependencies thành Docker containers. One command khởi động cả hệ thống.',
          flows: [
            {
              label: 'Bước 1: Tạo Dockerfile cho mỗi service',
              detail:
                'Tạo `thaca-auth/Dockerfile` (multi-stage: build Maven → copy JRE 21). Tương tự cho thaca-cms, thaca-gateway. Frontend: `thaca-web-cms/Dockerfile` (build Angular → serve Nginx).',
            },
            {
              label: 'Bước 2: Tạo docker-compose.yml',
              detail:
                'Define services: postgres, redis, zookeeper, kafka, thaca-auth, thaca-cms, thaca-gateway, thaca-web-cms. Config networks, volumes, health checks.',
            },
            {
              label: 'Bước 3: Config environment variables',
              detail:
                'Tạo `.env.example` với tất cả env vars cần thiết. Mỗi service đọc config từ env thay vì YAML hardcode.',
            },
            {
              label: 'Bước 4: Health checks & dependency',
              detail:
                'Thêm `depends_on` với `condition: service_healthy`. Config health check cho Postgres (`pg_isready`), Redis (`redis-cli ping`), mỗi service (`/actuator/health`).',
            },
          ],
        },
        {
          name: 'Structured Logging (JSON format)',
          icon: 'pi pi-list',
          priority: 'high',
          status: 'pending',
          description:
            'Hiện tại log dạng plain text. Production cần JSON format để ELK/Datadog parse được.',
          flows: [
            {
              label: 'Bước 1: Tạo logback-spring.json',
              detail:
                'Tạo `logback-spring.xml` với JSON encoder (LogstashEncoder). Thêm MDC fields: traceId, spanId, transId, userId, tenantCode.',
            },
            {
              label: 'Bước 2: Config per environment',
              detail:
                'Profile `dev`: plain text như hiện tại. Profile `prod`: JSON format + async appender + file rotation.',
            },
            {
              label: 'Bước 3: Mask sensitive data',
              detail:
                'Mở rộng `FwFilter.maskSensitiveData()` — thêm email, phone, token vào danh sách mask. Áp dụng cả request và response log.',
            },
          ],
        },
        {
          name: 'Distributed Tracing (OpenTelemetry)',
          icon: 'pi pi-sitemap',
          priority: 'medium',
          status: 'pending',
          description:
            'Hiện tại traceId chỉ sinh trong 1 service. Cần propagate qua Gateway → Auth → CMS để trace toàn bộ request.',
          flows: [
            {
              label: 'Bước 1: Thêm OpenTelemetry dependencies',
              detail:
                'Thêm `opentelemetry-spring-boot-starter` vào framework-core. Config exporter endpoint (Jaeger/Tempo).',
            },
            {
              label: 'Bước 2: Gateway propagate trace context',
              detail:
                'Sửa AuthenticationFilter — forward `traceparent` header downstream. Tạo TraceContextFilter trong framework-core để extract trace context.',
            },
            {
              label: 'Bước 3: Internal API propagate trace',
              detail:
                'Sửa InternalApiClient/RestTemplateInterceptor — thêm `traceparent` header vào mỗi internal request.',
            },
            {
              label: 'Bước 4: Setup Jaeger/Tempo',
              detail:
                'Thêm Jaeger vào docker-compose.yml. Config sampling rate (10% production, 100% dev).',
            },
          ],
        },
      ],
    },
    {
      title: 'Tối ưu & Mở rộng',
      icon: 'pi pi-chart-line',
      color: '#10b981',
      features: [
        {
          name: 'API Rate Limiting',
          icon: 'pi pi-ban',
          priority: 'high',
          status: 'pending',
          description:
            'Giới hạn số request mỗi phút theo tenant/user để chống abuse. Đặc biệt quan trọng khi mở API cho bên thứ 3.',
          flows: [
            {
              label: 'Bước 1: Tạo RateLimit annotation',
              detail:
                'Tạo `@RateLimit(maxRequests = 60, windowSeconds = 60)` annotation. Áp dụng ở class hoặc method level.',
            },
            {
              label: 'Bước 2: Implement RateLimitAspect',
              detail:
                'Tạo AOP aspect intercept @RateLimit. Dùng Redis INCR + EXPIRE để đếm request theo key (userId hoặc IP). Nếu vượt limit → throw FwException(429).',
            },
            {
              label: 'Bước 3: Config per plan',
              detail:
                'Mỗi Plan (FREE/BASIC/PRO/ENTERPRISE) có rate limit khác nhau. Đọc từ Plan entity.',
            },
            {
              label: 'Bước: Gateway level rate limiting',
              detail:
                'Thêm RequestRateLimiter filter trong Spring Cloud Gateway cho global protection.',
            },
          ],
        },
        {
          name: 'Root Parent POM (Version Management)',
          icon: 'pi pi-sitemap',
          priority: 'low',
          status: 'pending',
          description:
            'Hiện tại mỗi module tự quản lý dependency version → dễ bị mismatch (như Spring Boot 4.0.4 vs 4.0.5).',
          flows: [
            {
              label: 'Bước 1: Tạo root pom.xml',
              detail:
                'Tạo `pom.xml` ở root Thaca/. Packaging: pom. Khai báo `<modules>`: thaca-common-object, thaca-framework, thaca-auth, thaca-cms, thaca-gateway.',
            },
            {
              label: 'Bước 2: Centralize dependency versions',
              detail:
                'Thêm `<dependencyManagement>` trong root POM. Định nghĩa version cho: spring-boot, spring-cloud, lombok, redisson, jjwt, poi...',
            },
            {
              label: 'Bước 3: Update child POMs',
              detail:
                'Mỗi module đổi parent thành root POM. Bỏ version ở dependencies (kế thừa từ parent). Verify build.',
            },
          ],
        },
      ],
    },
    {
      title: 'Trải nghiệm Người dùng',
      icon: 'pi pi-star',
      color: '#f59e0b',
      features: [
        {
          name: 'Dark Mode hoàn chỉnh',
          icon: 'pi pi-moon',
          priority: 'medium',
          status: 'pending',
          description:
            'ThemeService đã có toggle nhưng chưa áp dụng đầy đủ cho tất cả component. Cần đảm bảo mọi element đều hỗ trợ dark mode.',
          flows: [
            {
              label: 'Bước 1: Audit CSS variables',
              detail:
                'Kiểm tra tất cả SCSS files — đảm bảo dùng CSS variables (--surface-card, --text-color, ...) thay vì hardcode color.',
            },
            {
              label: 'Bước 2: Fix DataTable dark mode',
              detail:
                'DataTable component có SCSS riêng nhưng chưa dùng CSS variables cho tất cả. Fix header, row hover, pagination trong dark mode.',
            },
            {
              label: 'Bước 3: Fix Modal & Form dark mode',
              detail:
                'ThacaModal, ThacaInput, ThacaDropdown cần theme-aware styling cho border, background, placeholder text.',
            },
            {
              label: 'Bước 4: Persist theme preference',
              detail:
                'Lưu theme vào localStorage. Khi reload → đọc lại preference. Sync với backend user profile nếu có.',
            },
          ],
        },
        {
          name: 'Bulk Actions cho DataTable',
          icon: 'pi pi-check-square',
          priority: 'medium',
          status: 'pending',
          description:
            'Cho phép chọn nhiều rows cùng lúc để thực hiện hành động hàng loạt: xóa, export, lock/unlock...',
          flows: [
            {
              label: 'Bước 1: Thêm checkbox column vào DataTable',
              detail:
                'Thêm option `selectable: true` vào ITableConfig. Render checkbox ở đầu mỗi row. Thêm "Select All" ở header.',
            },
            {
              label: 'Bước 2: Tạo selection state',
              detail:
                'Thêm `selectedRows` signal trong DataTableComponent. Emit `onSelectionChange` event khi selection thay đổi.',
            },
            {
              label: 'Bước 3: Tạo Bulk Action Bar',
              detail:
                'Hiển thị floating bar khi có selection: "Đã chọn X items" + các action buttons (Delete, Export, Lock...).',
            },
            {
              label: 'Bước 4: Backend bulk API',
              detail:
                'Tạo generic `bulkDelete`, `bulkUpdate` endpoints. Accept list of IDs + action. Process trong transaction.',
            },
          ],
        },
        {
          name: 'File Upload Component (Avatar, Logo, Media)',
          icon: 'pi pi-upload',
          priority: 'medium',
          status: 'pending',
          description:
            'Hiện tại logoUrl/AvatarUrl là text input. Cần component upload file thật sự với preview, drag-drop, crop.',
          flows: [
            {
              label: 'Bước 1: Tạo FileUploadComponent',
              detail:
                'Tạo `thaca-file-upload.component` với drag-drop zone, click to browse, preview image. Hỗ trợ multiple files.',
            },
            {
              label: 'Bước 2: Backend upload API',
              detail:
                'Tạo FileController trong thaca-cms: POST `/files/upload` nhận MultipartFile. Lưu vào storage (local/S3). Trả về URL.',
            },
            {
              label: 'Bước 3: Image crop & resize',
              detail:
                'Tích hợp `cropperjs` cho avatar crop (1:1 ratio). Auto resize logo về maxWidth 200px trước khi upload.',
            },
            {
              label: 'Bước 4: Tích hợp vào Tenant/SystemUser',
              detail:
                'Thay text input logoUrl/avatarUrl bằng FileUploadComponent. Hiển thị preview trong form.',
            },
          ],
        },
        {
          name: 'Activity Audit Log',
          icon: 'pi pi-history',
          priority: 'medium',
          status: 'pending',
          description:
            'Ghi lại mọi hành động quan trọng: ai đã tạo/sửa/xóa gì, khi nào, từ IP nào. Cần cho compliance và troubleshooting.',
          flows: [
            {
              label: 'Bước 1: Tạo AuditLog entity',
              detail:
                'Tạo `AuditLog` entity (id, userId, action, entityType, entityId, oldValue, newValue, ip, userAgent, createdAt). Index trên userId và createdAt.',
            },
            {
              label: 'Bước 2: Tạo AuditAspect (AOP)',
              detail:
                'Tạo `@Auditable` annotation + AOP aspect. Intercept method calls, capture before/after state, async save vào DB.',
            },
            {
              label: 'Bước 3: Áp dụng cho các service',
              detail:
                'Thêm `@Auditable` vào TenantService.create/update/lock, UserService.create/update, RoleService.assignPermissions...',
            },
            {
              label: 'Bước 4: Frontend Audit Log page',
              detail:
                'Tạo `audit-log.component` với DataTable: filter theo user, action, entity type, date range. Hiển thị diff (old vs new value).',
            },
          ],
        },
        {
          name: 'Export nâng cao (PDF, CSV, Custom template)',
          icon: 'pi pi-file-export',
          priority: 'low',
          status: 'pending',
          description:
            'Hiện tại chỉ export Excel. Cần thêm PDF (cho invoice, report), CSV (cho data analysis), và custom template.',
          flows: [
            {
              label: 'Bước 1: Thêm CSV export vào ExcelEngine',
              detail:
                'Mở rộng `ExcelEngine` thêm method `exportCsv(schema, rows)`. Output UTF-8 BOM để Excel mở tiếng Việt đúng.',
            },
            {
              label: 'Bước 2: Thêm PDF export',
              detail:
                'Thêm `itext7` dependency. Tạo `PdfEngine` class tương tự ExcelEngine. Hỗ trợ table layout, header/footer, page numbers.',
            },
            {
              label: 'Bước 3: Template-based export',
              detail:
                'Tạo template engine đọc `.docx` template với placeholders {{field}}. Fill data và convert sang PDF.',
            },
            {
              label: 'Bước 4: Frontend export dialog',
              detail:
                'Tạo modal chọn format (Excel/CSV/PDF), chọn columns muốn export, date range. Gọi API tương ứng.',
            },
          ],
        },
        {
          name: 'Multi-language nâng cao (Dynamic i18n)',
          icon: 'pi pi-globe',
          priority: 'low',
          status: 'pending',
          description:
            'Hiện tại i18n tĩnh (JSON files). Cần dynamic: admin có thể edit translations trên CMS, thêm ngôn ngữ mới không cần deploy.',
          flows: [
            {
              label: 'Bước 1: Tạo Translation entity',
              detail:
                'Tạo `Translation` entity (id, lang, key, value, module). Seed dữ liệu từ các JSON files hiện tại.',
            },
            {
              label: 'Bước 2: Translation API',
              detail:
                'Tạo TranslationService: getByLang(lang) trả về Map<String, String>. Cache trong Redis, invalidate khi admin edit.',
            },
            {
              label: 'Bước 3: Frontend hybrid loader',
              detail:
                'Sửa I18nService: ưu tiên load từ API (nếu có), fallback về static JSON files. Cache trong memory.',
            },
            {
              label: 'Bước 4: Admin translation editor',
              detail:
                'Tạo `translation-manager.component` với table hiển thị key-value theo ngôn ngữ. Inline edit, search, filter theo module.',
            },
          ],
        },
      ],
    },
    {
      title: 'DevOps & Monitoring',
      icon: 'pi pi-server',
      color: '#6366f1',
      features: [
        {
          name: 'Kubernetes Deployment',
          icon: 'pi pi-cloud',
          priority: 'high',
          status: 'pending',
          description:
            'Deploy toàn bộ microservices lên Kubernetes cluster. Hỗ trợ auto-scaling, rolling update, self-healing.',
          flows: [
            {
              label: 'Bước 1: Tạo K8s manifests',
              detail:
                'Tạo `k8s/` folder. Mỗi service có: Deployment (replicas, resource limits, health probes), Service (ClusterIP), ConfigMap, Secret.',
            },
            {
              label: 'Bước 2: Ingress configuration',
              detail:
                'Tạo Ingress resource với Nginx Ingress Controller. Route: `api.thaca.com/auth/*` → thaca-auth, `api.thaca.com/admin/*` → thaca-cms.',
            },
            {
              label: 'Bước 3: HPA (Horizontal Pod Autoscaler)',
              detail:
                'Config HPA cho thaca-auth và thaca-cms: scale up khi CPU > 70%, min 2 replicas, max 10.',
            },
            {
              label: 'Bước 4: Helm Chart',
              detail:
                'Tạo Helm chart packaging tất cả K8s manifests. Config values.yaml cho từng environment (dev/staging/prod).',
            },
          ],
        },
        {
          name: 'CI/CD Pipeline (GitHub Actions)',
          icon: 'pi pi-sync',
          priority: 'high',
          status: 'pending',
          description:
            'Tự động build, test, deploy khi push code. PR merge → staging, tag release → production.',
          flows: [
            {
              label: 'Bước 1: Tạo CI workflow',
              detail:
                'Tạo `.github/workflows/ci.yml`: on push/PR → checkout → build Java (Maven) → build Angular → run tests → upload artifacts.',
            },
            {
              label: 'Bước 2: Tạo CD workflow',
              detail:
                'Tạo `.github/workflows/cd.yml`: on tag `v*` → build Docker images → push to registry → deploy to K8s (staging → manual approve → production).',
            },
            {
              label: 'Bước 3: Code quality gates',
              detail:
                'Thêm SonarQube scan trong CI. Block merge nếu: coverage < 70%, bugs > 0, security hotspots > 0.',
            },
            {
              label: 'Bước 4: Secret management',
              detail:
                'Dùng GitHub Secrets cho Docker registry credentials, K8s kubeconfig, JWT secrets. Inject qua environment variables.',
            },
          ],
        },
        {
          name: 'Monitoring Dashboard (Grafana + Prometheus)',
          icon: 'pi pi-chart-bar',
          priority: 'medium',
          status: 'pending',
          description:
            'Theo dõi real-time: CPU, memory, request rate, error rate, response time. Alert khi có bất thường.',
          flows: [
            {
              label: 'Bước 1: Thêm Micrometer dependencies',
              detail:
                'Thêm `micrometer-registry-prometheus` vào mỗi service. Expose `/actuator/prometheus` endpoint.',
            },
            {
              label: 'Bước 2: Prometheus config',
              detail:
                'Tạo `prometheus.yml` scrape config. Scrape mỗi service mỗi 15 giây. Thêm vào docker-compose.',
            },
            {
              label: 'Bước 3: Grafana dashboards',
              detail:
                'Tạo dashboard: JVM metrics (heap, threads, GC), HTTP metrics (request rate, latency, error rate), Business metrics (active users, new tenants).',
            },
            {
              label: 'Bước 4: Alerting rules',
              detail:
                'Tạo Prometheus alert rules: error rate > 5% trong 5 phút, response time P99 > 2s, disk usage > 80%. Gửi notification qua Slack/Email.',
            },
          ],
        },
        {
          name: 'Database Backup & Migration Strategy',
          icon: 'pi pi-database',
          priority: 'medium',
          status: 'pending',
          description:
            'Tự động backup PostgreSQL hàng ngày. Strategy cho zero-downtime migration khi thay đổi schema.',
          flows: [
            {
              label: 'Bước 1: Automated backup',
              detail:
                'Tạo CronJob trong K8s chạy `pg_dump` hàng ngày lúc 2AM. Lưu vào S3/GCS với retention 30 ngày.',
            },
            {
              label: 'Bước 2: Backup verification',
              detail: 'Tạo script restore backup vào test DB, verify data integrity. Chạy weekly.',
            },
            {
              label: 'Bước 3: Zero-downtime migration',
              detail:
                'Document strategy: thêm column nullable trước → deploy code đọc column mới → backfill data → add NOT NULL constraint. Dùng Liquibase.',
            },
            {
              label: 'Bước 4: Point-in-time recovery',
              detail:
                'Config PostgreSQL WAL archiving cho PITR. Document procedure restore về thời điểm cụ thể.',
            },
          ],
        },
        {
          name: 'Feature Flags (Toggle features)',
          icon: 'pi pi-flag',
          priority: 'low',
          status: 'pending',
          description:
            'Bật/tắt tính năng theo tenant hoặc user mà không cần deploy lại. Hữu ích cho A/B testing và gradual rollout.',
          flows: [
            {
              label: 'Bước 1: Tạo FeatureFlag entity',
              detail:
                'Tạo `FeatureFlag` entity (id, key, description, enabled, tenantId nullable, rolloutPercentage). Cache trong Redis.',
            },
            {
              label: 'Bước 2: Tạo FeatureFlagService',
              detail:
                'Implement `isEnabled(flagKey, tenantId, userId)`: check Redis cache → DB → default. Hỗ trợ percentage rollout (hash userId).',
            },
            {
              label: 'Bước 3: Tạo @FeatureFlag annotation',
              detail:
                'Tạo annotation + AOP aspect. Nếu flag disabled → skip method hoặc return default value.',
            },
            {
              label: 'Bước 4: Admin UI quản lý flags',
              detail:
                'Tạo `feature-flags.component` với toggle switches, tenant filter, rollout percentage slider.',
            },
          ],
        },
        {
          name: 'API Documentation (Swagger/OpenAPI)',
          icon: 'pi pi-book',
          priority: 'low',
          status: 'pending',
          description:
            'Swagger UI đã có nhưng chưa đầy đủ. Cần document tất cả endpoints, request/response schemas, authentication.',
          flows: [
            {
              label: 'Bước 1: Thêm OpenAPI annotations',
              detail:
                'Thêm `@Operation`, `@ApiResponse`, `@Parameter` annotations vào tất cả controllers. Mô tả chi tiết request/response.',
            },
            {
              label: 'Bước 2: Document authentication flow',
              detail:
                'Tạo SecurityScheme trong OpenAPI config: Basic Auth cho internal APIs, Bearer JWT cho external APIs.',
            },
            {
              label: 'Bước 3: Generate client SDK',
              detail:
                'Dùng `openapi-generator` generate Java/TypeScript client SDK từ OpenAPI spec. Publish lên internal Maven/npm registry.',
            },
            {
              label: 'Bước 4: API changelog',
              detail:
                'Tạo `CHANGELOG.md` cho API. Document breaking changes, deprecations, new endpoints theo version.',
            },
          ],
        },
      ],
    },
  ];

  getDoneCount(group: IFeatureGroup): number {
    return group.features.filter((f) => f.status === 'done').length;
  }

  getPriorityClass(p: string): string {
    return p === 'high' ? 'priority-high' : p === 'medium' ? 'priority-medium' : 'priority-low';
  }

  getStatusClass(s: string): string {
    return s === 'done'
      ? 'status-done'
      : s === 'in_progress'
        ? 'status-progress'
        : 'status-pending';
  }
}
