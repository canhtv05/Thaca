import { Injectable, inject, effect, signal, computed } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../../core/services/auth.service';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private authService = inject(AuthService);

  private _menuModel = signal<MenuItem[]>([]);
  readonly menuModel = this._menuModel.asReadonly();

  // Flattened menu items for search
  readonly flatMenu = computed(() => {
    const items: { label: string; icon: string; routerLink: any[]; parentLabel?: string }[] = [];

    const flatten = (menuItems: MenuItem[], parentLabel?: string) => {
      for (const item of menuItems) {
        if (item.routerLink) {
          items.push({
            label: item.label || '',
            icon: item.icon || 'pi pi-circle',
            routerLink: item.routerLink as any[],
            parentLabel,
          });
        }
        if (item.items) {
          flatten(item.items, item.label);
        }
      }
    };

    flatten(this.menuModel());
    return items;
  });

  constructor() {
    effect(() => {
      const user = this.authService.user();
      const isSuperAdmin = user?.isSuperAdmin;

      const systemAdminItems: any[] = [
        {
          label: 'menu.access_control',
          icon: 'pi pi-shield',
          path: '/system',
          items: [
            {
              label: 'menu.system_user_management',
              icon: 'pi pi-id-card',
              routerLink: ['/system/system-users'],
            },
            {
              label: 'menu.role_permission',
              icon: 'pi pi-key',
              routerLink: ['/system/role-permission'],
            },
          ],
        },
        {
          label: 'menu.global_configuration',
          icon: 'pi pi-cog',
          path: '/system/settings',
          items: [
            {
              label: 'menu.mail_settings',
              icon: 'pi pi-envelope',
              routerLink: ['/system/settings/mail'],
            },
            {
              label: 'menu.storage_settings',
              icon: 'pi pi-database',
              routerLink: ['/system/settings/storage'],
            },
            {
              label: 'menu.api_keys',
              icon: 'pi pi-lock',
              routerLink: ['/system/settings/api-keys'],
            },
          ],
        },
        { label: 'menu.system_logs', icon: 'pi pi-server', routerLink: ['/system/logs'] },
        {
          label: 'menu.project_plan',
          icon: 'pi pi-map',
          routerLink: ['/system/project-plan'],
        },
        {
          label: 'Excel Engine Test',
          icon: 'pi pi-file-excel',
          routerLink: ['/system/excel-test'],
        },
      ];

      if (isSuperAdmin) {
        systemAdminItems.unshift({
          label: 'menu.plan_management',
          icon: 'pi pi-list',
          routerLink: ['/system/plans'],
        });
        systemAdminItems.unshift({
          label: 'menu.tenant_management',
          icon: 'pi pi-building',
          routerLink: ['/system/tenants'],
        });
      }

      this._menuModel.set([
        {
          label: 'menu.core',
          items: [
            { label: 'menu.dashboard', icon: 'pi pi-chart-line', routerLink: ['/home'] },
            { label: 'menu.analytics', icon: 'pi pi-chart-bar', routerLink: ['/analytics'] },
            {
              label: 'menu.lock_history',
              icon: 'pi pi-lock',
              routerLink: [`/overview/${user?.id}/lock-history`],
            },
          ],
        },
        {
          label: 'menu.moderation',
          items: [
            {
              label: 'menu.content_management',
              icon: 'pi pi-clone',
              path: '/content',
              items: [
                { label: 'menu.post_list', icon: 'pi pi-list', routerLink: ['/content/posts'] },
                {
                  label: 'menu.comment_list',
                  icon: 'pi pi-comments',
                  routerLink: ['/content/comments'],
                },
                {
                  label: 'menu.media_library',
                  icon: 'pi pi-images',
                  routerLink: ['/content/media'],
                },
              ],
            },
            {
              label: 'menu.report_management',
              icon: 'pi pi-flag',
              path: '/moderation/reports',
              items: [
                {
                  label: 'menu.user_reports',
                  icon: 'pi pi-user-minus',
                  routerLink: ['/moderation/reports/users'],
                },
                {
                  label: 'menu.post_reports',
                  icon: 'pi pi-exclamation-triangle',
                  routerLink: ['/moderation/reports/posts'],
                },
              ],
            },
          ],
        },
        {
          label: 'menu.user_engagement',
          items: [
            {
              label: 'menu.user_management',
              icon: 'pi pi-users',
              path: '/user-management',
              items: [
                {
                  label: 'menu.end_user_list',
                  icon: 'pi pi-user',
                  routerLink: ['/user-management/users'],
                },
                {
                  label: 'menu.verification',
                  icon: 'pi pi-verified',
                  routerLink: ['/user-management/verify'],
                },
              ],
            },
            {
              label: 'menu.point_system',
              icon: 'pi pi-star',
              path: '/engagement/points',
              items: [
                {
                  label: 'menu.point_rules',
                  icon: 'pi pi-list',
                  routerLink: ['/engagement/points/rules'],
                },
                {
                  label: 'menu.point_history',
                  icon: 'pi pi-history',
                  routerLink: ['/engagement/points/history'],
                },
              ],
            },
            { label: 'menu.notifications', icon: 'pi pi-bell', routerLink: ['/notifications'] },
          ],
        },
        {
          label: 'menu.system_administration',
          items: systemAdminItems,
        },
        {
          label: 'menu.development_ui',
          items: [
            {
              label: 'menu.ui_components',
              icon: 'pi pi-th-large',
              path: '/uikit',
              items: [
                { label: 'Form Layout', icon: 'pi pi-id-card', routerLink: ['/uikit/formlayout'] },
                { label: 'Input', icon: 'pi pi-check-square', routerLink: ['/uikit/input'] },
                { label: 'Button', icon: 'pi pi-mobile', routerLink: ['/uikit/button'] },
                { label: 'Table', icon: 'pi pi-table', routerLink: ['/uikit/table'] },
                { label: 'List', icon: 'pi pi-list', routerLink: ['/uikit/list'] },
                { label: 'Panel', icon: 'pi pi-tablet', routerLink: ['/uikit/panel'] },
                { label: 'Chart', icon: 'pi pi-chart-bar', routerLink: ['/uikit/charts'] },
              ],
            },
          ],
        },
      ]);
    });
  }
}
