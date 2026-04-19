import { Component, computed, inject, input, signal } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { LayoutService } from './layout.service';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: '[app-menuitem]',
  imports: [CommonModule, RouterModule, TooltipModule],
  template: `
    <!-- ═══ ROOT GROUP: section label + direct children ═══ -->
    @if (root() && hasChildren() && isVisible()) {
      <!-- Section label -->
      @if (!layoutService.isSidebarCollapsed()) {
        <div class="menu-section-label">
          <span>{{ item().label }}</span>
        </div>
      } @else {
        <div class="menu-section-label collapsed">
          <div class="section-dot"></div>
        </div>
      }

      <!-- Directly render children -->
      <ul class="menu-root-list">
        @for (child of item().items; track child?.label) {
          <li app-menuitem [item]="child" [parentPath]="fullPath()" [root]="false"></li>
        }
      </ul>
    }

    <!-- ═══ NON-ROOT: parent with children (no routerLink) ═══ -->
    @if (!root() && (!hasRouterLink() || hasChildren()) && isVisible()) {
      <a
        [attr.href]="item().url"
        (click)="itemClick($event)"
        [attr.target]="item().target"
        class="menu-link"
        [class.active]="isActive()"
        [pTooltip]="layoutService.isSidebarCollapsed() ? item().label : ''"
        tooltipPosition="right"
        tabindex="0"
      >
        <i [ngClass]="item().icon" class="menu-icon"></i>
        @if (!layoutService.isSidebarCollapsed()) {
          <span class="menu-text">{{ item().label }}</span>
        }
        @if (hasChildren() && !layoutService.isSidebarCollapsed()) {
          <i class="pi pi-chevron-down menu-chevron" [class.rotated]="isActive()"></i>
        }
      </a>
    }

    <!-- ═══ NON-ROOT: leaf link with routerLink ═══ -->
    @if (!root() && hasRouterLink() && !hasChildren() && isVisible()) {
      <a
        (click)="itemClick($event)"
        [routerLink]="item().routerLink"
        routerLinkActive="active-route"
        [routerLinkActiveOptions]="
          item().routerLinkActiveOptions || {
            paths: 'exact',
            queryParams: 'ignored',
            matrixParams: 'ignored',
            fragment: 'ignored',
          }
        "
        [fragment]="item().fragment"
        [queryParamsHandling]="item().queryParamsHandling"
        [preserveFragment]="item().preserveFragment"
        [skipLocationChange]="item().skipLocationChange"
        [replaceUrl]="item().replaceUrl"
        [state]="item().state"
        [queryParams]="item().queryParams"
        [attr.target]="item().target"
        class="menu-link"
        [class.active]="isActive()"
        [pTooltip]="layoutService.isSidebarCollapsed() ? item().label : ''"
        tooltipPosition="right"
        tabindex="0"
      >
        <i [ngClass]="item().icon" class="menu-icon"></i>
        @if (!layoutService.isSidebarCollapsed()) {
          <span class="menu-text">{{ item().label }}</span>
        }
      </a>
    }

    <!-- ═══ NON-ROOT: children submenu ═══ -->
    @if (!root() && hasChildren() && isVisible() && isActive()) {
      @if (!layoutService.isSidebarCollapsed()) {
        <ul class="submenu">
          @for (child of item().items; track child?.label) {
            <li app-menuitem [item]="child" [parentPath]="fullPath()" [root]="false"></li>
          }
        </ul>
      }
    }
  `,
  host: {
    '[class.active-menuitem]': 'isActive()',
    '[class.root-menuitem]': 'root()',
  },
  styles: [
    `
      :host {
        display: block;
      }

      /* ─── Section Label ─── */
      .menu-section-label {
        padding: 18px 16px 6px;
        font-size: 0.65rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--color-muted-foreground);
        opacity: 0.6;
        white-space: nowrap;
        overflow: hidden;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      .menu-section-label.collapsed {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 14px 0 6px;
      }

      .section-dot {
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: var(--color-muted-foreground);
        opacity: 0.4;
      }

      .menu-root-list {
        list-style: none;
        padding: 0;
        margin: 0;
      }

      /* ─── Menu Link ─── */
      .menu-link {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 9px 16px;
        margin: 1px 8px;
        border-radius: var(--radius-md);
        color: var(--color-foreground);
        opacity: 0.7;
        font-size: 0.835rem;
        font-weight: 500;
        text-decoration: none;
        cursor: pointer;
        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        white-space: nowrap;
        overflow: hidden;
      }

      .menu-link:hover {
        background: var(--color-accent);
        opacity: 1;
      }

      .menu-link.active-route {
        background: var(--color-primary);
        color: var(--color-primary-foreground);
        opacity: 1;
        font-weight: 600;
        box-shadow: 0 1px 3px color-mix(in oklch, var(--color-primary) 25%, transparent);
      }

      /* ─── Icon ─── */
      .menu-icon {
        font-size: 1.1rem;
        width: 20px;
        text-align: center;
        flex-shrink: 0;
      }

      .menu-text {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .menu-chevron {
        font-size: 0.7rem;
        margin-left: auto;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        opacity: 0.5;
      }

      .menu-chevron.rotated {
        transform: rotate(180deg);
      }

      /* ─── Submenu ─── */
      .submenu {
        list-style: none;
        padding: 2px 0 2px 20px;
        margin: 0;
        overflow: hidden;
      }

      .submenu .menu-link {
        font-size: 0.8rem;
        padding: 7px 16px;
        opacity: 0.6;
      }

      .submenu .menu-link:hover {
        opacity: 1;
      }

      .submenu .menu-link.active-route {
        background: color-mix(in oklch, var(--color-primary) 15%, transparent);
        color: var(--color-primary);
        box-shadow: none;
      }
    `,
  ],
})
export class AppMenuitem {
  layoutService = inject(LayoutService);

  router = inject(Router);

  item = input<any>(null);

  root = input<boolean>(false);

  parentPath = input<string | null>(null);

  isVisible = computed(() => this.item()?.visible !== false);

  hasChildren = computed(() => this.item()?.items && this.item()?.items.length > 0);

  hasRouterLink = computed(() => !!this.item()?.routerLink);

  fullPath = computed(() => {
    const itemPath = this.item()?.path;
    if (!itemPath) return this.parentPath();
    const parent = this.parentPath();
    if (parent && !itemPath.startsWith(parent)) {
      return parent + itemPath;
    }
    return itemPath;
  });

  isActive = computed(() => {
    const activePath = this.layoutService.layoutState().activePath;
    if (this.item()?.path) {
      return activePath?.startsWith(this.fullPath() ?? '') ?? false;
    }
    return false;
  });

  initialized = signal<boolean>(false);

  constructor() {
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      if (this.item()?.routerLink) {
        this.updateActiveStateFromRoute();
      }
    });
  }

  ngOnInit() {
    if (this.item()?.routerLink) {
      this.updateActiveStateFromRoute();
    }
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initialized.set(true);
    });
  }

  updateActiveStateFromRoute() {
    const item = this.item();
    if (!item?.routerLink) return;

    const isRouteActive = this.router.isActive(item.routerLink[0], {
      paths: 'exact',
      queryParams: 'ignored',
      matrixParams: 'ignored',
      fragment: 'ignored',
    });

    if (isRouteActive) {
      const parentPath = this.parentPath();
      if (parentPath) {
        this.layoutService.layoutState.update((val) => ({
          ...val,
          activePath: parentPath,
        }));
      }
    }
  }

  itemClick(event: Event) {
    const item = this.item();

    if (item?.disabled) {
      event.preventDefault();
      return;
    }

    if (item?.command) {
      item.command({ originalEvent: event, item: item });
    }

    if (this.hasChildren()) {
      if (this.isActive()) {
        this.layoutService.layoutState.update((val) => ({
          ...val,
          activePath: this.parentPath(),
        }));
      } else {
        this.layoutService.layoutState.update((val) => ({
          ...val,
          activePath: this.fullPath(),
        }));
      }
    } else {
      // Close mobile menu on leaf click
      if (!this.layoutService.isDesktop()) {
        this.layoutService.closeMobileMenu();
      }
    }
  }
}
