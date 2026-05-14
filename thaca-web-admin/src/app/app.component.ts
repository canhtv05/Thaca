import { Component, HostListener, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { pageTitle } from './core/stores/app.store';
import { LoadingComponent } from './shared/components/loading/loading.component';
import { ThacaPopupComponent } from './shared/components/thaca-popup/thaca-popup.component';
import { EscapeStackService } from './core/services/escape-stack.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, LoadingComponent, ThacaPopupComponent],
  template: `
    <router-outlet></router-outlet>
    <thaca-popup></thaca-popup>
    <app-loading></app-loading>
  `,
})
export class App {
  private readonly escapeStack = inject(EscapeStackService);

  constructor() {
    pageTitle.set('Thaca Web CMS');
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    this.escapeStack.trigger();
  }
}
