import { Component, inject, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { DatePickerModule } from 'primeng/datepicker';
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
import { TenantService } from '../../../core/services/tenant.service';
import { TenantDTO } from '../../../core/models/tenant.model';

@Component({
  selector: 'app-tenant',
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
    ThacaInputComponent,
    ThacaDropdownComponent,
    ThacaModalComponent,
  ],
  templateUrl: './tenant.component.html',
})
export class TenantComponent {
  private configService = inject(AppConfigService);
  private tenantService = inject(TenantService);
  private translate = inject(TranslateService);
  private fb = inject(FormBuilder);

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
  });

  statusOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'common.status.active', value: 'ACTIVE' },
    { label: 'common.status.inactive', value: 'INACTIVE' },
  ];

  planTypeOptions: IDropdownOption[] = [
    { label: 'tenant.plans.FREE', value: 'FREE' },
    { label: 'tenant.plans.BASIC', value: 'BASIC' },
    { label: 'tenant.plans.PRO', value: 'PRO' },
    { label: 'tenant.plans.ENTERPRISE', value: 'ENTERPRISE' },
  ];

  tenantForm = this.fb.group({
    id: [null],
    code: ['', [Validators.required]],
    name: ['', [Validators.required]],
    domain: [''],
    status: ['ACTIVE', [Validators.required]],
    planType: ['FREE'],
    expiresAt: [null],
    contactEmail: ['', [Validators.email]],
    logoUrl: [''],
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/cms/tenants/search`,
    rows: 10,
    showStt: true,
    withAudit: true,
    columns: [
      { field: 'code', header: 'tenant.code', sortable: true, width: '150px' },
      { field: 'name', header: 'tenant.name', sortable: true },
      { field: 'domain', header: 'tenant.domain' },
      {
        field: 'planType',
        header: 'tenant.plan_type',
        render: (row: TenantDTO) => {
          if (!row.planType) return '';
          return this.translate.instant(`tenant.plans.${row.planType}`);
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

  onSearch() {
    this.table.refresh(this.filter());
  }

  onCreate() {
    this.tenantForm.reset({ status: 'ACTIVE', planType: 'FREE' });
    this.tenantModal.show();
  }

  handleAction(event: ITableActionEvent) {
    if (event.key === 'edit') {
      this.tenantForm.patchValue({
        ...event.row,
        expiresAt: event.row.expiresAt ? new Date(event.row.expiresAt) : null,
      });
      this.tenantModal.show();
    } else if (event.key === 'delete') {
      if (confirm(this.translate.instant('common.confirm_delete'))) {
        this.tenantService.delete(event.row.id).subscribe(() => {
          this.onSearch();
        });
      }
    }
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
