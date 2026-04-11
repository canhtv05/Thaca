import { signal } from '@angular/core';

export const pageTitle = signal<string>('Thaca Web CMS');
export const currentLang = signal<string>(localStorage.getItem('lang') || 'vi');
export const currentUser = signal<any | null>(null);
