import { Component } from '@angular/core';

@Component({
  selector: 'app-comment-management',
  standalone: true,
  imports: [],
  template: `
    <div class="card">
      <h5>Quản lý bình luận</h5>
      <p>Kiểm soát các tương tác dưới bài viết.</p>
      <div class="p-4 border-2 border-dashed border-gray-300 border-radius-md">
        <p class="text-muted-foreground italic">
          Logic cần handle:
          <br />1. Load bình luận theo phân cấp (nested comments). <br />2. Lọc bình luận chứa từ
          khóa cấm (Spam/Hate speech). <br />3. Chức năng: Xóa bình luận, Khóa user comment.
        </p>
      </div>
    </div>
  `,
})
export class CommentManagementComponent {}
