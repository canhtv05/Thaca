import { Component, computed, inject, OnInit, signal, ViewChild } from '@angular/core';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import {
  DataTableComponent,
  ITableActionEvent,
  ITableConfig,
} from '../../../shared/components/data-table/data-table.component';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { IUserDTO } from '../user.model';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import {
  IDropdownOption,
  ThacaDropdownComponent,
} from '../../../shared/components/thaca-dropdown/thaca-dropdown.component';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ThacaModalComponent } from '../../../shared/components/thaca-modal/thaca-modal.component';
import { MenuItem } from 'primeng/api';
import { UserService } from '../user.service';
import { IImportResult } from '../../../core/models/common.model';
import { GlobalToast } from '../../../core/global/global-toast';
import { TenantService } from '../../system/tenant/tenant.service';
import { Router } from '@angular/router';
import { ThacaTextareaComponent } from '../../../shared/components/thaca-textarea/thaca-textarea.component';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';
import { Popup } from '../../../core/global/popup-notify';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BreadcrumbComponent,
    ReactiveFormsModule,
    DataTableComponent,
    ThacaInputComponent,
    ThacaDropdownComponent,
    ThacaButtonComponent,
    TranslateModule,
    ThacaModalComponent,
    ThacaTextareaComponent,
    ValidationMessageComponent,
  ],
  templateUrl: './user-list.component.html',
})
export class UserListComponent implements OnInit {
  private configService = inject(AppConfigService);
  private translate = inject(TranslateService);
  private userService = inject(UserService);
  private tenantService = inject(TenantService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  @ViewChild('mainTable') table!: DataTableComponent;
  @ViewChild('createModal') createModal!: ThacaModalComponent;
  @ViewChild('importResultModal') importResultModal!: ThacaModalComponent;
  @ViewChild('reasonModal') reasonModal!: ThacaModalComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-user', label: 'menu.user_management', routerLink: '/user-management/list' },
    { icon: 'pi pi-list', label: 'menu.user_list' },
  ];

  filter = signal({
    username: '',
    email: '',
    isActivated: null,
    isLocked: null,
    tenantId: null,
  });

  importResult = signal<IImportResult | null>(null);
  private selectedRow: any;
  importErrorRows = computed(() => {
    const r = this.importResult();
    if (!r?.errors?.length) return [];
    return r.errors.map((row, i) => ({ ...row, _tid: `e-${i}` }));
  });

  importErrorTableConfig: ITableConfig = {
    columns: [
      { field: 'row', header: 'user.import.error_row', width: '88px' },
      { field: 'column', header: 'user.import.error_column', width: '160px' },
      { field: 'columnKey', header: 'user.import.error_column_key', width: '140px' },
      { field: 'value', header: 'user.import.error_value' },
      { field: 'message', header: 'user.import.error_message' },
    ],
    rows: 10,
    showStt: true,
    withAudit: false,
    dataKey: '_tid',
  };

  statusOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'user.active', value: true },
    { label: 'user.inactive', value: false },
  ];
  tenantOptions = signal<IDropdownOption[]>([]);
  lockedOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'user.safe', value: false },
    { label: 'user.locked', value: true },
  ];

  lockReasonForm = this.fb.group({
    lockReason: ['', [Validators.required]],
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/cms/users/search`,
    rows: 10,
    withAudit: true,
    actionFixed: true,
    showStt: true,
    columns: [
      { field: 'username', header: 'user.username', sortable: true, width: '150px' },
      { field: 'email', header: 'user.email', sortable: true },
      {
        field: 'tenant',
        header: 'Tenant',
        center: true,
        render: (row: IUserDTO) => {
          return row.tenant
            ? `<span class="thaca-badge thaca-badge-primary"><span class="thb-dot"></span>${row.tenant.code} - ${row.tenant.name}</span>`
            : '';
        },
      },
      {
        field: 'isActivated',
        header: 'user.activated',
        render: (row: IUserDTO) => {
          const label = this.translate.instant(
            row.isActivated ? 'common.status.active' : 'common.status.inactive',
          );
          const variant = row.isActivated ? 'success' : 'danger';
          return `<span class="thaca-badge thaca-badge-${variant}">
                    <span class="thb-dot"></span>${label}
                  </span>`;
        },
      },
      {
        field: 'isLocked',
        header: 'user.locked',
        render: (row: IUserDTO) => {
          const label = this.translate.instant(
            row.isLocked ? 'common.status.lock' : 'common.status.unlock',
          );
          const variant = row.isLocked ? 'danger' : 'info';
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
    ],
    actions: [
      {
        icon: 'pi pi-pencil',
        titleKey: 'common.button.update',
        key: 'edit',
        color: 'secondary',
      },
      {
        icon: 'pi pi-key',
        key: 'lock',
        titleKey: 'common.button.lock',
        color: 'danger',
        condition: (row: IUserDTO) => !row.isLocked,
      },
      {
        icon: 'pi pi-unlock',
        key: 'unlock',
        titleKey: 'common.button.unlock',
        color: 'success',
        condition: (row: IUserDTO) => row.isLocked ?? false,
      },
      {
        icon: 'pi pi-eye',
        titleKey: 'common.button.view',
        key: 'view',
        color: 'primary',
      },
      {
        icon: 'pi pi-lock',
        titleKey: 'menu.lock_history',
        key: 'view_lock_history',
        color: 'primary',
      },
      {
        icon: 'pi pi-clock',
        titleKey: 'menu.login_history',
        key: 'view_login_history',
        color: 'primary',
      },
    ],
  };

  async ngOnInit(): Promise<void> {
    const tenants = await this.tenantService.getAll();
    if (tenants.body.status === 'OK') {
      this.tenantOptions.set(
        tenants.body.data.map((t) => ({
          label: `${t.code} - ${t.name}`,
          value: t.id,
        })),
      );
    }
  }

  onSearch() {
    this.table.refresh(this.filter());
  }

  handleAction(event: ITableActionEvent) {
    const { key } = event;

    switch (key) {
      case 'view':
        this.router.navigate(['/user-management/users', event.row?.username]);
        break;
      case 'view_login_history':
        this.router.navigate(['/user-management/users', event.row?.username, 'login-history']);
        break;
      case 'lock':
      case 'unlock': {
        this.selectedRow = event.row;
        this.lockReasonForm.reset();
        this.reasonModal.show();
        break;
      }
      case 'view_lock_history':
        this.router.navigate(['/user-management/users', event.row?.id, 'lock-history']);
        break;
    }
  }

  async confirmLockUnlock() {
    const isLocked = this.selectedRow.isLocked;
    const confirmed = await Popup.confirm({
      title: isLocked ? 'user.popup.unlock.title' : 'user.popup.lock.title',
      message: isLocked ? 'user.popup.unlock.message' : 'user.popup.lock.message',
      acceptText: isLocked ? 'common.button.unlock' : 'common.button.lock',
      cancelText: 'common.button.cancel',
    });
    if (!confirmed) return;

    const res = await this.userService.lockUnlock({
      ...this.selectedRow,
      lockReason: this.lockReasonForm.get('lockReason')?.value,
    });
    if (res.body.status === 'OK') {
      GlobalToast.success(
        'user.toast.lock_unlock.messageSuccess',
        'user.toast.lock_unlock.titleSuccess',
      );
      this.onSearch();
      this.reasonModal.hide();
    }
  }

  onCreate() {
    this.createModal.show();
  }

  async downloadTemplate() {
    await this.userService.downloadTemplate();
  }

  onImportClick(fileInput: HTMLInputElement) {
    fileInput.click();
  }

  async onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    input.value = '';
    const res = await this.userService.importUsers(file);
    if (res.body.status === 'OK') {
      const data = res.body.data as IImportResult;
      if (!data) {
        GlobalToast.error('user.import.error_unknown', 'common.error');
        return;
      }
      this.importResult.set(data);
      if (data.hasErrors) {
        const msg = this.translate.instant('user.import.has_errors', {
          errorCount: data.errorCount,
          successCount: data.successCount,
        });
        const title = this.translate.instant('user.import.partial_success');
        GlobalToast.warnPlain(msg, title);
        this.importResultModal?.show();
      } else {
        const msg = this.translate.instant('user.import.success', { count: data.successCount });
        const title = this.translate.instant('common.success');
        GlobalToast.successPlain(msg, title);
      }
    }
  }

  async downloadImportErrorExcel() {
    const data = this.importResult();
    if (!data?.errors?.length) return;
    try {
      await this.userService.downloadFileError(data);
      GlobalToast.successPlain(
        this.translate.instant('user.import.export_error_done'),
        this.translate.instant('common.success'),
      );
    } catch {
      GlobalToast.errorPlain(
        this.translate.instant('user.import.export_error_failed'),
        this.translate.instant('common.error'),
      );
    }
  }

  closeImportResult() {
    this.importResult.set(null);
    this.importResultModal?.hide();
  }
}
