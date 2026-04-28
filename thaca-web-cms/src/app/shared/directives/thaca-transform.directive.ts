import { Directive, HostListener, Input, Optional, Self, ElementRef } from '@angular/core';
import { NgControl } from '@angular/forms';
import { CommonUtils } from '../utils/common.utils';

@Directive({
  selector: '[thacaTransform]',
  standalone: true,
})
export class ThacaTransformDirective {
  @Input() noAccent = false;
  @Input() noSpace = false;
  @Input() trim = false;
  @Input() lowercase = false;
  @Input() uppercase = false;

  constructor(
    @Optional() @Self() private ngControl: NgControl,
    private el: ElementRef,
  ) {}

  @HostListener('input', ['$event'])
  onInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = input?.value;
    if (!value) return;

    let result = value;

    if (this.noAccent) {
      result = CommonUtils.removeVietnameseTones(result);
    }

    if (this.noSpace) {
      result = result.replace(/\s+/g, '');
    }

    if (this.lowercase) {
      result = result.toLowerCase();
    } else if (this.uppercase) {
      result = result.toUpperCase();
    }

    this.updateValue(result);
  }

  @HostListener('blur', ['$event'])
  onBlur(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = input?.value;
    if (!value) return;
    let result = value;

    // Phải xử lý lại các transform khác vì giá trị có thể đã thay đổi
    if (this.noAccent) result = CommonUtils.removeVietnameseTones(result);
    if (this.noSpace) result = result.replace(/\s+/g, '');
    if (this.lowercase) result = result.toLowerCase();
    else if (this.uppercase) result = result.toUpperCase();

    if (this.trim) {
      result = result.trim();
    }

    this.updateValue(result);
  }

  private updateValue(val: string) {
    if (this.ngControl) {
      this.ngControl.control?.setValue(val, { emitEvent: false });
      this.ngControl.valueAccessor?.writeValue(val);
    }
  }
}
