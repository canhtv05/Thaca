import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { pageTitle } from './core/stores/app.store';
import { HttpClient } from '@angular/common/http';
import { LoadingComponent } from './shared/components/loading/loading.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, LoadingComponent],
  template: `
    <router-outlet></router-outlet>
    <app-loading></app-loading>
  `,
})
export class App implements OnInit {
  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {
    pageTitle.set('Thaca Web CMS');
    (window as any).__appGlobal = {
      httpClient: this.http,
    };
  }

  async ngOnInit(): Promise<void> {
    await this.authService.getUserProfile();
  }
}
