import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbComponent } from '../../../../shared/components/breadcrumb/breadcrumb.component';
import { ThacaInputComponent } from '../../../../shared/components/thaca-input/thaca-input.component';
import { ThacaTextareaComponent } from '../../../../shared/components/thaca-textarea/thaca-textarea.component';
import { SystemUserService } from '../system-user.service';
import { ISystemUserDTO } from '../system-user.model';
import { GlobalToast } from '../../../../core/global/global-toast';

@Component({
  selector: 'app-system-user-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    BreadcrumbComponent,
    ThacaInputComponent,
    ThacaTextareaComponent,
  ],
  templateUrl: './system-user-detail.component.html',
})
export class SystemUserDetailComponent implements OnInit {
  private systemUserService = inject(SystemUserService);
  private translate = inject(TranslateService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  user = signal<ISystemUserDTO | null>(null);
  roleEntries = signal<{ roleCode: string; permissions: { code: string; effect: string }[] }[]>([]);
  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    { icon: 'pi pi-shield', label: 'menu.access_control' },
    {
      icon: 'pi pi-id-card',
      label: 'menu.system_user_management',
      routerLink: ['/system/system-users'],
    },
  ];

  async ngOnInit(): Promise<void> {
    const userId = this.route.snapshot.paramMap.get('targetUserId');
    if (!userId) {
      GlobalToast.error('system_user.toast.user_id_is_invalid', 'system_user.toast.error');
      this.router.navigate(['/system/system-users']);
      return;
    }

    const response = await this.systemUserService.getSystemUser({ id: +userId } as any);
    if (response.body.status === 'OK') {
      this.user.set(response.body.data);
      this.buildRoleEntries(response.body.data);
      this.breadcrumbItems.push({
        icon: 'pi pi-eye',
        label: this.translate.instant('system_user.detail_title', {
          username: response.body.data.username,
        }),
      });
    } else {
      GlobalToast.error('system_user.toast.user_not_found', 'system_user.toast.error');
      this.router.navigate(['/system/system-users']);
    }
  }

  private buildRoleEntries(user: ISystemUserDTO): void {
    if (!user.roles) return;
    const entries = Object.entries(user.roles).map(([roleCode, perms]) => ({
      roleCode,
      permissions: Object.entries(perms).map(([code, effect]) => ({ code, effect })),
    }));
    this.roleEntries.set(entries);
  }

  getRoleCount(): number {
    return this.roleEntries().length;
  }

  getPermCount(): number {
    return this.roleEntries().reduce((sum, r) => sum + r.permissions.length, 0);
  }

  countGrant(entry: { permissions: { code: string; effect: string }[] }): number {
    return entry.permissions.filter((p) => p.effect === 'GRANT').length;
  }

  countDeny(entry: { permissions: { code: string; effect: string }[] }): number {
    return entry.permissions.filter((p) => p.effect === 'DENY').length;
  }
}
