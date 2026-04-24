import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-verification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h5>Xác minh danh tính (User Verification)</h5>
      <p>Phê duyệt các yêu cầu cấp tích xanh (Verified Badge).</p>
      <div class="p-4 border-2 border-dashed border-gray-300 border-radius-md">
        <p class="text-muted-foreground italic">
          Logic cần handle:
          <br />1. Hiển thị ảnh CCCD/Hộ chiếu mà người dùng tải lên. <br />2. So khớp khuôn mặt (nếu
          có AI tích hợp). <br />3. Chức năng: Duyệt / Từ chối (kèm lý do).
        </p>
      </div>
    </div>
  `,
})
export class UserVerificationComponent {}
