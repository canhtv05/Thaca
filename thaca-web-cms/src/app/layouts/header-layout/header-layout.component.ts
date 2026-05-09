import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LayoutService } from '../../shared/components/sidebar/sidebar.service';
import { ThemeService } from '../../core/theme/theme.service';
import { currentUser } from '../../core/stores/app.store';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { Router, RouterLink } from '@angular/router';
import { ClickOutsideDirective } from '../../shared/directives/click-outside.directive';
import { HeaderSearchComponent } from './header-search/header-search.component';
import { APP_CONFIG_ICONS } from '../../core/configs/app-config.icon';
import { I18nService } from '../../core/i18n/i18n.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header-layout',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    ClickOutsideDirective,
    RouterLink,
    HeaderSearchComponent,
  ],
  templateUrl: './header-layout.component.html',
  styleUrl: './header-layout.component.scss',
})
export class HeaderLayoutComponent {
  layoutService = inject(LayoutService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  i18nService = inject(I18nService);
  router = inject(Router);
  authService = inject(AuthService);

  currentUser = currentUser;
  APP_CONFIG_ICONS = APP_CONFIG_ICONS;

  isUserMenuOpen = signal(false);
  isLangOpen = signal(false);

  currentLang = signal(this.translateService.currentLang || 'vi');

  toggleSidebar() {
    this.layoutService.toggleSidebar();
  }

  toggleTheme() {
    this.themeService.toggle();
  }

  toggleUserMenu() {
    this.isUserMenuOpen.update((v) => !v);
  }

  closeUserMenu() {
    this.isUserMenuOpen.set(false);
  }

  toggleLang() {
    this.isLangOpen.update((v) => !v);
  }

  closeLang() {
    this.isLangOpen.set(false);
  }

  onChangeLang(lang: string) {
    this.i18nService.setLanguage(lang as 'vi' | 'en');
    this.currentLang.set(lang);
    this.closeUserMenu();
  }

  async logout() {
    await this.authService.logoutAsync();
    this.router.navigate(['/login']);
  }
}
