import { Component, inject, Input, TemplateRef } from '@angular/core';
import { APP_CONFIG_ICONS } from '../../core/configs/app-config.icon';
import { NgIcon } from '@ng-icons/core';
import { I18nService } from '../../core/i18n/i18n.service';
import { ClickOutsideDirective } from '../../shared/directives/click-outside.directive';
import { currentLang as currentLangSignal } from '../../core/stores/app.store';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-auth-layout',
  imports: [NgIcon, ClickOutsideDirective, NgTemplateOutlet],
  templateUrl: './auth-layout.component.html',
})
export class AuthLayoutComponent {
  readonly i18nService = inject(I18nService);
  readonly APP_CONFIG_ICONS = APP_CONFIG_ICONS;
  showDropdown = false;
  readonly currentLang = currentLangSignal;

  @Input() contentTemplate?: TemplateRef<any>;

  onShowDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  onChangeLang(lang: string): void {
    this.i18nService.setLanguage(lang as 'vi' | 'en');
    this.showDropdown = false;
  }
}
