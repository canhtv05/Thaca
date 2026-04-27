import { Injectable, signal } from '@angular/core';
import { PopupOptions } from '../models/popup.model';

@Injectable({ providedIn: 'root' })
export class PopupService {
  private _visible = signal(false);
  private _options = signal<PopupOptions | null>(null);
  private _resolver: ((value: boolean) => void) | null = null;

  visible = this._visible.asReadonly();
  options = this._options.asReadonly();

  open(options: PopupOptions): Promise<boolean> {
    this._options.set({
      showClose: false,
      showCancel: true,
      showAccept: true,
      acceptText: 'common.button.ok',
      cancelText: 'common.button.cancel',
      type: 'info',
      ...options,
    });

    this._visible.set(true);

    return new Promise((resolve) => {
      this._resolver = resolve;
    });
  }

  accept() {
    this._resolver?.(true);
    this.close();
  }

  close() {
    this._resolver?.(false);
    this._visible.set(false);
    this._options.set(null);
    this._resolver = null;
  }
}
