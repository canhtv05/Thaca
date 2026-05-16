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
import { MailConfigService } from './mail-config.service';
import { ThacaSwitchComponent } from '../../../shared/components/thaca-switch/thaca-switch.component';
@Component({
  selector: 'app-mail-config',
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
    ThacaSwitchComponent,
  ],
  templateUrl: './mail-config.component.html',
})
export class MailConfigComponent {
  private configService = inject(AppConfigService);
  private router = inject(Router);
  private mailConfigService = inject(MailConfigService);

  @ViewChild(DataTableComponent) table!: DataTableComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-shield', label: 'menu.access_control' },
    { icon: 'pi pi-key', label: 'menu.role_permission' },
  ];

  filter = signal<{ code: string | null }>({
    code: null,
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/auth/admin/mail-configs/search`,
    rows: 10,
    showStt: true,
    columns: [
      { field: 'configCode', header: 'mail_config.configCode', sortable: true },
      { field: 'description', header: 'mail_config.description' },
      { field: 'fromName', header: 'mail_config.fromName' },
      { field: 'fromEmail', header: 'mail_config.fromEmail' },
      { field: 'host', header: 'mail_config.host' },
      { field: 'port', header: 'mail_config.port' },
      { field: 'username', header: 'mail_config.username' },
      { field: 'password', header: 'mail_config.password' },
      { field: 'isAuth', header: 'mail_config.isAuth' },
      { field: 'isStarttls', header: 'mail_config.isStarttls' },
      { field: 'status', header: 'mail_config.status' },
      { field: 'isDefault', header: 'mail_config.isDefault' },
    ],
    actions: [
      {
        icon: 'pi pi-eye',
        titleKey: 'mail_config.actions.view_mail_config',
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
