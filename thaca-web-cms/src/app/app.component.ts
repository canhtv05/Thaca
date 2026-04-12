import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { pageTitle } from './core/stores/app.store';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`,
})
export class App {
  constructor() {
    pageTitle.set('Thaca Web CMS');
  }
}
