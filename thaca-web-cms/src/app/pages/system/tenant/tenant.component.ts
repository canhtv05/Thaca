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
import { TenantDTO } from '../../../core/models/tenant.model';
import { PlanService } from '../plan/plan.service';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';
import { ThacaDatepickerComponent } from '../../../shared/components/thaca-datepicker/thaca-datepicker.component';
import { ThacaTextareaComponent } from '../../../shared/components/thaca-textarea/thaca-textarea.component';
import { Popup } from '../../../core/global/popup-notify';
import { GlobalToast } from '../../../core/global/global-toast';
import { isLoading } from '../../../core/stores/app.store';

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
  readonly isLoading = isLoading;

  private originalValue: any;
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
    code: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9]+$/)]],
    name: ['', [Validators.required]],
    domain: [''],
    status: [{ value: 'ACTIVE', disabled: true }, [Validators.required]],
    planId: [null, [Validators.required]],
    expiresAt: [null],
    contactEmail: ['', [Validators.email]],
    logoUrl: [''],
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
      {
        field: 'plan.name',
        header: 'tenant.plan',
        render: (row: TenantDTO) => {
          if (!row.plan) return '';
          return row.plan.name;
        },
      },
      {
        field: 'expiresAt',
        header: 'tenant.expires_at',
        render: (row: TenantDTO) => {
          if (!row.expiresAt) return this.translate.instant('common.infinite');
          return row.expiresAt;
        },
      },
      {
        field: 'status',
        header: 'tenant.status',
        render: (row: TenantDTO) => {
          const variant = row.status === 'ACTIVE' ? 'success' : 'warning';
          const label = this.translate.instant(`common.status.${row.status.toLowerCase()}`);
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
    ],
    actions: [
      { icon: 'pi pi-pencil', key: 'edit', titleKey: 'tenant.actions.edit' },
      { icon: 'pi pi-trash', key: 'delete', titleKey: 'tenant.actions.delete', color: 'danger' },
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
    if (event.key === 'edit') {
      this.tenantForm.patchValue({
        ...event.row,
        expiresAt: event.row.expiresAt ? new Date(event.row.expiresAt) : null,
      });
      this.tenantForm.get('code')?.disable();
      this.originalValue = this.tenantForm.getRawValue();
      this.tenantModal.show();
    } else if (event.key === 'delete') {
      Popup.confirm({
        title: 'tenant.popup.delete.title',
        message: 'common.confirm_delete',
        acceptText: 'common.button.delete',
        cancelText: 'common.button.cancel',
      }).then((result) => {
        if (result) {
          this.tenantService.delete(event.row.id).subscribe(() => {
            GlobalToast.success(
              'tenant.toast.delete.messageSuccess',
              'tenant.toast.delete.titleSuccess',
            );
            this.onSearch();
          });
        }
      });
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
    const isUpdate = !!this.tenantForm.value.id;
    const confirmed = await Popup.confirm({
      title: isUpdate ? 'tenant.popup.update.title' : 'tenant.popup.create.title',
      message: isUpdate ? 'tenant.popup.update.message' : 'tenant.popup.create.message',
      acceptText: isUpdate ? 'common.button.update' : 'common.button.create',
      cancelText: 'common.button.cancel',
    });
    if (!confirmed) return;

    const data = this.tenantForm.getRawValue() as any;
    this.tenantService.save(data).subscribe(() => {
      GlobalToast.success(
        isUpdate ? 'tenant.toast.update.messageSuccess' : 'tenant.toast.create.messageSuccess',
        isUpdate ? 'tenant.toast.update.titleSuccess' : 'tenant.toast.create.titleSuccess',
      );
      this.tenantModal.hide();
      this.onSearch();
    });
  }

  onSave() {
    if (this.tenantForm.invalid) return;
    const data = this.tenantForm.value as any;
    this.tenantService.save(data).subscribe(() => {
      this.tenantModal.hide();
      this.onSearch();
    });
  }
}
