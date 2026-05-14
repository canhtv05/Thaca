import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h5>Thông báo hệ thống (Social Push)</h5>
      <p>Quản lý các chiến dịch thông báo và tin nhắn hệ thống.</p>
      <div class="p-4 border-2 border-dashed border-info/30 border-radius-md bg-info/5">
        <p class="text-sm font-semibold mb-2">Logic nghiệp vụ cần xử lý:</p>
        <ul class="text-muted-foreground text-sm space-y-1">
          <li>• Tích hợp Firebase Cloud Messaging (FCM) để gửi Push Notification.</li>
          <li>• Phân loại đối tượng: Gửi toàn sàn, gửi theo Group User, hoặc gửi đích danh ID.</li>
          <li>
            • Quản lý Template: Soạn thảo nội dung thông báo có chứa biến (ví dụ: Chào
            {{ username }}).
          </li>
          <li>• Tracking: Theo dõi trạng thái Đã gửi / Đã đọc (Seen) của thông báo.</li>
        </ul>
      </div>
    </div>
  `,
})
export class NotificationsComponent {
  private auth = inject(AuthService);
  username = this.auth.user()?.username;
}
