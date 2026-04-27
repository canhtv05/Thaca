import { Component, forwardRef, HostBinding, Input, signal, Injector, inject } from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  NgControl,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'thaca-textarea',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './thaca-textarea.component.html',
  styleUrls: ['./thaca-textarea.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ThacaTextareaComponent),
      multi: true,
    },
  ],
})
export class ThacaTextareaComponent implements ControlValueAccessor {
  @Input() label?: string;
  @Input() placeholder = '';
  @Input() required = false;
  @Input() rows = 3;
  @Input() id = `thaca-textarea-${Math.random().toString(36).substring(2, 9)}`;

  @HostBinding('class')
  get hostClass() {
    return 'w-full block';
  }

  value = signal<string>('');
  disabled = false;

  private onChange = (_: any) => {};
  private onTouched = () => {};
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

  writeValue(value: any): void {
    this.value.set(value ?? '');
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

  onInput(event: Event): void {
    const val = (event.target as HTMLTextAreaElement).value;
    this.value.set(val);
    this.onChange(val);
  }

  onBlur(): void {
    this.onTouched();
  }

  hasError(): boolean {
    const control = this.ngControl?.control;
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
