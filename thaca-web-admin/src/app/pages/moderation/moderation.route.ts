import { Routes } from '@angular/router';
import { PostManagementComponent } from './post-management.component';
import { CommentManagementComponent } from './comment-management.component';
import { ReportManagementComponent } from './report-management.component';
import { I18nResolver } from '../../core/i18n/i18n.resolver';

export const moderationRoutes: Routes = [
  {
    path: 'posts',
    component: PostManagementComponent,
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
  {
    path: 'comments',
    component: CommentManagementComponent,
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
  {
    path: 'reports',
    children: [
      {
        path: 'users',
        component: ReportManagementComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'], type: 'users' },
      },
      {
        path: 'posts',
        component: ReportManagementComponent,
        resolve: { i18n: I18nResolver },
        data: { i18n: ['common'], type: 'posts' },
      },
    ],
  },
  {
    path: 'media',
    component: PostManagementComponent, // Reuse or create Media component
    resolve: { i18n: I18nResolver },
    data: { i18n: ['common'] },
  },
];
