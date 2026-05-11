import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthLayoutComponent } from '../../../layouts/auth-layout/auth-layout.component';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { ITenantInfoPrj } from '../../system/tenant/tenant.model';
import { TenantService } from '../../system/tenant/tenant.service';

@Component({
  selector: 'app-platform-selection',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, AuthLayoutComponent, ThacaButtonComponent],
  templateUrl: './platform-selection.component.html',
  styleUrl: './platform-selection.component.scss',
})
export class PlatformSelectionComponent implements OnInit {
  private readonly tenantService = inject(TenantService);
  private readonly router = inject(Router);

  selectionStep = signal<'choose-type' | 'choose-tenant'>('choose-type');
  tenants = signal<ITenantInfoPrj[]>([]);
  isLoadingTenants = signal<boolean>(false);

  ngOnInit(): void {}

  onSelectSuperAdmin(): void {
    this.router.navigate(['/auth/login']);
  }

  async onSelectOrganization(): Promise<void> {
    this.selectionStep.set('choose-tenant');
    await this.loadTenants();
  }

  async loadTenants(): Promise<void> {
    this.isLoadingTenants.set(true);
    try {
      const res = await this.tenantService.getAll();
      if (res.body.status === 'OK') {
        this.tenants.set(res.body.data);
      }
    } finally {
      this.isLoadingTenants.set(false);
    }
  }

  onSelectTenant(tenant: ITenantInfoPrj): void {
    this.router.navigate(['/auth/login'], { queryParams: { tenantId: tenant.id } });
  }

  onBackToType(): void {
    this.selectionStep.set('choose-type');
  }
}
