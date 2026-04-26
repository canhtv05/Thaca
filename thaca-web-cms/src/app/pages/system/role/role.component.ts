import { Component, inject, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DatePickerModule } from 'primeng/datepicker';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import {
  DataTableComponent,
  ITableActionEvent,
  ITableConfig,
} from '../../../shared/components/data-table/data-table.component';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { MenuItem } from 'primeng/api';
import { Router } from '@angular/router';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';

@Component({
  selector: 'app-role',
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
  templateUrl: './role.component.html',
})
export class RoleComponent {
  private configService = inject(AppConfigService);
  private router = inject(Router);

  @ViewChild(DataTableComponent) table!: DataTableComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-shield', label: 'menu.access_control' },
    { icon: 'pi pi-key', label: 'menu.role_permission' },
  ];

  filter = signal<{ code: string | null }>({
    code: null,
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/cms/roles/search`,
    rows: 10,
    showStt: true,
    columns: [
      { field: 'code', header: 'role_permission.role.code', sortable: true },
      { field: 'description', header: 'role_permission.role.description' },
    ],
    actions: [
      {
        icon: 'pi pi-eye',
        titleKey: 'role_permission.actions.view_permission',
        key: 'view',
      },
    ],
  };

  handleAction(event: ITableActionEvent) {
    if (event.key === 'view') {
      const roleCode = event.row?.code;
      if (roleCode) {
        this.router.navigate(['/system/role-permission', roleCode, 'permissions']);
      }
    }
  }

  onSearch() {
    this.table.refresh(this.filter());
  }
}
