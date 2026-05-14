import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-post-management',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h5>Quản lý bài viết (Social Posts)</h5>
      <p>Trang này dùng để liệt kê và kiểm soát tất cả bài viết trên mạng xã hội.</p>
      <div class="p-4 border-2 border-dashed border-gray-300 border-radius-md">
        <p class="text-muted-foreground italic">
          Logic cần handle:
          <br />1. Load danh sách bài viết từ Post-Service (Microservice). <br />2. Cho phép xem chi
          tiết nội dung, media (ảnh/video) đính kèm. <br />3. Handle Logic: Ẩn bài viết, Xóa bài
          viết (Soft delete), hoặc Đánh dấu vi phạm. <br />4. Tích hợp Filter theo User, thời gian,
          và Hashtag.
        </p>
      </div>
    </div>
  `,
})
export class PostManagementComponent {}
