import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AbstractControl } from '@angular/forms';

@Component({
  selector: 'validation-message',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './validation-message.component.html',
})
export class ValidationMessageComponent {
  @Input({ required: true }) control!: AbstractControl | null;
  @Input() fieldName?: string;
  @Input() i18nPrefix: string = 'validation';
  @Input() customMessages?: Record<string, string>;
  @Input() class: string = '';

  getErrorMessage(): string | null {
    if (!this.control || !this.control.errors || !this.control.touched) {
      return null;
    }
    const errors = this.control.errors;
    const errorKeys = Object.keys(errors);
    if (this.customMessages) {
      for (const key of errorKeys) {
        if (this.customMessages[key]) {
          return this.customMessages[key];
        }
      }
    }
    const errorType = errorKeys[0];
    const messageKey = this.getI18nKey(errorType);

    return messageKey ? `${this.i18nPrefix}.${messageKey}` : null;
  }

  getErrorParams(): any {
    if (!this.control || !this.control.errors) {
      return {};
    }

    const errors = this.control.errors;
    const errorType = Object.keys(errors)[0];
    const errorValue = errors[errorType];

    if (errorType === 'minlength') {
      return { min: errorValue.requiredLength };
    }
    if (errorType === 'maxlength') {
      return { max: errorValue.requiredLength };
    }
    if (errorType === 'min') {
      return { min: errorValue.min };
    }
    if (errorType === 'max') {
      return { max: errorValue.max };
    }

    return errorValue && typeof errorValue === 'object' ? errorValue : {};
  }

  private getI18nKey(errorType: string): string | null {
    if (this.fieldName) {
      const errorMap: Record<string, string> = {
        required: `${this.fieldName}Required`,
        minlength: `${this.fieldName}MinLength`,
        maxlength: `${this.fieldName}MaxLength`,
        email: 'emailInvalid',
        pattern: `${this.fieldName}Pattern`,
        min: `${this.fieldName}Min`,
        max: `${this.fieldName}Max`,
      };
      return errorMap[errorType] ?? errorType;
    }

    const errorMap: Record<string, string> = {
      required: 'required',
      minlength: 'minLength',
      maxlength: 'maxLength',
      email: 'emailInvalid',
      pattern: 'pattern',
      min: 'min',
      max: 'max',
    };

    return errorMap[errorType] ?? errorType;
  }

  shouldShowError(): boolean {
    return !!(this.control && this.control.invalid && this.control.touched);
  }
}
