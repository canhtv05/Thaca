import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-point-system',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h5>Hệ thống điểm thưởng (Gamification)</h5>
      <p>Quản lý cơ chế tích điểm và đổi quà của người dùng.</p>
      <div class="p-4 border-2 border-dashed border-gray-300 border-radius-md">
        <p class="text-muted-foreground italic">
          Logic cần handle:
          <br />1. Point Rules: Cấu hình bao nhiêu điểm cho 1 Like, 1 Post, 1 Follow. <br />2. Point
          History: Truy xuất lịch sử biến động điểm của từng User. <br />3. Handle Logic: Cộng điểm
          thủ công hoặc trừ điểm vi phạm.
        </p>
      </div>
    </div>
  `,
})
export class PointSystemComponent {}
