import { Component, inject, OnInit, OnDestroy, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { LayoutService } from './layout.service';
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
        label: 'Tổng quan',
        items: [{ label: 'Dashboard', icon: 'pi pi-home', routerLink: ['/home'] }],
      },
      {
        label: 'Quản lý',
        items: [
          {
            label: 'UI Components',
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
          {
            label: 'Pages',
            icon: 'pi pi-briefcase',
            path: '/pages',
            items: [
              {
                label: 'Auth',
                icon: 'pi pi-user',
                path: '/auth',
                items: [
                  { label: 'Login', icon: 'pi pi-sign-in', routerLink: ['/auth/login'] },
                  { label: 'Error', icon: 'pi pi-times-circle', routerLink: ['/auth/error'] },
                ],
              },
              { label: 'Crud', icon: 'pi pi-pencil', routerLink: ['/pages/crud'] },
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
