import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'thaca-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<'light' | 'dark'>('light');

  constructor() {
    if (typeof document === 'undefined' || typeof localStorage === 'undefined') {
      return;
    }
    const stored = localStorage.getItem(STORAGE_KEY) as 'light' | 'dark' | null;
    const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
    const initial = stored ?? (prefersDark ? 'dark' : 'light');
    this.applyTheme(initial, false);
  }

  toggle(): void {
    this.applyTheme(this.theme() === 'dark' ? 'light' : 'dark', true);
  }

  setTheme(mode: 'light' | 'dark'): void {
    this.applyTheme(mode, true);
  }

  private applyTheme(mode: 'light' | 'dark', persist: boolean): void {
    this.theme.set(mode);
    document.documentElement.classList.toggle('dark', mode === 'dark');
    if (persist) {
      localStorage.setItem(STORAGE_KEY, mode);
    }
  }
}
