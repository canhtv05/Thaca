import { Component, inject, OnInit, signal, computed, ViewChild } from '@angular/core';
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
import { IRoleDTO } from '../role/role.model';
import { ITenantInfoPrj } from '../tenant/tenant.model';
import { ThacaTextareaComponent } from '../../../shared/components/thaca-textarea/thaca-textarea.component';
import { IPermissionDTO } from '../permission/permission.model';
import { Router } from '@angular/router';

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
    ThacaTextareaComponent,
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
  private router = inject(Router);

  private originalValue: any;
  roleOptions = signal<IDropdownOption[]>([]);
  tenantOptions = signal<IDropdownOption[]>([]);
  availablePermissions = signal<any[]>([]);
  grantedPermissions = signal<Map<string, Set<string>>>(new Map());
  private selectedRow: any;
  private editingPermissions: Map<string, Set<string>> | null = null;

  @ViewChild(DataTableComponent) table!: DataTableComponent;
  @ViewChild('userModal') userModal!: ThacaModalComponent;
  @ViewChild('reasonModal') reasonModal!: ThacaModalComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-shield', label: 'menu.access_control' },
    { icon: 'pi pi-id-card', label: 'menu.system_user_management' },
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
    { label: 'common.status.active', value: true },
    { label: 'common.status.inactive', value: false },
  ];

  lockedOptions: IDropdownOption[] = [
    { label: 'common.status.all', value: null },
    { label: 'common.status.lock', value: true },
    { label: 'common.status.unlock', value: false },
  ];

  userForm = this.fb.group({
    id: [null],
    username: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    fullname: ['', [Validators.required]],
    password: [''],
    tenantId: [null, [Validators.required]],
    isActivated: [true],
    isLocked: [false],
    isSuperAdmin: [false],
    avatarUrl: [''],
    roleCodes: [[] as string[], [Validators.required, Validators.minLength(1)]],
    lockReason: [''],
  });

  lockReasonForm = this.fb.group({
    lockReason: ['', [Validators.required]],
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
        field: 'tenantInfo',
        header: 'system_user.tenant',
        center: true,
        render: (row: ISystemUserDTO) => {
          return row.tenantInfo
            ? `<span class="thaca-badge thaca-badge-primary"><span class="thb-dot"></span>${row.tenantInfo.code} - ${row.tenantInfo.name}</span>`
            : '';
        },
      },
      {
        field: 'isActivated',
        header: 'system_user.is_activated',
        render: (row: ISystemUserDTO) => {
          const variant = row.isActivated ? 'success' : 'danger';
          const label = this.translate.instant(
            row.isActivated ? 'common.status.active' : 'common.status.inactive',
          );
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
      {
        field: 'isLocked',
        header: 'system_user.is_locked',
        render: (row: ISystemUserDTO) => {
          const variant = row.isLocked ? 'danger' : 'success';
          const label = this.translate.instant(
            row.isLocked ? 'common.status.lock' : 'common.status.unlock',
          );
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
      {
        field: 'isSuperAdmin',
        header: 'system_user.is_super_admin',
        center: true,
        render: (row: ISystemUserDTO) => {
          return row.isSuperAdmin ? `<i class="pi pi-check-circle text-primary"></i>` : '';
        },
      },
    ],
    actions: [
      {
        icon: 'pi pi-eye',
        key: 'view',
        titleKey: 'common.button.view',
        color: 'primary',
      },
      {
        icon: 'pi pi-pencil',
        key: 'edit',
        titleKey: 'common.button.update',
      },
      {
        icon: 'pi pi-key',
        key: 'lock',
        titleKey: 'common.button.lock',
        color: 'danger',
        condition: (row: ISystemUserDTO) => !row.isLocked,
      },
      {
        icon: 'pi pi-unlock',
        key: 'unlock',
        titleKey: 'common.button.unlock',
        color: 'success',
        condition: (row: ISystemUserDTO) => row.isLocked ?? false,
      },
      {
        icon: 'pi pi-lock',
        key: 'view_lock_reason',
        titleKey: 'menu.system_user_lock_history',
        color: 'primary',
      },
      {
        icon: 'pi pi-clock',
        key: 'view_login_history',
        titleKey: 'menu.login_history',
        color: 'primary',
      },
    ],
  };

  async ngOnInit(): Promise<void> {
    await Promise.all([this.loadRoles(), this.loadTenants()]);

    this.userForm.get('roleCodes')?.valueChanges.subscribe(async (roles) => {
      if (roles && roles.length > 0) {
        const res = await this.roleService.getPermissionsByRoles(roles);
        if (res.body.status === 'OK' && res.body.data) {
          const perms = res.body.data;
          this.availablePermissions.set(perms);

          const newMap = new Map<string, Set<string>>();
          roles.forEach((role: string) => {
            let granted: Set<string>;
            if (this.editingPermissions && this.editingPermissions.has(role)) {
              granted = this.editingPermissions.get(role)!;
            } else {
              granted = new Set<string>(
                perms
                  .filter((p: IPermissionDTO) => p.roleCode === role)
                  .map((p: IPermissionDTO) => p.code),
              );
            }
            newMap.set(role, granted);
          });
          this.grantedPermissions.set(newMap);
          this.editingPermissions = null;
        }
      } else {
        this.availablePermissions.set([]);
        this.grantedPermissions.set(new Map());
      }
    });

    this.userForm.get('isLocked')?.valueChanges.subscribe((locked) => {
      const reasonCtrl = this.userForm.get('lockReason');
      if (locked) {
        reasonCtrl?.setValidators([Validators.required]);
      } else {
        reasonCtrl?.clearValidators();
      }
      reasonCtrl?.updateValueAndValidity();
    });
  }

  isPermissionGranted(role: string, perm: string): boolean {
    return this.grantedPermissions().get(role)?.has(perm) ?? false;
  }

  getPermissionsForRole(roleCode: string): IPermissionDTO[] {
    return this.availablePermissions().filter((p: IPermissionDTO) => p.roleCode === roleCode);
  }

  togglePermission(role: string, perm: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    const map = new Map(this.grantedPermissions());
    const perms = new Set(map.get(role) ?? []);
    if (checked) {
      perms.add(perm);
    } else {
      perms.delete(perm);
    }
    map.set(role, perms);
    this.grantedPermissions.set(map);
  }

  async loadRoles() {
    const res = await this.roleService.getAllRoles();
    if (res.body.status === 'OK') {
      this.roleOptions.set(
        res.body.data.map((role: IRoleDTO) => ({
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
        res.body.data.map((tenant: ITenantInfoPrj) => ({
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
    this.userForm.reset({
      isActivated: true,
      isLocked: false,
      isSuperAdmin: false,
      roleCodes: [],
      avatarUrl: '',
    });
    this.userForm.get('username')?.enable();
    this.userForm.get('password')?.setValidators([Validators.required]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.originalValue = this.userForm.getRawValue();

    this.userForm.patchValue({
      username: 'test',
      email: 'test@gmail.com',
      fullname: 'test',
      tenantId: 1 as any,
      password: 'Thaca@2026',
    });
    this.userModal.show();
  }

  handleAction(event: ITableActionEvent) {
    if (event.key === 'view') {
      this.router.navigate(['/system/system-users', event.row.id]);
    } else if (event.key === 'view_login_history') {
      this.router.navigate(['/system/system-users', event.row?.username, 'login-history']);
    } else if (event.key === 'edit') {
      const savedPermissions = new Map<string, Set<string>>();
      if (event.row.roles) {
        Object.entries(event.row.roles).forEach(([role, perms]) => {
          const granted = new Set<string>();
          Object.entries(perms as { [key: string]: string }).forEach(([p, effect]) => {
            if (effect === 'GRANT') granted.add(p);
          });
          savedPermissions.set(role, granted);
        });
      }
      this.editingPermissions = savedPermissions;
      this.userForm.get('username')?.disable();
      this.userForm.get('password')?.setValidators([]);
      this.userForm.get('password')?.updateValueAndValidity();
      this.userForm.patchValue({
        ...event.row,
        roleCodes: Object.keys(event.row.roles || {}),
      });
      this.userModal.show();
    } else if (event.key === 'lock' || event.key === 'unlock') {
      this.selectedRow = event.row;
      this.lockReasonForm.reset();
      this.reasonModal.show();
    } else if (event.key === 'view_lock_reason') {
      this.router.navigate(['/system/system-users', event.row.id, 'lock-history']);
    }
  }

  async confirmLockUnlock() {
    const isLocked = this.selectedRow.isLocked;
    const confirmed = await Popup.confirm({
      title: isLocked ? 'system_user.popup.unlock.title' : 'system_user.popup.lock.title',
      message: isLocked ? 'system_user.popup.unlock.message' : 'system_user.popup.lock.message',
      acceptText: isLocked ? 'common.button.unlock' : 'common.button.lock',
      cancelText: 'common.button.cancel',
    });
    if (!confirmed) return;

    const res = await this.systemUserService.lockUnlock({
      ...this.selectedRow,
      lockReason: this.lockReasonForm.get('lockReason')?.value,
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
    const { roleCodes, ...req } = this.userForm.getRawValue() as any;

    const roles: { [key: string]: { [key: string]: string } } = {};
    this.grantedPermissions().forEach((perms, role) => {
      const permsMap: { [key: string]: string } = {};
      const allPossiblePerms = this.getPermissionsForRole(role);
      allPossiblePerms.forEach((p) => {
        permsMap[p.code] = perms.has(p.code) ? 'GRANT' : 'DENY';
      });
      roles[role] = permsMap;
    });
    req.roles = roles;

    const isUpdate = !!req.id;

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
