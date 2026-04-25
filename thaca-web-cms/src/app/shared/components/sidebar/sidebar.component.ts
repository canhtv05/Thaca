import { Component, inject, OnInit, OnDestroy, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from '../menu-item/menu-item.component';
import { LayoutService } from './sidebar.service';
import { filter, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule, AppMenuitem],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class Sidebar implements OnInit, OnDestroy {
  model: MenuItem[] = [];
  layoutService = inject(LayoutService);
  router = inject(Router);
  el = inject(ElementRef);

  private destroy$ = new Subject<void>();

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.layoutService.isDesktop() && this.layoutService.isMobileMenuActive()) {
      const sidebarEl = this.el.nativeElement;
      const toggleBtn = document.querySelector('[data-sidebar-toggle]');

      const isOutside =
        !sidebarEl?.contains(event.target as Node) && !toggleBtn?.contains(event.target as Node);

      if (isOutside) {
        this.layoutService.closeMobileMenu();
      }
    }
  }

  ngOnInit() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$),
      )
      .subscribe((event) => {
        const navEvent = event as NavigationEnd;
        this.onRouteChange(navEvent.urlAfterRedirects);
      });

    this.onRouteChange(this.router.url);

    this.model = [
      {
        label: 'menu.core',
        items: [
          { label: 'menu.dashboard', icon: 'pi pi-chart-line', routerLink: ['/home'] },
          { label: 'menu.analytics', icon: 'pi pi-chart-bar', routerLink: ['/analytics'] },
          { label: 'menu.login_history', icon: 'pi pi-history', routerLink: ['/login-history'] },
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
              { label: 'menu.media_library', icon: 'pi pi-images', routerLink: ['/content/media'] },
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
                routerLink: ['/user-management/list'],
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
        items: [
          {
            label: 'menu.access_control',
            icon: 'pi pi-shield',
            path: '/system/access',
            items: [
              {
                label: 'menu.admin_accounts',
                icon: 'pi pi-id-card',
                routerLink: ['/system/admins'],
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
        ],
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
    ];
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private onRouteChange(path: string) {
    this.layoutService.layoutState.update((val) => ({
      ...val,
      activePath: path,
      mobileMenuActive: false,
    }));
  }
}
