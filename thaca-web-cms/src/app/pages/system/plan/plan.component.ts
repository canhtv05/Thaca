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
import { PlanService } from './plan.service';
import { IPlanDTO } from '../../../core/models/plan.model';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';
import { Popup } from '../../../core/global/popup-notify';
import { GlobalToast } from '../../../core/global/global-toast';
import { isLoading } from '../../../core/stores/app.store';

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
  readonly isLoading = isLoading;

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
    code: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9-_]+$/)]],
    name: ['', [Validators.required]],
    type: ['FREE', [Validators.required]],
    maxUsers: [0, [Validators.required, Validators.min(1)]],
    isUpdate: [false],
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
        render: (row: IPlanDTO) => {
          return this.translate.instant(`plan.plans.${row.type}`);
        },
      },
      { field: 'maxUsers', header: 'plan.max_users', sortable: true, width: '120px' },
      {
        field: 'status',
        header: 'plan.status',
        render: (row: IPlanDTO) => {
          const variant = row.status === 'ACTIVE' ? 'success' : 'warning';
          const label = this.translate.instant(`common.status.${row.status.toLowerCase()}`);
          return `<span class="thaca-badge thaca-badge-${variant}"><span class="thb-dot"></span>${label}</span>`;
        },
      },
    ],
    actionFixed: true,
    actions: [
      {
        icon: 'pi pi-pencil',
        key: 'update',
        titleKey: 'common.button.update',
        condition: (row: IPlanDTO) => row.status === 'ACTIVE',
      },
      {
        icon: 'pi pi-key',
        key: 'lock',
        titleKey: 'common.button.lock',
        color: 'info',
        condition: (row: IPlanDTO) => row.status === 'ACTIVE',
      },
      {
        icon: 'pi pi-unlock',
        key: 'unlock',
        titleKey: 'common.button.unlock',
        color: 'info',
        condition: (row: IPlanDTO) => row.status === 'INACTIVE',
      },
    ],
  };

  onSearch() {
    this.table.refresh(this.filter());
  }

  onCreate() {
    this.planForm.reset({ type: 'FREE', maxUsers: 10, isUpdate: false });
    this.planForm.get('code')?.enable();
    this.originalValue = this.planForm.getRawValue();
    this.planModal.show();
  }

  handleAction(event: ITableActionEvent) {
    if (event.key === 'update') {
      this.planForm.patchValue(event.row);
      this.planForm.get('code')?.disable();
      this.planForm.get('isUpdate')?.setValue(true);
      this.originalValue = this.planForm.getRawValue();
      this.planModal.show();
    } else if (event.key === 'lock' || event.key === 'unlock') {
      Popup.confirm({
        title: event.key === 'lock' ? 'plan.popup.lock.title' : 'plan.popup.unlock.title',
        message: event.key === 'lock' ? 'plan.popup.lock.message' : 'plan.popup.unlock.message',
        acceptText: event.key === 'lock' ? 'common.button.lock' : 'common.button.unlock',
        cancelText: 'common.button.cancel',
      }).then(async (result: boolean) => {
        if (result) {
          if (event.key === 'lock' || event.key === 'unlock') {
            const req = { ...event.row, status: event.key === 'lock' ? 'INACTIVE' : 'ACTIVE' };
            const res = await this.planService.lockUnlock(req);
            if (res.body.status === 'OK') {
              const messageKey =
                event.key === 'lock'
                  ? 'plan.toast.lock.messageSuccess'
                  : 'plan.toast.unlock.messageSuccess';
              const titleKey =
                event.key === 'lock'
                  ? 'plan.toast.lock.titleSuccess'
                  : 'plan.toast.unlock.titleSuccess';
              GlobalToast.success(messageKey, titleKey);
              this.onSearch();
            }
          }
        }
      });
    }
  }

  isUnchanged(): boolean {
    return (
      JSON.stringify(this.planForm.getRawValue()) === JSON.stringify(this.originalValue) ||
      this.planForm.invalid
    );
  }

  async onSubmit(): Promise<void> {
    if (this.isUnchanged()) return;
    const req = this.planForm.getRawValue() as unknown as IPlanDTO & { isUpdate: boolean };
    const isUpdate = req?.isUpdate;
    const confirmed = await Popup.confirm({
      title: isUpdate ? 'plan.popup.update.title' : 'plan.popup.create.title',
      message: isUpdate ? 'plan.popup.update.message' : 'plan.popup.create.message',
      acceptText: isUpdate ? 'common.button.update' : 'common.button.create',
      cancelText: 'common.button.cancel',
    });
    if (!confirmed) return;
    const res = isUpdate ? await this.planService.update(req) : await this.planService.create(req);
    if (res.body.status === 'OK') {
      GlobalToast.success(
        isUpdate ? 'plan.toast.update.messageSuccess' : 'plan.toast.create.messageSuccess',
        isUpdate ? 'plan.toast.update.titleSuccess' : 'plan.toast.create.titleSuccess',
      );
      this.onSearch();
      this.planModal.hide();
    }
  }

  async onExport(): Promise<void> {
    await this.planService.exportData(this.table.getSearchRequest());
  }
}
