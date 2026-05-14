import { signal } from '@angular/core';
import { IAuthUserDTO } from '../models/auth.model';

export const pageTitle = signal<string>('Thaca Web CMS');
export const currentLang = signal<string>(localStorage.getItem('lang') || 'vi');
export const currentUser = signal<IAuthUserDTO | null>(null);
export const isLoading = signal<boolean>(false);
