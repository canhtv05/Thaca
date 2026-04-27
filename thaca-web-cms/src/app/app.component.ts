import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { pageTitle } from './core/stores/app.store';
import { LoadingComponent } from './shared/components/loading/loading.component';
import { ThacaPopupComponent } from './shared/components/thaca-popup/thaca-popup.component';

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
  constructor() {
    pageTitle.set('Thaca Web CMS');
  }
}
