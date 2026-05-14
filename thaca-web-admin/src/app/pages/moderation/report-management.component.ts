import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-report-management',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h5>Quản lý báo cáo (Reports)</h5>
      <p>Xử lý các khiếu nại từ người dùng về nội dung hoặc tài khoản khác.</p>
      <div class="p-4 border-2 border-dashed border-gray-300 border-radius-md">
        <p class="text-muted-foreground italic">
          Logic cần handle:
          <br />1. Phân loại báo cáo: Bài viết, User, Bình luận. <br />2. Quy trình: Tiếp nhận ->
          Đang xử lý -> Đã giải quyết -> Từ chối. <br />3. Tự động khóa nội dung nếu đạt ngưỡng báo
          cáo (Threshold).
        </p>
      </div>
    </div>
  `,
})
export class ReportManagementComponent {}
