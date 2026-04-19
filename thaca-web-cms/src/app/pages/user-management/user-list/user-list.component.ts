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
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BreadcrumbComponent,
    DataTableComponent,
    InputTextModule,
    ButtonModule,
    TagModule,
    ThacaInputComponent,
  ],
  templateUrl: './user-list.component.html',
})
export class UserListComponent {
  private configService = inject(AppConfigService);
  @ViewChild(DataTableComponent) table!: DataTableComponent;

  filter = signal({
    username: '',
    email: '',
    fullname: '',
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/admin/users/search`,
    rows: 10,
    actionFixed: true,
    showStt: true,
    columns: [
      { field: 'username', header: 'Username', sortable: true, width: '150px' },
      { field: 'email', header: 'Email', sortable: true },
      {
        field: 'isActivated',
        header: 'Status',
        render: (row: IUserDTO) => {
          const severity = row.isActivated ? 'success' : 'warn';
          const label = row.isActivated ? 'Active' : 'Inactive';
          return `<span class="px-2 py-0.5 rounded-full text-xs font-bold bg-${severity}-100 text-${severity}-700">${label}</span>`;
        },
      },
      {
        field: 'isLocked',
        header: 'Security',
        render: (row: IUserDTO) => {
          const icon = row.isLocked ? 'pi-lock text-red-500' : 'pi-lock-open text-green-500';
          const label = row.isLocked ? 'Locked' : 'Safe';
          return `<div class="flex items-center gap-1"><i class="pi ${icon}"></i><span class="text-xs opacity-60">${label}</span></div>`;
        },
      },
    ],
    actions: [
      { icon: 'pi pi-pencil', titleKey: 'Edit', color: 'secondary' },
      {
        icon: 'pi pi-trash',
        titleKey: 'Delete',
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
