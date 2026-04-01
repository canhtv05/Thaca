import { Component, model } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { APP_CONFIG_ICONS } from '../../core/configs/app-config.icon';

@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet, FormsModule],
  templateUrl: './auth-layout.component.html',
})
export class AuthLayoutComponent {
  readonly APP_CONFIG_ICONS = APP_CONFIG_ICONS;
  cities = [
    { name: 'New York', code: 'NY' },
    { name: 'Rome', code: 'RM' },
  ];
  /** Hai chiều với `[(ngModel)]` trên `app-thaca-select`. */
  selectedCity = model<string>('NY');
}
