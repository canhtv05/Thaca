import { Component, inject, signal, computed, effect, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { DatePickerModule } from 'primeng/datepicker';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { MailConfigService, ITenant } from './mail-config.service';
import { IMailConfigDTO } from './mail-config.model';
import { MenuItem } from 'primeng/api';
import { Router } from '@angular/router';
import {
  DataTableComponent,
  ITableConfig,
  ITableActionEvent,
} from '../../../shared/components/data-table/data-table.component';
import { GlobalToast } from '../../../core/global/global-toast';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { ThacaTabsComponent } from '../../../shared/components/thaca-tabs/thaca-tabs.component';
import { ThacaTabComponent } from '../../../shared/components/thaca-tabs/thaca-tab.component';
import { ThacaModalComponent } from '../../../shared/components/thaca-modal/thaca-modal.component';

@Component({
  selector: 'app-mail-config',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    DatePickerModule,
    BreadcrumbComponent,
    DataTableComponent,
    ThacaButtonComponent,
    ThacaTabsComponent,
    ThacaTabComponent,
    ThacaModalComponent,
  ],
  templateUrl: './mail-config.component.html',
})
export class MailConfigComponent implements OnInit {
  private configService = inject(AppConfigService);
  private router = inject(Router);
  private mailConfigService = inject(MailConfigService);
  private fb = inject(FormBuilder);
  private translate = inject(TranslateService);

  // State signals
  activeTab = signal<'system' | 'tenant'>('system');
  selectedTenantId = signal<string | null>(null);
  tenantList = signal<ITenant[]>([]);
  systemConfig = signal<IMailConfigDTO | null>(null);
  tenantConfig = signal<IMailConfigDTO | null>(null);
  loading = signal<boolean>(false);
  testingConnection = signal<boolean>(false);
  showConfigForm = signal<boolean>(false);
  editingConfig = signal<IMailConfigDTO | null>(null);
  testResult = signal<{ success: boolean; message: string } | null>(null);

  @ViewChild('configModal') configModal!: ThacaModalComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-envelope', label: 'menu.mail_config' },
  ];

  @ViewChild(DataTableComponent) table!: DataTableComponent;

  filter = signal({
    code: '',
    name: '',
    status: null,
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/admin/tenants/search`,
    rows: 10,
    showStt: true,
    columns: [
      { field: 'code', header: 'tenant.code', sortable: true, width: '150px' },
      { field: 'name', header: 'tenant.name', sortable: true },
      { field: 'domain', header: 'tenant.domain' },
      {
        field: 'status',
        header: 'tenant.status',
        render: (row: any) => {
          const variant = row.status === 'ACTIVE' ? 'success' : 'warning';
          const label = this.translate.instant(`common.status.${row.status.toLowerCase()}`);
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
    ],
    actions: [
      {
        icon: 'pi pi-cog',
        key: 'config',
        titleKey: 'mail_config.button.config',
      },
    ],
  };

  configForm!: FormGroup;

  ngOnInit() {
    this.initializeForm();
    this.loadSystemConfig();
    this.loadTenantList();
  }

  private initializeForm() {
    this.configForm = this.fb.group({
      id: [null],
      host: ['', [Validators.required]],
      port: ['587', [Validators.required]],
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
      fromName: [''],
      fromEmail: ['', [Validators.email]],
      isAuth: [true],
      isStarttls: [true],
      isDefault: [false],
      configCode: [''],
      description: [''],
      tenantId: [null],
    });
  }

  async loadSystemConfig() {
    this.loading.set(true);
    try {
      const result = await this.mailConfigService.search({
        page: { page: 1, size: 1 } as any,
        filter: { tenantId: null } as unknown as IMailConfigDTO,
      });
      if (result.body?.data && result.body.data.length > 0) {
        this.systemConfig.set(result.body.data[0]);
      }
    } catch (err) {
      console.error('Error loading system config:', err);
    } finally {
      this.loading.set(false);
    }
  }

  async loadTenantList() {
    // Left empty as we now use DataTableComponent
  }

  async loadTenantConfig(tenantId: string) {
    this.loading.set(true);
    try {
      const result = await this.mailConfigService.search({
        page: { page: 1, size: 10 } as any,
        filter: { tenantId } as unknown as IMailConfigDTO,
      });
      if (result.body?.data && result.body.data.length > 0) {
        this.tenantConfig.set(result.body.data[0]);
      } else {
        this.tenantConfig.set(null);
      }
    } catch (err) {
      console.error('Error loading tenant config:', err);
      this.tenantConfig.set(null);
    } finally {
      this.loading.set(false);
    }
  }

  onTabChange(tab: 'system' | 'tenant') {
    this.activeTab.set(tab);
    this.resetFormState();
    if (tab === 'system' && !this.systemConfig()) {
      this.loadSystemConfig();
    }
  }

  onSearch() {
    this.table?.refresh(this.filter());
  }

  handleAction(event: ITableActionEvent) {
    if (event.key === 'config') {
      const tenantId = event.row.id?.toString() || null;
      this.selectedTenantId.set(tenantId);
      this.resetFormState();
      if (tenantId) {
        this.loadTenantConfig(tenantId);
      }
    }
  }

  backToTenantList() {
    this.selectedTenantId.set(null);
    this.tenantConfig.set(null);
    this.resetFormState();
  }

  openCreateForm() {
    this.editingConfig.set(null);
    this.configForm.reset({
      port: 587,
      isAuth: true,
      isStarttls: true,
      isDefault: false,
      tenantId: this.activeTab() === 'tenant' ? this.selectedTenantId() : null,
    });
    this.showConfigForm.set(true);
    this.configModal?.show();
  }

  openEditForm(config: IMailConfigDTO) {
    this.editingConfig.set(config);
    this.configForm.patchValue({
      id: config.id,
      host: config.host,
      port: config.port,
      username: config.username,
      password: '', // Don't auto-fill password for security
      fromName: config.fromName || '',
      fromEmail: config.fromEmail || '',
      isAuth: config.isAuth !== false,
      isStarttls: config.isStarttls !== false,
      isDefault: config.isDefault || false,
      configCode: config.configCode || '',
      description: config.description || '',
      tenantId: config.tenantId,
    });
    this.showConfigForm.set(true);
    this.configModal?.show();
  }

  async saveConfig() {
    if (this.configForm.invalid) {
      GlobalToast.error(this.translate.instant('common.error.validation_failed'));
      return;
    }

    this.loading.set(true);
    try {
      const formValue = this.configForm.value;
      const isEdit = this.editingConfig() !== null;

      const payload: IMailConfigDTO = {
        ...formValue,
        status: 'ACTIVE',
      };

      if (isEdit) {
        await this.mailConfigService.update(payload);
        GlobalToast.success(this.translate.instant('common.message.update_success'));
      } else {
        await this.mailConfigService.create(payload);
        GlobalToast.success(this.translate.instant('common.message.create_success'));
      }

      this.closeForm();
      if (this.activeTab() === 'system') {
        this.loadSystemConfig();
      } else if (this.selectedTenantId()) {
        this.loadTenantConfig(this.selectedTenantId()!);
      }
    } catch (err: any) {
      const errorMsg = err?.body?.message || 'Error saving config';
      GlobalToast.error(errorMsg);
    } finally {
      this.loading.set(false);
    }
  }

  async deleteConfig(config: IMailConfigDTO) {
    if (!confirm(this.translate.instant('common.message.confirm_delete'))) {
      return;
    }

    this.loading.set(true);
    try {
      await this.mailConfigService.delete(config.id!);
      GlobalToast.success(this.translate.instant('common.message.delete_success'));

      if (this.activeTab() === 'system') {
        this.loadSystemConfig();
      } else if (this.selectedTenantId()) {
        this.loadTenantConfig(this.selectedTenantId()!);
      }
    } catch (err: any) {
      const errorMsg = err?.body?.message || 'Error deleting config';
      GlobalToast.error(errorMsg);
    } finally {
      this.loading.set(false);
    }
  }

  async testConnection() {
    if (this.configForm.get('host')?.invalid || this.configForm.get('port')?.invalid) {
      GlobalToast.error(this.translate.instant('mail_config.error.invalid_host_port'));
      return;
    }

    this.testingConnection.set(true);
    this.testResult.set(null);

    try {
      const testConfig = {
        host: this.configForm.get('host')?.value,
        port: this.configForm.get('port')?.value,
        username: this.configForm.get('username')?.value,
        password: this.configForm.get('password')?.value,
        isAuth: this.configForm.get('isAuth')?.value,
        isStarttls: this.configForm.get('isStarttls')?.value,
      };

      const result = await this.mailConfigService.testConnection(testConfig);
      this.testResult.set(result.body?.data || { success: false, message: 'Unknown error' });

      if (result.body?.data?.success) {
        GlobalToast.success(result.body.data.message);
      } else {
        GlobalToast.error(result.body?.data?.message || 'Connection failed');
      }
    } catch (err: any) {
      const message = err?.body?.data?.message || err?.body?.message || 'Connection test failed';
      this.testResult.set({ success: false, message });
      GlobalToast.error(message);
    } finally {
      this.testingConnection.set(false);
    }
  }

  async resetToDefault() {
    if (!this.tenantConfig()) return;

    if (!confirm(this.translate.instant('mail_config.message.confirm_reset_to_default'))) {
      return;
    }

    try {
      await this.mailConfigService.delete(this.tenantConfig()!.id!);
      GlobalToast.success(this.translate.instant('mail_config.message.reset_to_default_success'));
      this.tenantConfig.set(null);
      this.closeForm();
    } catch (err: any) {
      const errorMsg = err?.body?.message || 'Error resetting to default';
      GlobalToast.error(errorMsg);
    }
  }

  closeForm() {
    this.configModal?.hide();
    this.showConfigForm.set(false);
    this.editingConfig.set(null);
    this.testResult.set(null);
    this.configForm.reset({
      port: 587,
      isAuth: true,
      isStarttls: true,
      isDefault: false,
    });
  }

  private resetFormState() {
    this.closeForm();
  }

  // Helper getters
  get currentConfig() {
    return this.activeTab() === 'system' ? this.systemConfig() : this.tenantConfig();
  }

  get isSystemTab() {
    return this.activeTab() === 'system';
  }

  get isTenantTab() {
    return this.activeTab() === 'tenant';
  }

  get selectedTenant() {
    return this.tenantList().find((t) => t.id?.toString() === this.selectedTenantId());
  }

  get showNoConfigBadge() {
    return this.isTenantTab && this.selectedTenantId() && !this.tenantConfig();
  }

  get showConfigCard() {
    return (
      (this.isSystemTab && this.systemConfig()) ||
      (this.isTenantTab &&
        this.selectedTenantId() &&
        (this.tenantConfig() || this.showNoConfigBadge))
    );
  }

  get canShowCreateForm() {
    return this.showConfigForm() && !this.editingConfig();
  }

  get canShowEditForm() {
    return this.showConfigForm() && this.editingConfig();
  }
}
