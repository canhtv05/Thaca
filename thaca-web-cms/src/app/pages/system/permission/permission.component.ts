import { Component, inject, OnInit, signal, computed, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DatePickerModule } from 'primeng/datepicker';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import {
  DataTableComponent,
  ITableConfig,
} from '../../../shared/components/data-table/data-table.component';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { MenuItem } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import { GlobalToast } from '../../../core/global/global-toast';
import { TranslateService } from '@ngx-translate/core';
import { PermissionService } from './permission.service';

@Component({
  selector: 'app-permission',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    DatePickerModule,
    BreadcrumbComponent,
    DataTableComponent,
    ThacaButtonComponent,
    ThacaInputComponent,
  ],
  templateUrl: './permission.component.html',
})
export class PermissionComponent implements OnInit {
  private configService = inject(AppConfigService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private translateService = inject(TranslateService);
  private permissionService = inject(PermissionService);
  table = viewChild(DataTableComponent);
  roleDescription = computed(() => {
    const data = this.table()?.data();
    if (data && data.length > 0) {
      return data[0].roleDescription;
    }
    return null;
  });
  title = computed(() => {
    const roleDescription = this.roleDescription();
    if (roleDescription) {
      return this.translateService.instant('menu.permission') + ' - ' + roleDescription;
    }
    return this.translateService.instant('menu.permission');
  });

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-shield', label: 'menu.access_control' },
    { icon: 'pi pi-key', label: 'menu.role_permission', routerLink: '/system/role-permission' },
    { icon: 'pi pi-check-circle', label: 'menu.permission' },
  ];

  filter = signal<{ code: string | null; roleCode: string | null }>({
    code: null,
    roleCode: null,
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/auth/admin/permissions/search`,
    rows: 10,
    showStt: true,
    columns: [
      { field: 'code', header: 'role_permission.permission.code', sortable: true },
      { field: 'description', header: 'role_permission.permission.description' },
    ],
  };

  ngOnInit(): void {
    const roleCode = this.route.snapshot.paramMap.get('roleCode');
    if (roleCode) {
      this.filter.set({ ...this.filter(), roleCode: roleCode });
    } else {
      GlobalToast.error(
        'role_permission.toast.role_code_is_invalid',
        'role_permission.toast.error',
      );
      this.router.navigate(['/system/role-permission']);
    }
  }

  onSearch() {
    this.table()?.refresh(this.filter());
  }

  async onExport(): Promise<void> {
    await this.permissionService.exportPermissions(this.table()?.getSearchRequest());
  }
}
