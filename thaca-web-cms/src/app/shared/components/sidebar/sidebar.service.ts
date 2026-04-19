import { Injectable, signal, computed } from '@angular/core';

export interface LayoutState {
  sidebarCollapsed: boolean;
  mobileMenuActive: boolean;
  activePath: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class LayoutService {
  private readonly BREAKPOINT = 991;

  layoutState = signal<LayoutState>({
    sidebarCollapsed: false,
    mobileMenuActive: false,
    activePath: null,
  });

  isSidebarCollapsed = computed(() => this.layoutState().sidebarCollapsed);

  isMobileMenuActive = computed(() => this.layoutState().mobileMenuActive);

  toggleSidebar() {
    if (this.isDesktop()) {
      this.layoutState.update((s) => ({
        ...s,
        sidebarCollapsed: !s.sidebarCollapsed,
      }));
    } else {
      this.layoutState.update((s) => ({
        ...s,
        mobileMenuActive: !s.mobileMenuActive,
      }));
    }
  }

  closeMobileMenu() {
    this.layoutState.update((s) => ({
      ...s,
      mobileMenuActive: false,
    }));
  }

  isDesktop(): boolean {
    return window.innerWidth > this.BREAKPOINT;
  }
}
