import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import { isLoading } from '../../../core/stores/app.store';
import { GlobalToast } from '../../../core/global/global-toast';
import { IUserDTO } from '../user.model';
import { UserService } from '../user.service';
import { AuditDetailComponent } from '../../../shared/components/audit-detail/audit-detail.component';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    BreadcrumbComponent,
    ThacaInputComponent,
    AuditDetailComponent,
  ],
  templateUrl: './user-detail.component.html',
})
export class UserDetailComponent implements OnInit {
  private userService = inject(UserService);
  private translate = inject(TranslateService);
  private router = inject(Router);
  readonly isLoading = isLoading;
  private route = inject(ActivatedRoute);

  user = signal<IUserDTO | null>(null);
  breadcrumbItems: MenuItem[] = [
    { icon: 'pi pi-cog', label: 'menu.system_administration' },
    {
      icon: 'pi pi-user',
      label: 'menu.user_management',
      routerLink: ['/user-management/users'],
    },
  ];

  async ngOnInit(): Promise<void> {
    const username = this.route.snapshot.paramMap.get('username') || '';
    if (username) {
      this.breadcrumbItems.push({
        icon: 'pi pi-eye',
        label: this.translate.instant('user.detail_title', { username: username }),
      });
      const response = await this.userService.getUser({ username: username });
      if (response.body.status === 'OK') {
        this.user.set(response.body.data);
      }
    } else {
      GlobalToast.error('user.toast.username_is_invalid', 'user.toast.error');
      this.router.navigate(['/user-management/users']);
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
