import { Component, Input, Output, EventEmitter, HostBinding } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { APP_CONFIG_ICONS } from '../../../core/configs/app-config.icon';
import { NgClass } from '@angular/common';

export type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'outline'
  | 'ghost'
  | 'destructive'
  | 'linear';
export type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'thaca-button',
  standalone: true,
  imports: [NgIcon, NgClass],
  templateUrl: './thaca-button.component.html',
  styleUrls: ['./thaca-button.component.scss'],
})
export class ThacaButtonComponent {
  @HostBinding('class') hostClass = 'inline-flex';

  @Input() class = '';
  @Input() variant: ButtonVariant = 'primary';
  @Input() size: ButtonSize = 'lg';
  @Input() loading = false;
  @Input() disabled = false;
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() startIcon?: (typeof APP_CONFIG_ICONS)[keyof typeof APP_CONFIG_ICONS];
  @Input() endIcon?: (typeof APP_CONFIG_ICONS)[keyof typeof APP_CONFIG_ICONS];
  @Input() iconOnly = false;

  @Output() clicked = new EventEmitter<MouseEvent>();

  get isDisabled() {
    return this.disabled || this.loading;
  }

  onClick(event: MouseEvent) {
    if (!this.isDisabled) this.clicked.emit(event);
  }
}
