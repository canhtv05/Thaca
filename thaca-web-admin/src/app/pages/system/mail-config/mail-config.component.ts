import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import { MailConfigService } from './mail-config.service';
import { IMailConfigDTO } from './mail-config.model';
import { GlobalToast } from '../../../core/global/global-toast';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { ThacaTabsComponent } from '../../../shared/components/thaca-tabs/thaca-tabs.component';
import { ThacaTabComponent } from '../../../shared/components/thaca-tabs/thaca-tab.component';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';
import {
  IDropdownOption,
  ThacaDropdownComponent,
} from '../../../shared/components/thaca-dropdown/thaca-dropdown.component';
import { ThacaSwitchComponent } from '../../../shared/components/thaca-switch/thaca-switch.component';

@Component({
  selector: 'app-mail-config',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslateModule,
    BreadcrumbComponent,
    ThacaButtonComponent,
    ThacaTabsComponent,
    ThacaTabComponent,
    ThacaInputComponent,
    ValidationMessageComponent,
    ThacaDropdownComponent,
    ThacaSwitchComponent,
  ],
  templateUrl: './mail-config.component.html',
})
export class MailConfigComponent implements OnInit {
  private mailConfigService = inject(MailConfigService);
  private fb = inject(FormBuilder);
  private translate = inject(TranslateService);

  activeTab = signal<'system' | 'tenant'>('system');
  systemConfig = signal<IMailConfigDTO | null>(null);
  loading = signal<boolean>(false);
  saving = signal<boolean>(false);
  testingConnection = signal<boolean>(false);
  testResult = signal<{ success: boolean; message: string } | null>(null);

  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    {
      icon: 'pi pi-cog',
      label: 'menu.global_configuration',
      routerLink: ['/system/settings/mail-config'],
    },
    { icon: 'pi pi-envelope', label: 'menu.mail_settings' },
  ];

  portOptions: IDropdownOption[] = [
    { label: '587 - STARTTLS', value: 587 },
    { label: '465 - SSL/TLS', value: 465 },
    { label: '25', value: 25 },
  ];

  configForm!: FormGroup;

  ngOnInit() {
    this.initializeForm();
    this.loadSystemConfig();
  }

  private initializeForm() {
    this.configForm = this.fb.group({
      id: [null],
      host: ['', [Validators.required]],
      port: [587, [Validators.required]],
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
      fromName: [''],
      fromEmail: ['', [Validators.email]],
      isAuth: [true],
      isStarttls: [true],
      isDefault: [false],
      configCode: [''],
      description: [''],
      tenantId: [null],
    });
  }

  async loadSystemConfig() {
    this.loading.set(true);
    try {
      const result = await this.mailConfigService.search({
        page: { page: 1, size: 1 } as any,
        filter: { tenantId: null } as unknown as IMailConfigDTO,
      });
      if (result.body?.data && result.body.data.length > 0) {
        this.systemConfig.set(result.body.data[0]);
        this.patchFormFromConfig(result.body.data[0]);
      } else {
        this.systemConfig.set(null);
        this.resetForm();
      }
    } catch (err) {
      console.error('Error loading system config:', err);
    } finally {
      this.loading.set(false);
    }
  }

  private patchFormFromConfig(config: IMailConfigDTO) {
    this.configForm.patchValue({
      id: config.id,
      host: config.host,
      port: config.port,
      username: config.username,
      password: '',
      fromName: config.fromName || '',
      fromEmail: config.fromEmail || '',
      isAuth: config.isAuth !== false,
      isStarttls: config.isStarttls !== false,
      isDefault: config.isDefault || false,
      configCode: config.configCode || '',
      description: config.description || '',
      tenantId: null,
    });
    this.configForm.get('password')?.clearValidators();
    this.configForm.get('password')?.updateValueAndValidity();
  }

  resetForm() {
    this.testResult.set(null);
    const config = this.systemConfig();
    if (config) {
      this.patchFormFromConfig(config);
      return;
    }
    this.configForm.reset({
      port: 587,
      isAuth: true,
      isStarttls: true,
      isDefault: false,
      tenantId: null,
    });
    this.configForm.get('password')?.setValidators([Validators.required]);
    this.configForm.get('password')?.updateValueAndValidity();
  }

  onTabChange(tab: 'system' | 'tenant') {
    this.activeTab.set(tab);
    if (tab === 'system' && !this.systemConfig()) {
      this.loadSystemConfig();
    }
  }

  async saveConfig() {
    if (this.configForm.invalid) {
      GlobalToast.error(this.translate.instant('common.error.validation_failed'));
      return;
    }

    this.saving.set(true);
    try {
      const formValue = this.configForm.value;
      const isEdit = !!this.systemConfig()?.id;

      const payload: IMailConfigDTO = {
        ...formValue,
        status: 'ACTIVE',
      };

      if (isEdit) {
        await this.mailConfigService.update(payload);
        GlobalToast.success(this.translate.instant('common.message.update_success'));
      } else {
        await this.mailConfigService.create(payload);
        GlobalToast.success(this.translate.instant('common.message.create_success'));
      }

      await this.loadSystemConfig();
    } catch (err: any) {
      const errorMsg = err?.body?.message || 'Error saving config';
      GlobalToast.error(errorMsg);
    } finally {
      this.saving.set(false);
    }
  }

  async testConnection() {
    if (this.configForm.get('host')?.invalid || this.configForm.get('port')?.invalid) {
      GlobalToast.error(this.translate.instant('mail_config.error.invalid_host_port'));
      return;
    }

    this.testingConnection.set(true);
    this.testResult.set(null);

    try {
      const testConfig = {
        host: this.configForm.get('host')?.value,
        port: this.configForm.get('port')?.value,
        username: this.configForm.get('username')?.value,
        password: this.configForm.get('password')?.value,
        isAuth: this.configForm.get('isAuth')?.value,
        isStarttls: this.configForm.get('isStarttls')?.value,
      };

      const result = await this.mailConfigService.testConnection(testConfig);
      this.testResult.set(result.body?.data || { success: false, message: 'Unknown error' });

      if (result.body?.data?.success) {
        GlobalToast.success(result.body.data.message);
      } else {
        GlobalToast.error(result.body?.data?.message || 'Connection failed');
      }
    } catch (err: any) {
      const message = err?.body?.data?.message || err?.body?.message || 'Connection test failed';
      this.testResult.set({ success: false, message });
      GlobalToast.error(message);
    } finally {
      this.testingConnection.set(false);
    }
  }
}
