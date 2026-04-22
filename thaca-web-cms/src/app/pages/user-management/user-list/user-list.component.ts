import { Component, inject, signal, ViewChild } from '@angular/core';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import {
  DataTableComponent,
  ITableConfig,
} from '../../../shared/components/data-table/data-table.component';
import { AppConfigService } from '../../../core/configs/app-config.service';
import { IUserDTO } from '../../../core/models/user.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import {
  IDropdownOption,
  ThacaDropdownComponent,
} from '../../../shared/components/thaca-dropdown/thaca-dropdown.component';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BreadcrumbComponent,
    DataTableComponent,
    ThacaInputComponent,
    ThacaDropdownComponent,
    ThacaButtonComponent,
    TranslateModule,
  ],
  templateUrl: './user-list.component.html',
})
export class UserListComponent {
  private configService = inject(AppConfigService);
  private translate = inject(TranslateService);

  @ViewChild(DataTableComponent) table!: DataTableComponent;

  filter = signal({
    username: '',
    email: '',
    isActivated: true,
  });

  statusOptions: IDropdownOption[] = [
    { label: 'user.active', value: true },
    { label: 'user.inactive', value: false },
  ];

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/admin/users/search`,
    rows: 10,
    actionFixed: true,
    showStt: true,
    columns: [
      { field: 'username', header: 'user.username', sortable: true, width: '150px' },
      { field: 'email', header: 'user.email', sortable: true },
      {
        field: 'isActivated',
        header: 'user.status',
        render: (row: IUserDTO) => {
          const label = this.translate.instant(row.isActivated ? 'user.active' : 'user.inactive');
          const variant = row.isActivated ? 'success' : 'warning';
          return `<span class="thaca-badge thaca-badge-${variant}">
                    <span class="thb-dot"></span>${label}
                  </span>`;
        },
      },
      {
        field: 'isLocked',
        header: 'user.isActivated',
        render: (row: IUserDTO) => {
          const label = this.translate.instant(row.isLocked ? 'user.locked' : 'user.safe');
          const variant = row.isLocked ? 'danger' : 'info';
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
    ],
    actions: [
      { icon: 'pi pi-pencil', titleKey: 'common.button.update', color: 'secondary' },
      {
        icon: 'pi pi-trash',
        titleKey: 'common.button.delete',
        color: 'danger',
        condition: (row) => !row.isActivated,
      },
    ],
  };

  onSearch() {
    this.table.refresh();
  }

  handleAction(event: { actionKey: string; row: any }) {
    const { actionKey, row } = event;

    switch (actionKey) {
      case 'Edit':
        console.log('Editing user:', row);
        break;
      case 'Delete':
        console.log('Deleting user:', row);
        break;
      default:
        console.warn('Unknown action:', actionKey);
    }
  }
}
