import { Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import {
  DataTableComponent,
  ITableActionEvent,
  ITableConfig,
} from '../../../shared/components/data-table/data-table.component';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import {
  IDropdownOption,
  ThacaDropdownComponent,
} from '../../../shared/components/thaca-dropdown/thaca-dropdown.component';
import { ThacaModalComponent } from '../../../shared/components/thaca-modal/thaca-modal.component';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { TenantService } from './tenant.service';
import { ITenantDTO } from './tenant.model';
import { PlanService } from '../plan/plan.service';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';
import { ThacaDatepickerComponent } from '../../../shared/components/thaca-datepicker/thaca-datepicker.component';
import { ThacaTextareaComponent } from '../../../shared/components/thaca-textarea/thaca-textarea.component';
import { Popup } from '../../../core/global/popup-notify';
import { GlobalToast } from '../../../core/global/global-toast';
import { isLoading } from '../../../core/stores/app.store';
import { CommonUtils } from '../../../shared/utils/common.utils';
import { Router } from '@angular/router';

@Component({
  selector: 'app-tenant',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    BreadcrumbComponent,
    DataTableComponent,
    ThacaButtonComponent,
    ThacaInputComponent,
    ThacaDropdownComponent,
    ThacaModalComponent,
    ValidationMessageComponent,
    ThacaDatepickerComponent,
    ThacaTextareaComponent,
  ],
  templateUrl: './tenant.component.html',
})
export class TenantComponent implements OnInit {
  private configService = inject(AppConfigService);
  private tenantService = inject(TenantService);
  private translate = inject(TranslateService);
  private fb = inject(FormBuilder);
  private planService = inject(PlanService);
  private router = inject(Router);
  readonly isLoading = isLoading;

  private originalValue: any;
  minDate = new Date();
  planOptions = signal<IDropdownOption[]>([]);

  @ViewChild(DataTableComponent) table!: DataTableComponent;
  @ViewChild('tenantModal') tenantModal!: ThacaModalComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-building', label: 'menu.tenant_management' },
  ];

  filter = signal({
    code: '',
    name: '',
    status: null,
    planId: null,
  });

  statusOptions: IDropdownOption[] = [
    { label: 'common.status.all', value: null },
    { label: 'common.status.active', value: 'ACTIVE' },
    { label: 'common.status.suspended', value: 'SUSPENDED' },
    { label: 'common.status.inactive', value: 'INACTIVE' },
  ];

  tenantForm = this.fb.group({
    id: [null],
    code: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9-_]+$/)]],
    name: ['', [Validators.required]],
    domain: [''],
    status: [{ value: 'ACTIVE', disabled: true }, [Validators.required]],
    planId: [null, [Validators.required]],
    expiresAt: [null],
    contactEmail: ['', [Validators.email]],
    logoUrl: [''],
    version: [null],
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/cms/tenants/search`,
    rows: 10,
    showStt: true,
    withAudit: true,
    actionFixed: true,
    columns: [
      { field: 'code', header: 'tenant.code', sortable: true, width: '150px' },
      { field: 'name', header: 'tenant.name', sortable: true },
      { field: 'domain', header: 'tenant.domain' },
      { field: 'contactEmail', header: 'tenant.contact_email' },
      {
        field: 'logoUrl',
        header: 'Logo',
        center: true,
        render: (row: ITenantDTO) => {
          if (!row?.logoUrl) return '';
          return `
                <div class="flex flex-col items-center justify-center gap-1">
                  <div class="w-10 h-10 overflow-hidden rounded border border-gray-200 bg-gray-50">
                    <img
                      src="${row.logoUrl}"
                      alt="Logo"
                      class="w-full h-full object-cover"
                      onerror="this.style.display='none'"
                    />
                  </div>
                  <span class="text-[10px] text-foreground break-all text-center min-w-[150px]">
                    ${row.logoUrl}
                  </span>
                </div>
              `;
        },
      },
      {
        field: 'plan.name',
        header: 'tenant.plan',
        render: (row: ITenantDTO) => {
          if (!row.plan) return '';
          return row.plan.name;
        },
      },
      {
        field: 'expiresAt',
        header: 'tenant.expires_at',
        render: (row: ITenantDTO) => {
          if (!row.expiresAt) return this.translate.instant('common.infinite');
          return row.expiresAt;
        },
      },
      {
        field: 'status',
        header: 'tenant.status',
        render: (row: ITenantDTO) => {
          const variant = row.status === 'ACTIVE' ? 'success' : 'warning';
          const label = this.translate.instant(`common.status.${row.status.toLowerCase()}`);
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
      {
        field: 'version',
        header: 'tenant.version',
      },
    ],
    actions: [
      {
        icon: 'pi pi-pencil',
        key: 'update',
        titleKey: 'common.button.update',
        condition: (row: ITenantDTO) => row.status === 'ACTIVE',
      },
      {
        icon: 'pi pi-eye',
        key: 'view',
        titleKey: 'common.button.view',
      },
      {
        icon: 'pi pi-key',
        key: 'lock',
        titleKey: 'common.button.lock',
        color: 'info',
        condition: (row: ITenantDTO) => row.status === 'ACTIVE',
      },
      {
        icon: 'pi pi-unlock',
        key: 'unlock',
        titleKey: 'common.button.unlock',
        color: 'info',
        condition: (row: ITenantDTO) => row.status === 'INACTIVE',
      },
    ],
  };

  async ngOnInit(): Promise<void> {
    const plans = await this.planService.getAll();
    if (plans.body.status === 'OK') {
      this.planOptions.set(
        plans.body.data.map((plan) => ({
          label: plan.name,
          value: plan.id,
        })),
      );
    }
  }

  onSearch() {
    this.table.refresh(this.filter());
  }

  onCreate() {
    this.tenantForm.reset({ status: 'ACTIVE' });
    this.tenantForm.get('code')?.enable();
    this.originalValue = this.tenantForm.getRawValue();
    this.tenantModal.show();
  }

  handleAction(event: ITableActionEvent) {
    if (event.key === 'update') {
      this.tenantForm.patchValue({
        ...event.row,
        expiresAt: CommonUtils.parseBackendDate(event.row.expiresAt),
      });
      this.tenantForm.get('code')?.disable();
      this.originalValue = this.tenantForm.getRawValue();
      this.tenantModal.show();
    } else if (event.key === 'lock' || event.key === 'unlock') {
      Popup.confirm({
        title: event.key === 'lock' ? 'tenant.popup.lock.title' : 'tenant.popup.unlock.title',
        message: event.key === 'lock' ? 'tenant.popup.lock.message' : 'tenant.popup.unlock.message',
        acceptText: event.key === 'lock' ? 'common.button.lock' : 'common.button.unlock',
        cancelText: 'common.button.cancel',
      }).then(async (result: boolean) => {
        if (result) {
          if (event.key === 'lock' || event.key === 'unlock') {
            const req = { ...event.row, status: event.key === 'lock' ? 'INACTIVE' : 'ACTIVE' };
            const res = await this.tenantService.lockUnlock(req);
            if (res.body.status === 'OK') {
              const messageKey =
                event.key === 'lock'
                  ? 'tenant.toast.lock.messageSuccess'
                  : 'tenant.toast.unlock.messageSuccess';
              const titleKey =
                event.key === 'lock'
                  ? 'tenant.toast.lock.titleSuccess'
                  : 'tenant.toast.unlock.titleSuccess';
              GlobalToast.success(messageKey, titleKey);
              this.onSearch();
            }
          }
        }
      });
    } else if (event.key === 'view') {
      this.router.navigate(['/system/tenants', event.row.code]);
    }
  }

  isUnchanged(): boolean {
    return (
      JSON.stringify(this.tenantForm.getRawValue()) === JSON.stringify(this.originalValue) ||
      this.tenantForm.invalid
    );
  }

  async onSubmit() {
    if (this.isUnchanged()) return;
    const req = this.tenantForm.getRawValue() as any;
    if (req.expiresAt) {
      req.expiresAt = CommonUtils.formatDate(req.expiresAt);
    }

    const isUpdate = !!this.tenantForm.value.id;
    const confirmed = await Popup.confirm({
      title: isUpdate ? 'tenant.popup.update.title' : 'tenant.popup.create.title',
      message: isUpdate ? 'tenant.popup.update.message' : 'tenant.popup.create.message',
      acceptText: isUpdate ? 'common.button.update' : 'common.button.create',
      cancelText: 'common.button.cancel',
    });
    if (!confirmed) return;
    const res = isUpdate
      ? await this.tenantService.update(req)
      : await this.tenantService.create(req);
    if (res.body.status === 'OK') {
      GlobalToast.success(
        isUpdate ? 'tenant.toast.update.messageSuccess' : 'tenant.toast.create.messageSuccess',
        isUpdate ? 'tenant.toast.update.titleSuccess' : 'tenant.toast.create.titleSuccess',
      );
      this.onSearch();
      this.tenantModal.hide();
    }
  }

  async onExport(): Promise<void> {
    await this.tenantService.exportData(this.table.getSearchRequest());
  }
}
