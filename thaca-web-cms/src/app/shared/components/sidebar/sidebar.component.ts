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
        label: 'menu.overview',
        items: [{ label: 'menu.dashboard', icon: 'pi pi-home', routerLink: ['/home'] }],
      },
      {
        label: 'menu.user_management',
        items: [
          {
            label: 'menu.user_list',
            icon: 'pi pi-users',
            routerLink: ['/user-management/list'],
          },
          {
            label: 'menu.permissions',
            icon: 'pi pi-shield',
            routerLink: ['/user-management/permissions'],
          },
          {
            label: 'menu.login_history',
            icon: 'pi pi-history',
            routerLink: ['/user-management/login-history'],
          },
        ],
      },
      {
        label: 'menu.ui_components',
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
      {
        label: 'menu.pages',
        items: [
          {
            label: 'menu.auth',
            icon: 'pi pi-lock',
            path: '/auth',
            items: [
              { label: 'menu.login', icon: 'pi pi-sign-in', routerLink: ['/auth/login'] },
              { label: 'menu.error', icon: 'pi pi-times-circle', routerLink: ['/auth/error'] },
            ],
          },
          { label: 'menu.crud', icon: 'pi pi-pencil', routerLink: ['/pages/crud'] },
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
