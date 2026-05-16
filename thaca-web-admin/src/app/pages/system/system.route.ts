import { Routes } from '@angular/router';
import { SystemSettingsComponent } from './system-settings.component';
import { I18nResolver } from '../../core/i18n/i18n.resolver';
import { RoleComponent } from './role/role.component';
import { PermissionComponent } from './permission/permission.component';
import { tenantRoutes } from './tenant/tenant.route';
import { systemUserRoutes } from './system-user/system-user.route';

export const systemRoutes: Routes = [
  ...tenantRoutes,
  ...systemUserRoutes,
  {
    path: 'settings',
    children: [
      {
        path: 'mail-config',
        loadComponent: () =>
          import('./mail-config/mail-config.component').then((m) => m.MailConfigComponent),
        resolve: { i18n: I18nResolver },
        data: { i18n: ['mail_config'] },
      },
      {
        path: 'storage',
        component: SystemSettingsComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
      },
      {
        path: 'api-keys',
        component: SystemSettingsComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
      },
    ],
  },
  {
    path: 'logs',
    component: SystemSettingsComponent,
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
  {
    path: 'role-permission',
    resolve: { i18n: I18nResolver },
    data: { i18n: ['role_permission'] },
    children: [
      {
        path: '',
        component: RoleComponent,
      },
      {
        path: ':roleCode/permissions',
        component: PermissionComponent,
      },
    ],
  },
  {
    path: 'plans',
    loadComponent: () => import('./plan/plan.component').then((m) => m.PlanComponent),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common', 'plan'] },
  },
  {
    path: 'project-plan',
    loadComponent: () =>
      import('./project-plan/project-plan.component').then((m) => m.ProjectPlanComponent),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common', 'project_plan'] },
  },
  {
    path: 'excel-test',
    loadComponent: () =>
      import('./excel-test/excel-test.component').then((m) => m.ExcelTestComponent),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
];
