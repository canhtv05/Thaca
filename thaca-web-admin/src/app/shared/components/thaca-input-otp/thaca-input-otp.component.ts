import { Component, forwardRef, HostBinding, Input, signal, Injector, inject } from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  NgControl,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { InputOtpModule } from 'primeng/inputotp';

@Component({
  selector: 'thaca-input-otp',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, InputOtpModule],
  templateUrl: './thaca-input-otp.component.html',
  styleUrls: ['./thaca-input-otp.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ThacaInputOtpComponent),
      multi: true,
    },
  ],
})
export class ThacaInputOtpComponent implements ControlValueAccessor {
  @Input() size: 'sm' | 'md' | 'lg' = 'lg';
  @Input() label?: string;
  @Input() required?: boolean;
  @Input() id: string = `thaca-input-otp-${Math.random().toString(36).substring(2, 15)}`;

  @Input() length: number = 4;
  @Input() integerOnly: boolean = false;
  @Input() mask: boolean = false;

  @HostBinding('class')
  get hostClass(): string {
    return `w-full thi--${this.size}`;
  }

  value = signal<any>(null);
  disabled = false;
  private injector = inject(Injector);
  private _ngControl?: NgControl | null;

  get ngControl(): NgControl | null {
    if (this._ngControl === undefined) {
      this._ngControl = this.injector.get(NgControl, null, { self: true });
      if (this._ngControl) {
        this._ngControl.valueAccessor = this;
      }
    }
    return this._ngControl;
  }

  onChange = (value: any) => {};
  onTouched = () => {};

  writeValue(value: any): void {
    this.value.set(value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onModelChange(val: any) {
    this.value.set(val);
    this.onChange(val);
  }

  hasError() {
    const control = this.ngControl?.control;
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
