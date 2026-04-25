import { Component, inject, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { DatePickerModule } from 'primeng/datepicker';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import {
  DataTableComponent,
  ITableConfig,
} from '../../../shared/components/data-table/data-table.component';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import {
  IDropdownOption,
  ThacaDropdownComponent,
} from '../../../shared/components/thaca-dropdown/thaca-dropdown.component';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { ILoginHistoryDTO } from '../../../core/models/login-history.model';
import { ThacaDatepickerComponent } from '../../../shared/components/thaca-datepicker/thaca-datepicker.component';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-role-permission',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    DatePickerModule,
    BreadcrumbComponent,
    DataTableComponent,
    ThacaInputComponent,
    ThacaDropdownComponent,
    ThacaButtonComponent,
    ThacaDatepickerComponent,
  ],
  templateUrl: './role-permission.component.html',
})
export class RolePermissionComponent {
  private configService = inject(AppConfigService);
  private translate = inject(TranslateService);

  @ViewChild(DataTableComponent) table!: DataTableComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-shield', label: 'menu.access_control' },
    { icon: 'pi pi-key', label: 'menu.role_permission' },
  ];

  rangeDates: Date[] | undefined;

  filter = signal({
    status: null,
    channel: null,
    deviceType: null,
    browser: '',
    fromDate: null as string | null,
    toDate: null as string | null,
  });

  statusOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'auth.success', value: 'SUCCESS' },
    { label: 'auth.failed', value: 'FAILED' },
  ];

  channelOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'auth.web', value: 'WEB' },
    { label: 'auth.mobile', value: 'MOBILE' },
  ];

  deviceTypeOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'auth.desktop', value: 'DESKTOP' },
    { label: 'auth.mobile', value: 'MOBILE' },
    { label: 'auth.tablet', value: 'TABLET' },
    { label: 'auth.unknown', value: 'UNKNOWN' },
  ];

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/cms/roles/search`,
    rows: 10,
    showStt: true,
    columns: [
      { field: 'code', header: 'role.code', sortable: true },
      { field: 'description', header: 'role.description' },
    ],
  };

  onSearch() {
    if (
      this.rangeDates &&
      this.rangeDates.length === 2 &&
      this.rangeDates[0] &&
      this.rangeDates[1]
    ) {
      this.filter.update((f) => ({
        ...f,
        fromDate: this.rangeDates![0].toISOString(),
        toDate: this.rangeDates![1].toISOString(),
      }));
    } else {
      this.filter.update((f) => ({ ...f, fromDate: null, toDate: null }));
    }
    this.table.refresh(this.filter());
  }
}
