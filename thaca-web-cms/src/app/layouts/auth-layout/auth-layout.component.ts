import { Component, inject, Input, TemplateRef } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { APP_CONFIG_ICONS } from '../../core/configs/app-config.icon';
import { NgIcon } from '@ng-icons/core';
import { I18nService } from '../../core/i18n/i18n.service';
import { ClickOutsideDirective } from '../../shared/directives/click-outside.directive';
import { currentLang as currentLangSignal } from '../../core/stores/app.store';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-auth-layout',
  imports: [NgIcon, ClickOutsideDirective, NgTemplateOutlet, TranslateModule],
  templateUrl: './auth-layout.component.html',
  styleUrls: ['./auth-layout.component.scss'],
})
export class AuthLayoutComponent {
  readonly i18nService = inject(I18nService);
  readonly router = inject(Router);
  readonly APP_CONFIG_ICONS = APP_CONFIG_ICONS;
  showDropdown = false;
  readonly currentLang = currentLangSignal;

  @Input() contentTemplate?: TemplateRef<any>;
  @Input() showSuperAdmin = true;
  @Input() showFooter = true;

  onShowDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  onChangeLang(lang: string): void {
    this.i18nService.setLanguage(lang as 'vi' | 'en');
    this.showDropdown = false;
  }

  onSelectSuperAdmin(): void {
    this.router.navigate(['/auth/login'], { state: { type: 'SUPER_ADMIN' } });
  }

  isShowRegisterWorkspace(): boolean {
    return this.router.url !== '/auth/platform';
  }
}
