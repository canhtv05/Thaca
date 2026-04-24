import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-system-settings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h5>Cấu hình hạ tầng (Infrastructure Settings)</h5>
      <p>Thiết lập các kết nối kỹ thuật cốt lõi của hệ thống.</p>
      <div class="p-4 border-2 border-dashed border-danger/30 border-radius-md bg-danger/5">
        <p class="text-sm font-semibold mb-2 text-danger">Lưu ý bảo mật:</p>
        <ul class="text-muted-foreground text-sm space-y-1">
          <li>• <b>SMTP:</b> Cấu hình Server Mail để gửi Verification Code và System Alerts.</li>
          <li>
            • <b>Cloud Storage:</b> Kết nối AWS S3 hoặc MinIO để lưu trữ ảnh/video từ Social Feed.
          </li>
          <li>• <b>Security:</b> Quản lý các mã JWT Secret, API Key của các dịch vụ Gateway.</li>
          <li>
            • <b>Maintenance Mode:</b> Chế độ bảo trì hệ thống (Bật/Tắt truy cập từ người dùng).
          </li>
        </ul>
      </div>
    </div>
  `,
})
export class SystemSettingsComponent {}
