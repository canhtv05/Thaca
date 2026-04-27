import {
  Component,
  inject,
  OnInit,
  OnDestroy,
  ElementRef,
  HostListener,
  effect,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from '../menu-item/menu-item.component';
import { LayoutService } from './sidebar.service';
import { filter, Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { MenuService } from '../../../core/services/menu.service';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule, AppMenuitem],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class Sidebar implements OnInit, OnDestroy {
  menuService = inject(MenuService);
  layoutService = inject(LayoutService);
  router = inject(Router);
  el = inject(ElementRef);
  authService = inject(AuthService);

  private destroy$ = new Subject<void>();

  get model(): MenuItem[] {
    return this.menuService.menuModel();
  }

  constructor() {}

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
