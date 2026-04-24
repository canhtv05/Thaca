import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h5>Phân tích số liệu (Social Analytics)</h5>
      <p>Theo dõi hiệu suất và tăng trưởng của nền tảng.</p>
      <div class="p-4 border-2 border-dashed border-primary/30 border-radius-md bg-primary/5">
        <p class="text-sm font-semibold mb-2">Logic nghiệp vụ cần xử lý:</p>
        <ul class="text-muted-foreground text-sm space-y-1">
          <li>• Tích hợp Chart.js hoặc PrimeNG Charts để vẽ biểu đồ tăng trưởng User.</li>
          <li>• Handle Real-time data: Hiển thị số lượng user đang Online (từ Socket/Redis).</li>
          <li>• Thống kê "Engagement Rate" (Like/Comment/Share) theo từng khung giờ.</li>
          <li>• Xuất báo cáo (Export PDF/Excel) cho ban quản trị.</li>
        </ul>
      </div>
    </div>
  `,
})
export class AnalyticsComponent {}
