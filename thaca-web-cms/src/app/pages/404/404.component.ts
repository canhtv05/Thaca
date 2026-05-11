import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink, TranslateModule],
  template: `
    <main class="flex min-h-screen flex-col items-center justify-center gap-3 p-6 text-center">
      <h1 class="text-6xl font-bold">404</h1>
      <p class="text-xl font-semibold">{{ 'error.notFoundTitle' | translate }}</p>
      <p class="text-gray-500">{{ 'error.notFoundDescription' | translate }}</p>
      <a routerLink="/auth/platform" class="font-semibold underline">{{
        'error.backToLogin' | translate
      }}</a>
    </main>
  `,
})
export class NotFoundComponent {}
