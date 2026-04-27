import { TranslateService } from '@ngx-translate/core';
import { PopupOptions } from '../models/popup.model';
import { PopupService } from '../services/popup.service';

export class Popup {
  private static get popup(): PopupService | null {
    return (window as any).__appGlobal?.popup || null;
  }

  private static get translate(): TranslateService | null {
    return (window as any).__appGlobal?.translate || null;
  }

  private static t(value?: string): string | undefined {
    if (!value) return undefined;
    return this.translate ? this.translate.instant(value) : value;
  }

  private static buildOptions(
    type: 'info' | 'success' | 'warning' | 'error',
    options: PopupOptions = {},
  ): PopupOptions {
    return {
      type,
      title: this.t(options.title),
      message: this.t(options.message),
      acceptText: this.t(options.acceptText),
      cancelText: this.t(options.cancelText),
      showAccept: options.showAccept ?? true,
      showClose: options.showClose ?? true,
      ...options,
    };
  }

  static info(options: PopupOptions = {}): Promise<boolean> {
    if (!this.popup) return Promise.resolve(false);
    return this.popup.open(this.buildOptions('info', options));
  }

  static success(options: PopupOptions = {}): Promise<boolean> {
    if (!this.popup) return Promise.resolve(false);
    return this.popup.open(this.buildOptions('success', options));
  }

  static warning(options: PopupOptions = {}): Promise<boolean> {
    if (!this.popup) return Promise.resolve(false);
    return this.popup.open(this.buildOptions('warning', options));
  }

  static error(options: PopupOptions = {}): Promise<boolean> {
    if (!this.popup) return Promise.resolve(false);
    return this.popup.open(this.buildOptions('error', options));
  }

  static confirm(options: PopupOptions = {}): Promise<boolean> {
    if (!this.popup) {
      return Promise.resolve(false);
    }
    return this.popup.open({
      ...this.buildOptions('warning', options),
      showAccept: options.showAccept ?? true,
      showCancel: options.showCancel ?? true,
      showClose: options.showClose ?? false,
    });
  }
}
