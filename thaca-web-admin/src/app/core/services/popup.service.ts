import { inject, Injectable, signal } from '@angular/core';
import { PopupOptions } from '../models/popup.model';
import { EscapeStackService } from './escape-stack.service';

@Injectable({ providedIn: 'root' })
export class PopupService {
  private _visible = signal(false);
  private _options = signal<PopupOptions | null>(null);
  private _resolver: ((value: boolean) => void) | null = null;
  private readonly escapeStack = inject(EscapeStackService);
  private readonly _closeHandler = () => this.close();

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
    this.escapeStack.register(this._closeHandler);

    return new Promise((resolve) => {
      this._resolver = resolve;
    });
  }

  accept() {
    const resolve = this._resolver;
    this._resolver = null;
    this.close();
    resolve?.(true);
  }

  close() {
    this._resolver?.(false);
    this._visible.set(false);
    this._options.set(null);
    this._resolver = null;
    this.escapeStack.unregister(this._closeHandler);
  }
}
