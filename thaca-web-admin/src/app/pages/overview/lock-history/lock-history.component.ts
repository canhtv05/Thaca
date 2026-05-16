import { Component, inject, OnInit, signal, computed, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { AppConfigService } from '../../../core/configs/app-config.service';
import {
  DataTableComponent,
  ITableConfig,
} from '../../../shared/components/data-table/data-table.component';
import {
  IDropdownOption,
  ThacaDropdownComponent,
} from '../../../shared/components/thaca-dropdown/thaca-dropdown.component';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { ActivatedRoute, Router } from '@angular/router';
import { GlobalToast } from '../../../core/global/global-toast';
import { IUserLockHistoryDTO } from './lock-history.model';

@Component({
  selector: 'app-lock-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    BreadcrumbComponent,
    DataTableComponent,
    ThacaButtonComponent,
    ThacaDropdownComponent,
    ThacaInputComponent,
  ],
  templateUrl: './lock-history.component.html',
})
export class LockHistoryComponent implements OnInit {
  private configService = inject(AppConfigService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private translate = inject(TranslateService);

  @ViewChild(DataTableComponent) table!: DataTableComponent;

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-shield', label: 'menu.access_control' },
    {
      icon: 'pi pi-id-card',
      label: 'menu.system_user_management',
      routerLink: ['/system/access-control/system-users'],
    },
    { icon: 'pi pi-lock', label: 'menu.system_user_lock_history' },
  ];

  filter = signal({
    reason: '',
    targetUserId: null,
    action: null,
  }) as any;

  lockedOptions: IDropdownOption[] = [
    { label: 'common.status.all', value: null },
    { label: 'common.status.lock', value: true },
    { label: 'common.status.unlock', value: false },
  ];

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/auth/admin/system-users/search-lock-histories`,
    rows: 10,
    showStt: true,
    withAudit: true,
    actionFixed: true,
    columns: [
      {
        field: 'action',
        header: 'system_user.action',
        sortable: true,
        render: (row: IUserLockHistoryDTO) => {
          const variant = row.action === 'LOCK' ? 'danger' : 'success';
          const label = row.action === 'LOCK' ? 'common.status.lock' : 'common.status.unlock';
          return `<span class="thaca-badge thaca-badge-${variant}">
                    <span class="thb-dot"></span>${this.translate.instant(label)}
                  </span>`;
        },
      },
      { field: 'reason', header: 'system_user.reason', sortable: true },
    ],
  };

  async ngOnInit(): Promise<void> {
    const targetUserId = this.route.snapshot.paramMap.get('targetUserId');
    const viewMode = this.route.snapshot.data['viewMode'];
    if (viewMode === 'current-user') {
      this.breadcrumbItems = [{ icon: 'pi pi-lock', label: 'menu.system_user_lock_history' }];
    } else if (viewMode === 'user') {
      this.breadcrumbItems = [
        {
          icon: 'pi pi-user',
          label: 'menu.user_management',
        },
        {
          icon: 'pi pi-list',
          label: 'menu.user_management',
          routerLink: ['/user-management/users'],
        },
        { icon: 'pi pi-lock', label: 'menu.lock_history' },
      ];
    }
    if (targetUserId) {
      this.filter.set({ ...this.filter(), targetUserId: targetUserId });
    } else {
      GlobalToast.error('system_user.toast.user_id_is_invalid', 'system_user.toast.error');
      this.router.navigate(['/system/access-control/system-users']);
    }
  }

  onSearch() {
    this.table.refresh(this.filter());
  }

  onExport() {}
}
