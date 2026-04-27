import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { FileUploadModule, FileUploadHandlerEvent } from 'primeng/fileupload';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ExcelService } from '../../../core/services/excel.service';

@Component({
  selector: 'app-excel-test',
  standalone: true,
  imports: [CommonModule, ButtonModule, FileUploadModule, CardModule, ToastModule],
  providers: [MessageService],
  template: `
    <div class="p-6">
      <h1 class="text-2xl font-bold mb-6">Excel Engine Test Portal</h1>
      <p-toast></p-toast>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Template & Export Section -->
        <p-card header="Downloads" subheader="Test Template and Data Export">
          <div class="flex flex-col gap-4">
            <p-button
              label="Download Template"
              icon="pi pi-download"
              styleClass="p-button-outlined"
              (onClick)="downloadTemplate()"
            >
            </p-button>
            <p-button
              label="Export Sample Data (50 rows)"
              icon="pi pi-file-excel"
              styleClass="p-button-success"
              (onClick)="exportData()"
            >
            </p-button>
          </div>
        </p-card>

        <!-- Import Section -->
        <p-card header="Import" subheader="Test File Security & Validation">
          <p-fileUpload
            mode="advanced"
            accept=".xlsx"
            [maxFileSize]="1000000"
            [customUpload]="true"
            (uploadHandler)="handleUpload($event)"
            chooseLabel="Choose XLSX"
            uploadLabel="Upload & Validate"
            [auto]="false"
          >
            <ng-template pTemplate="content">
              <ul
                *ngIf="importResults"
                class="mt-4 p-4 bg-gray-50 rounded border text-sm max-h-60 overflow-auto"
              >
                <li class="font-bold text-blue-600 mb-2">
                  Result: {{ importResults.successCount }} success,
                  {{ importResults.errorCount }} errors
                </li>
                <li *ngFor="let err of importResults.errors" class="text-red-500 mb-1">
                  Row {{ err.row }}: [{{ err.column }}] {{ err.message }} (Value: {{ err.value }})
                </li>
                <li *ngIf="importResults.preview?.length > 0" class="mt-2 text-gray-500 italic">
                  Preview (First 5 rows):
                  <pre class="text-[10px]">{{ importResults.preview | json }}</pre>
                </li>
              </ul>
            </ng-template>
          </p-fileUpload>
          <div class="mt-2 text-xs text-gray-400">
            * Remember: File name must start with "thaca-"
          </div>
        </p-card>
      </div>
    </div>
  `,
  styles: [
    `
      :host ::ng-deep .p-fileupload-content {
        min-height: 10rem;
      }
    `,
  ],
})
export class ExcelTestComponent {
  private readonly excelService = inject(ExcelService);
  private readonly messageService = inject(MessageService);

  importResults: any = null;

  async downloadTemplate() {
    try {
      await this.excelService.downloadTemplate();
      this.messageService.add({
        severity: 'success',
        summary: 'Success',
        detail: 'Template downloaded',
      });
    } catch (err) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to download template',
      });
    }
  }

  async exportData() {
    try {
      await this.excelService.exportData();
      this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Data exported' });
    } catch (err) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to export data',
      });
    }
  }

  async handleUpload(event: FileUploadHandlerEvent) {
    const file = event.files[0];

    try {
      // Gọi service thực hiện import
      const res = await this.excelService.importFile(file);

      // Theo cấu trúc ApiPayload, dữ liệu thực tế nằm trong res.body.data
      const data = res.body?.data;
      this.importResults = data;

      this.messageService.add({
        severity: data.hasErrors ? 'warn' : 'success',
        summary: 'Import Complete',
        detail: `Processed ${data.totalRows} rows.`,
      });
    } catch (err: any) {
      // Xử lý lỗi từ backend (đã qua interceptor)
      const errorData = err.data?.body?.data || err.data;
      this.importResults = errorData;
      this.messageService.add({
        severity: 'error',
        summary: 'Import Failed',
        detail: errorData?.message || 'Check file security constraints',
      });
    }
  }
}
