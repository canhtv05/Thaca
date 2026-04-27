import { Component, computed, inject, signal, ViewChild } from '@angular/core';
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
import { PlanService } from '../../../core/services/plan.service';
import { PlanDTO } from '../../../core/models/plan.model';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';

@Component({
  selector: 'app-plan',
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
  templateUrl: './plan.component.html',
})
export class PlanComponent {
  private configService = inject(AppConfigService);
  private planService = inject(PlanService);
  private translate = inject(TranslateService);
  private fb = inject(FormBuilder);

  @ViewChild(DataTableComponent) table!: DataTableComponent;
  @ViewChild('planModal') planModal!: ThacaModalComponent;

  private originalValue: any;
  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-list', label: 'plan.title' },
  ];

  filter = signal({
    code: '',
    name: '',
    type: null,
    status: null,
  });

  statusOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'common.status.active', value: 'ACTIVE' },
    { label: 'common.status.inactive', value: 'INACTIVE' },
  ];

  planTypeOptions: IDropdownOption[] = [
    { label: 'common.all', value: null },
    { label: 'plan.plans.FREE', value: 'FREE' },
    { label: 'plan.plans.BASIC', value: 'BASIC' },
    { label: 'plan.plans.PRO', value: 'PRO' },
    { label: 'plan.plans.ENTERPRISE', value: 'ENTERPRISE' },
  ];

  planForm = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9]+$/)]],
    name: ['', [Validators.required]],
    type: ['FREE', [Validators.required]],
    maxUsers: [0, [Validators.required, Validators.min(1)]],
  });

  tableConfig: ITableConfig = {
    url: `${this.configService.getApiUrl()}/cms/plans/search`,
    rows: 10,
    showStt: true,
    withAudit: true,
    columns: [
      { field: 'code', header: 'plan.code', sortable: true, width: '150px' },
      { field: 'name', header: 'plan.name', sortable: true },
      {
        field: 'type',
        header: 'plan.type',
        render: (row: PlanDTO) => {
          return this.translate.instant(`plan.plans.${row.type}`);
        },
      },
      { field: 'maxUsers', header: 'plan.max_users', sortable: true, width: '120px' },
      {
        field: 'status',
        header: 'plan.status',
        render: (row: PlanDTO) => {
          const variant = row.status === 'ACTIVE' ? 'success' : 'warning';
          const label = this.translate.instant(`common.status.${row.status.toLowerCase()}`);
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
    ],
    actions: [
      { icon: 'pi pi-pencil', key: 'update', titleKey: 'common.button.update' },
      {
        icon: 'pi pi-key',
        key: 'lock',
        titleKey: 'common.button.lock',
        color: 'info',
        condition: (row: PlanDTO) => row.status === 'ACTIVE',
      },
      {
        icon: 'pi pi-unlock',
        key: 'unlock',
        titleKey: 'common.button.unlock',
        color: 'info',
        condition: (row: PlanDTO) => row.status === 'INACTIVE',
      },
    ],
  };

  onSearch() {
    this.table.refresh(this.filter());
  }

  onCreate() {
    this.planForm.reset({ type: 'FREE', maxUsers: 10 });
    this.planForm.get('code')?.enable();
    this.planModal.show();
  }

  handleAction(event: ITableActionEvent) {
    if (event.key === 'update') {
      this.planForm.patchValue(event.row);
      this.planForm.get('code')?.disable();
      this.originalValue = this.planForm.getRawValue();
      this.planModal.show();
    } else if (event.key === 'delete') {
      if (confirm(this.translate.instant('common.confirm_delete'))) {
        this.planService.delete(event.row.id).subscribe(() => {
          this.onSearch();
        });
      }
    }
  }

  onSave() {
    if (this.planForm.invalid) return;
    const data = this.planForm.value as any;
    this.planService.save(data).subscribe(() => {
      this.planModal.hide();
      this.onSearch();
    });
  }

  isUnchanged(): boolean {
    return (
      JSON.stringify(this.planForm.getRawValue()) === JSON.stringify(this.originalValue) ||
      this.planForm.invalid
    );
  }
}
