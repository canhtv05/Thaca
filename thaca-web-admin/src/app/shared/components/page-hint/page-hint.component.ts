import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type PageHintVariant = 'info' | 'success' | 'warning' | 'primary' | 'secondary';

const DEFAULT_ICONS: Record<PageHintVariant, string> = {
  info: 'pi pi-info-circle',
  success: 'pi pi-check-circle',
  warning: 'pi pi-exclamation-triangle',
  primary: 'pi pi-shield',
  secondary: 'pi pi-list',
};

const CARD_CLASSES: Record<PageHintVariant, string> = {
  info: 'card-info',
  success: 'card-success',
  warning: 'card-warning',
  primary: 'card-primary',
  secondary: 'card-secondary',
};

@Component({
  selector: 'app-page-hint',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './page-hint.component.html',
  host: { class: 'block' },
})
export class PageHintComponent {
  @Input() variant: PageHintVariant = 'info';
  @Input() icon?: string;
  @Input() title?: string;

  get cardClass(): string {
    return CARD_CLASSES[this.variant];
  }

  get resolvedIcon(): string {
    return this.icon ?? DEFAULT_ICONS[this.variant];
  }
}
