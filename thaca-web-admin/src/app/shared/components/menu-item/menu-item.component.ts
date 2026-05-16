import { Component, computed, inject, input, signal } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { LayoutService } from '../sidebar/sidebar.service';
import { TooltipModule } from 'primeng/tooltip';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: '[app-menuitem]',
  imports: [CommonModule, RouterModule, TooltipModule, TranslateModule],
  templateUrl: './menu-item.component.html',
  styleUrl: './menu-item.component.scss',
  host: {
    '[class.active-menuitem]': 'isActive()',
    '[class.root-menuitem]': 'root()',
    '[class.layout-collapsed]': 'layoutService.isSidebarCollapsed() && layoutService.isDesktop()',
  },
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
    const menuPath = this.fullPath();
    if (!this.item()?.path || !activePath || !menuPath) return false;

    // User toggled this section manually
    if (activePath === menuPath) return true;

    // Current URL belongs to a child route of this section
    return this.matchesDescendantRoute(activePath);
  });

  private matchesDescendantRoute(url: string): boolean {
    return this.collectRouterLinks(this.item()).some((route) => this.urlMatchesRoute(url, route));
  }

  private collectRouterLinks(item: any): string[] {
    if (!item) return [];

    const links: string[] = [];
    if (item.routerLink) {
      const route = Array.isArray(item.routerLink)
        ? item.routerLink.join('')
        : String(item.routerLink);
      links.push(route.startsWith('/') ? route : `/${route}`);
    }
    if (item.items) {
      for (const child of item.items) {
        links.push(...this.collectRouterLinks(child));
      }
    }
    return links;
  }

  private urlMatchesRoute(url: string, route: string): boolean {
    return url === route || url.startsWith(`${route}/`);
  }

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

    const route = Array.isArray(item.routerLink)
      ? item.routerLink.join('')
      : String(item.routerLink);
    const normalizedRoute = route.startsWith('/') ? route : `/${route}`;
    const currentUrl = this.router.url.split('?')[0];
    const isRouteActive = this.urlMatchesRoute(currentUrl, normalizedRoute);

    if (isRouteActive) {
      const parentMenuPath = this.parentPath();
      if (parentMenuPath) {
        this.layoutService.layoutState.update((val) => ({
          ...val,
          activePath: parentMenuPath,
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
