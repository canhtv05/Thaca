import { Component, inject, OnInit, signal, ViewChild } from '@angular/core';
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
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login-history',
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
  templateUrl: './login-history.component.html',
})
export class LoginHistoryComponent implements OnInit {
  private configService = inject(AppConfigService);
  private translate = inject(TranslateService);
  private route = inject(ActivatedRoute);

  @ViewChild(DataTableComponent) table!: DataTableComponent;

  breadcrumbItems: MenuItem[] = [{ label: 'menu.login_history' }];

  rangeDates: Date[] | undefined;

  filter = signal({
    status: null,
    channel: null,
    deviceType: null,
    browser: '',
    fromDate: null as string | null,
    toDate: null as string | null,
    username: null as string | null,
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
    url: `${this.configService.getApiUrl()}/auth/search-login-history`,
    rows: 10,
    showStt: true,
    columns: [
      { field: 'loginTime', header: 'auth.loginTime', width: '180px' },
      { field: 'country', header: 'auth.country' },
      { field: 'city', header: 'auth.city' },
      { field: 'latitude', header: 'auth.latitude' },
      { field: 'longitude', header: 'auth.longitude' },
      { field: 'approxLocation', header: 'auth.approxLocation' },
      { field: 'countryIsoCode', header: 'auth.countryIsoCode' },
      { field: 'ipAddress', header: 'auth.ipAddress', width: '130px' },
      {
        field: 'location',
        header: 'auth.location',
        render: (row: ILoginHistoryDTO) =>
          `${row.city || ''}${row.city && row.country ? ', ' : ''}${row.country || ''}`,
      },
      {
        field: 'device',
        header: 'auth.device',
        render: (row: ILoginHistoryDTO) => `
          <div class="flex flex-col gap-0.5 min-w-52">
            <span class="text-xs opacity-70">${this.translate.instant(row.deviceType || 'auth.unknown')}</span>
            <span>${row.os || ''} / ${row.browser || ''}</span>
          </div>
        `,
      },
      {
        field: 'isNewDevice',
        header: 'auth.isNewDevice',
        render: (row: ILoginHistoryDTO) =>
          row.isNewDevice
            ? `<span class="text-success thaca-badge thaca-badge-success font-bold">${this.translate.instant('common.yes')}</span>`
            : `<span class="opacity-50 thaca-badge thaca-badge-danger font-bold">${this.translate.instant('common.no')}</span>`,
      },
      { field: 'deviceId', header: 'auth.deviceId', width: '150px' },
      { field: 'channel', header: 'auth.channel', width: '100px' },
      {
        field: 'status',
        header: 'user.status',
        width: '120px',
        render: (row: ILoginHistoryDTO) => {
          const isSuccess = row.status === 'SUCCESS';
          const label = this.translate.instant(isSuccess ? 'auth.success' : 'auth.failed');
          const variant = isSuccess ? 'success' : 'danger';
          return `<span class="thaca-badge thaca-badge-${variant}">
                    <span class="thb-dot"></span>${label}
                  </span>`;
        },
      },
      {
        field: 'riskScore',
        header: 'auth.riskScore',
        width: '100px',
        render: (row: ILoginHistoryDTO) => {
          const score = row.riskScore || 0;
          let variant = 'success';
          if (score > 50) variant = 'danger';
          else if (score > 20) variant = 'warning';
          return `<span class="thaca-badge thaca-badge-${variant}">${score}</span>`;
        },
      },
      {
        field: 'isVpn',
        header: 'auth.vpn',
        width: '80px',
        render: (row: ILoginHistoryDTO) =>
          row.isVpn
            ? `<span class="text-danger font-bold">YES</span>`
            : `<span class="opacity-50">NO</span>`,
      },
      { field: 'requestId', header: 'auth.requestId', width: '150px' },
      { field: 'failureReason', header: 'auth.failureReason' },
    ],
  };

  async ngOnInit(): Promise<void> {
    const viewMode = this.route.snapshot.data['viewMode'];
    const username = this.route.snapshot.paramMap.get('targetUserId');
    if (username) {
      this.filter.set({ ...this.filter(), username: username });
    }
    if (viewMode === 'system-user') {
      this.breadcrumbItems = [
        { icon: 'pi pi-cog', label: 'menu.system_administration' },
        { icon: 'pi pi-shield', label: 'menu.access_control' },
        {
          icon: 'pi pi-id-card',
          label: 'menu.system_user_management',
          routerLink: ['/system/access-control/system-users'],
        },
        { icon: 'pi pi-clock', label: 'menu.login_history' },
      ];
    } else if (viewMode === 'user') {
      this.breadcrumbItems = [
        {
          icon: 'pi pi-user',
          label: 'menu.user_management',
          routerLink: ['/user-management/users'],
        },
        { icon: 'pi pi-clock', label: 'menu.login_history' },
      ];
    }
  }

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
