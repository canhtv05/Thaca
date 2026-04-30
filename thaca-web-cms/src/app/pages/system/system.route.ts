import { Routes } from '@angular/router';
import { SystemSettingsComponent } from './system-settings.component';
import { I18nResolver } from '../../core/i18n/i18n.resolver';
import { RoleComponent } from './role/role.component';
import { PermissionComponent } from './permission/permission.component';
import { tenantRoutes } from './tenant/tenant.route';
import { SystemUserComponent } from './system-user/system-user.component';

export const systemRoutes: Routes = [
  ...tenantRoutes,
  {
    path: 'settings',
    children: [
      {
        path: 'mail',
        component: SystemSettingsComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'] },
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
    path: 'system-users',
    component: SystemUserComponent,
    resolve: { i18n: I18nResolver },
    data: { i18n: ['system_user'] },
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
    path: 'excel-test',
    loadComponent: () =>
      import('./excel-test/excel-test.component').then((m) => m.ExcelTestComponent),
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
];
