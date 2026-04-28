import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbComponent } from '../../../../shared/components/breadcrumb/breadcrumb.component';
import { ThacaInputComponent } from '../../../../shared/components/thaca-input/thaca-input.component';
import { ThacaTextareaComponent } from '../../../../shared/components/thaca-textarea/thaca-textarea.component';
import { TenantService } from '../tenant.service';
import { isLoading } from '../../../../core/stores/app.store';
import { ITenantDTO } from '../tenant.model';
import { GlobalToast } from '../../../../core/global/global-toast';

@Component({
  selector: 'app-tenant-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    BreadcrumbComponent,
    ThacaInputComponent,
    ThacaTextareaComponent,
  ],
  templateUrl: './tenant-detail.component.html',
})
export class TenantDetailComponent implements OnInit {
  private tenantService = inject(TenantService);
  private translate = inject(TranslateService);
  private router = inject(Router);
  readonly isLoading = isLoading;
  private route = inject(ActivatedRoute);

  tenant = signal<ITenantDTO | null>(null);
  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-building', label: 'menu.tenant_management', routerLink: ['/system/tenants'] },
  ];

  async ngOnInit(): Promise<void> {
    const tenantCode = this.route.snapshot.paramMap.get('tenantCode') || '';
    if (tenantCode) {
      this.breadcrumbItems.push({
        icon: 'pi pi-eye',
        label: this.translate.instant('tenant.detail_title', { code: tenantCode }),
      });
      const response = await this.tenantService.getTenant({ code: tenantCode });
      if (response.body.status === 'OK') {
        this.tenant.set(response.body.data);
      }
    } else {
      GlobalToast.error('tenant.toast.tenant_code_is_invalid', 'tenant.toast.error');
      this.router.navigate(['/system/tenants']);
    }
  }

  getStatusLabel(status: string): string {
    if (!status) return '';
    return this.translate.instant(`common.status.${status.toLowerCase()}`);
  }

  getExpiresAtLabel(expiresAt: string): string {
    if (!expiresAt) return this.translate.instant('common.infinite');
    return expiresAt;
  }
}
