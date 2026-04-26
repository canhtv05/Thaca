import { ToastrService } from 'ngx-toastr';
import { TranslateService } from '@ngx-translate/core';

export class GlobalToast {
  private static get toastr(): ToastrService | null {
    return (window as any).__appGlobal?.toastr || null;
  }

  private static get translate(): TranslateService | null {
    return (window as any).__appGlobal?.translate || null;
  }

  private static getTranslated(key: string): string {
    if (!key) return key;
    return this.translate ? this.translate.instant(key) : key;
  }

  static success(messageKey: string, titleKey?: string) {
    if (this.toastr) {
      this.toastr.success(
        this.getTranslated(messageKey),
        titleKey ? this.getTranslated(titleKey) : undefined,
      );
    }
  }

  static error(messageKey: string, titleKey?: string) {
    if (this.toastr) {
      this.toastr.error(
        this.getTranslated(messageKey),
        titleKey ? this.getTranslated(titleKey) : undefined,
      );
    }
  }

  static warn(messageKey: string, titleKey?: string) {
    if (this.toastr) {
      this.toastr.warning(
        this.getTranslated(messageKey),
        titleKey ? this.getTranslated(titleKey) : undefined,
      );
    }
  }

  static info(messageKey: string, titleKey?: string) {
    if (this.toastr) {
      this.toastr.info(
        this.getTranslated(messageKey),
        titleKey ? this.getTranslated(titleKey) : undefined,
      );
    }
  }
}
