import { Component, inject } from '@angular/core';
import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.component.html',
})
export class HomeComponent {
  protected readonly theme = inject(ThemeService);

  protected toggleTheme(): void {
    document.documentElement.classList.add('changing-theme');
    this.theme.toggle();
    requestAnimationFrame(() => {
      setTimeout(() => document.documentElement.classList.remove('changing-theme'), 0);
    });
  }
}
