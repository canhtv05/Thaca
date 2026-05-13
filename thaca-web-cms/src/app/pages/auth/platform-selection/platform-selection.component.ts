import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthLayoutComponent } from '../../../layouts/auth-layout/auth-layout.component';
import { ITenantInfoPrj } from '../../system/tenant/tenant.model';
import { TenantService } from '../../system/tenant/tenant.service';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';

@Component({
  selector: 'app-platform-selection',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    AuthLayoutComponent,
    ThacaButtonComponent,
  ],
  templateUrl: './platform-selection.component.html',
  styleUrl: './platform-selection.component.scss',
})
export class PlatformSelectionComponent implements OnInit {
  private readonly tenantService = inject(TenantService);
  private readonly router = inject(Router);

  tenants = signal<ITenantInfoPrj[]>([]);
  isLoadingTenants = signal<boolean>(false);
  searchText = signal<string>('');
  selectedTenantId = signal<number | null>(null);

  filteredTenants = computed(() => {
    const search = this.searchText().toLowerCase().trim();
    if (!search) return this.tenants();
    return this.tenants().filter(
      (t) => t.name.toLowerCase().includes(search) || t.code.toLowerCase().includes(search),
    );
  });

  ngOnInit(): void {
    const state = history.state;
    if (!state || !state.email) {
      this.router.navigate(['/auth/verify']);
      return;
    }
    this.loadTenants();
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
    this.selectedTenantId.set(tenant.id);
  }

  onContinue(): void {
    const id = this.selectedTenantId();
    const tenant = this.tenants().find((t) => t.id === id);
    if (id && tenant) {
      this.router.navigate(['/auth/login'], {
        state: {
          tenant,
        },
      });
    }
  }

  onSelectSuperAdmin(): void {
    this.router.navigate(['/auth/login'], { state: { type: 'SUPER_ADMIN' } });
  }
}
