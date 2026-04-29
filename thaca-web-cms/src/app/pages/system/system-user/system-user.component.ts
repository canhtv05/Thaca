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
import { SystemUserService } from './system-user.service';
import { ISystemUserDTO } from './system-user.model';
import { RoleService } from '../role/role.service';
import { TenantService } from '../tenant/tenant.service';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';
import { Popup } from '../../../core/global/popup-notify';
import { GlobalToast } from '../../../core/global/global-toast';
import { isLoading } from '../../../core/stores/app.store';

@Component({
  selector: 'app-system-user',
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
  ],
  templateUrl: './system-user.component.html',
})
export class SystemUserComponent implements OnInit {
  private configService = inject(AppConfigService);
  private systemUserService = inject(SystemUserService);
  private roleService = inject(RoleService);
  private tenantService = inject(TenantService);
  private translate = inject(TranslateService);
  private fb = inject(FormBuilder);
  readonly isLoading = isLoading;

  private originalValue: any;
  roleOptions = signal<IDropdownOption[]>([]);
  tenantOptions = signal<IDropdownOption[]>([]);
  lockReason = '';
  private selectedRow: any;

  @ViewChild(DataTableComponent) table!: DataTableComponent;
  @ViewChild('userModal') userModal!: ThacaModalComponent;
  @ViewChild('reasonModal') reasonModal!: ThacaModalComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-users', label: 'menu.system_user_management' },
  ];

  filter = signal({
    username: '',
    email: '',
    fullname: '',
    isActivated: null,
    isLocked: null,
  });

  activatedOptions: IDropdownOption[] = [
    { label: 'common.status.all', value: null },
    { label: 'common.status.activated', value: true },
    { label: 'common.status.not_activated', value: false },
  ];

  lockedOptions: IDropdownOption[] = [
    { label: 'common.status.all', value: null },
    { label: 'common.status.locked', value: true },
    { label: 'common.status.unlocked', value: false },
  ];

  userForm = this.fb.group({
    id: [null],
    username: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    fullname: ['', [Validators.required]],
    password: [''],
    tenantId: [null],
    isActivated: [true],
    isLocked: [false],
    isSuperAdmin: [false],
    roleCodes: [[] as string[]],
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/cms/system-users/search`,
    rows: 10,
    showStt: true,
    withAudit: true,
    actionFixed: true,
    columns: [
      { field: 'username', header: 'system_user.username', sortable: true },
      { field: 'fullname', header: 'system_user.fullname', sortable: true },
      { field: 'email', header: 'system_user.email', sortable: true },
      {
        field: 'isActivated',
        header: 'system_user.is_activated',
        render: (row: ISystemUserDTO) => {
          const variant = row.isActivated ? 'success' : 'danger';
          const label = this.translate.instant(
            row.isActivated ? 'common.status.activated' : 'common.status.not_activated',
          );
          return `<span class="thaca-badge thaca-badge-${variant}">${label}</span>`;
        },
      },
      {
        field: 'isLocked',
        header: 'system_user.is_locked',
        render: (row: ISystemUserDTO) => {
          const variant = row.isLocked ? 'danger' : 'success';
          const label = this.translate.instant(
            row.isLocked ? 'common.status.locked' : 'common.status.unlocked',
          );
          return `<span class="thaca-badge thaca-badge-${variant}">${label}</span>`;
        },
      },
      {
        field: 'isSuperAdmin',
        header: 'system_user.is_super_admin',
        render: (row: ISystemUserDTO) => {
          return row.isSuperAdmin ? `<i class="pi pi-check-circle text-primary"></i>` : '';
        },
      },
    ],
    actions: [
      {
        icon: 'pi pi-pencil',
        key: 'edit',
        titleKey: 'common.button.update',
      },
      {
        icon: 'pi pi-key',
        key: 'lock-unlock',
        titleKey: 'common.button.lock_unlock',
        color: 'info',
      },
    ],
  };

  async ngOnInit(): Promise<void> {
    this.loadRoles();
    this.loadTenants();
  }

  async loadRoles() {
    const res = await this.roleService.searchRoles({ page: { pageNumber: 0, pageSize: 100 } });
    if (res.body.status === 'OK') {
      this.roleOptions.set(
        res.body.data.content.map((role: any) => ({
          label: role.code,
          value: role.code,
        })),
      );
    }
  }

  async loadTenants() {
    const res = await this.tenantService.getAll();
    if (res.body.status === 'OK') {
      this.tenantOptions.set(
        res.body.data.map((tenant: any) => ({
          label: tenant.name,
          value: tenant.id,
        })),
      );
    }
  }

  onSearch() {
    this.table.refresh(this.filter());
  }

  onCreate() {
    this.userForm.reset({ isActivated: true, isLocked: false, isSuperAdmin: false, roleCodes: [] });
    this.userForm.get('username')?.enable();
    this.userForm.get('password')?.setValidators([Validators.required]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.originalValue = this.userForm.getRawValue();
    this.userModal.show();
  }

  handleAction(event: ITableActionEvent) {
    if (event.key === 'edit') {
      this.userForm.patchValue({
        ...event.row,
        roleCodes: event.row.roles || [],
      });
      this.userForm.get('username')?.disable();
      this.userForm.get('password')?.setValidators([]);
      this.userForm.get('password')?.updateValueAndValidity();
      this.originalValue = this.userForm.getRawValue();
      this.userModal.show();
    } else if (event.key === 'lock-unlock') {
      this.selectedRow = event.row;
      this.lockReason = '';
      this.reasonModal.show();
    }
  }

  async confirmLockUnlock() {
    const res = await this.systemUserService.lockUnlock({
      ...this.selectedRow,
      lockReason: this.lockReason,
    });
    if (res.body.status === 'OK') {
      GlobalToast.success(
        'system_user.toast.lock_unlock.messageSuccess',
        'system_user.toast.lock_unlock.titleSuccess',
      );
      this.onSearch();
      this.reasonModal.hide();
    }
  }

  isUnchanged(): boolean {
    return (
      JSON.stringify(this.userForm.getRawValue()) === JSON.stringify(this.originalValue) ||
      this.userForm.invalid
    );
  }

  async onSubmit() {
    if (this.isUnchanged()) return;
    const req = this.userForm.getRawValue() as any;
    const isUpdate = !!this.userForm.value.id;

    const confirmed = await Popup.confirm({
      title: isUpdate ? 'system_user.popup.update.title' : 'system_user.popup.create.title',
      message: isUpdate ? 'system_user.popup.update.message' : 'system_user.popup.create.message',
      acceptText: isUpdate ? 'common.button.update' : 'common.button.create',
      cancelText: 'common.button.cancel',
    });
    if (!confirmed) return;

    const res = isUpdate
      ? await this.systemUserService.update(req)
      : await this.systemUserService.create(req);

    if (res.body.status === 'OK') {
      GlobalToast.success(
        isUpdate
          ? 'system_user.toast.update.messageSuccess'
          : 'system_user.toast.create.messageSuccess',
        isUpdate
          ? 'system_user.toast.update.titleSuccess'
          : 'system_user.toast.create.titleSuccess',
      );
      this.onSearch();
      this.userModal.hide();
    }
  }

  async onExport(): Promise<void> {
    await this.systemUserService.exportData(this.table.getSearchRequest());
  }
}
