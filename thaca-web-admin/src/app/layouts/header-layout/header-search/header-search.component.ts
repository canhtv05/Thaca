import { Component, inject, signal, computed, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { MenuService } from '../../../shared/components/menu-item/menu-item.service';

@Component({
  selector: 'app-header-search',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './header-search.component.html',
  styleUrl: './header-search.component.scss',
})
export class HeaderSearchComponent {
  menuService = inject(MenuService);
  router = inject(Router);
  translateService = inject(TranslateService);

  isSearchOpen = signal(false);
  searchQuery = signal('');
  activeIndex = signal(0);

  // The active list of items to navigate (either search results or quick access)
  activeList = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) {
      return this.menuService.flatMenu().slice(0, 6);
    }
    return this.menuService
      .flatMenu()
      .filter((route) => {
        const translatedLabel = this.translateService.instant(route.label).toLowerCase();
        const translatedParent = route.parentLabel
          ? this.translateService.instant(route.parentLabel).toLowerCase()
          : '';

        return (
          translatedLabel.includes(query) ||
          translatedParent.includes(query) ||
          route.label.toLowerCase().includes(query)
        ); // Fallback to key search
      })
      .slice(0, 15);
  });

  // Grouped results with global index mapping
  groupedResults = computed(() => {
    const items = this.activeList();
    const groups: { label: string; items: any[] }[] = [];

    items.forEach((item, index) => {
      const groupLabel = item.parentLabel || 'common.other';
      let group = groups.find((g) => g.label === groupLabel);
      if (!group) {
        group = { label: groupLabel, items: [] };
        groups.push(group);
      }
      // Add item with its global index
      group.items.push({ ...item, globalIndex: index });
    });

    return groups;
  });

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
      event.preventDefault();
      this.toggleSearch();
      return;
    }

    if (!this.isSearchOpen()) return;

    const listLength = this.activeList().length;

    if (event.key === 'Escape') {
      this.closeSearch();
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.activeIndex.update((i) => (i + 1) % listLength);
    }

    if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.activeIndex.update((i) => (i - 1 + listLength) % listLength);
    }

    if (event.key === 'Enter') {
      event.preventDefault();
      this.onEnter();
    }
  }

  toggleSearch() {
    this.isSearchOpen.update((v) => !v);
    if (this.isSearchOpen()) {
      this.searchQuery.set('');
      this.activeIndex.set(0);
      setTimeout(() => {
        const input = document.getElementById('route-search-input');
        input?.focus();
      }, 0);
    }
  }

  closeSearch() {
    this.isSearchOpen.set(false);
    this.searchQuery.set('');
  }

  clearSearch() {
    this.searchQuery.set('');
    this.activeIndex.set(0);
    const input = document.getElementById('route-search-input') as HTMLInputElement;
    if (input) input.value = '';
    input?.focus();
  }

  onSearchInput(event: any) {
    this.searchQuery.set(event.target.value);
    this.activeIndex.set(0);
  }

  onEnter() {
    const items = this.activeList();
    if (items.length > 0) {
      this.navigateTo(items[this.activeIndex()]);
    }
  }

  navigateTo(route: any) {
    this.router.navigate(route.routerLink);
    this.closeSearch();
  }
}
