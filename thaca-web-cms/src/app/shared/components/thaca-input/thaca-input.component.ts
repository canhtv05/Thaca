import {
  Component,
  forwardRef,
  HostBinding,
  Input,
  signal,
  computed,
  Injector,
  inject,
  Output,
  EventEmitter,
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  NgControl,
  ReactiveFormsModule,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { NgIcon } from '@ng-icons/core';
import { APP_CONFIG_ICONS } from '../../../core/configs/app-config.icon';

@Component({
  selector: 'thaca-input',
  standalone: true,
  imports: [ReactiveFormsModule, InputTextModule, IconFieldModule, NgIcon],
  templateUrl: './thaca-input.component.html',
  styleUrls: ['./thaca-input.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ThacaInputComponent),
      multi: true,
    },
  ],
})
export class ThacaInputComponent implements ControlValueAccessor {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() readonly = false;
  @Input() step: number = 1;
  @Input() min?: number;
  @Input() max?: number;
  @Input() noAccent = false;
  @Input() noSpace = false;
  @Input() trim = false;
  @Input() lowercase = false;
  @Input() uppercase = false;

  @HostBinding('class')
  get hostClass(): string {
    return `w-full thi--${this.size}`;
  }

  @Input() placeholder = '';
  @Input() startIcon?: (typeof APP_CONFIG_ICONS)[keyof typeof APP_CONFIG_ICONS];
  @Input() endIcon?: (typeof APP_CONFIG_ICONS)[keyof typeof APP_CONFIG_ICONS];
  @Input() type: string = 'text';
  @Input() label?: string;
  @Input() required?: boolean;
  @Input() id: string = `thaca-input-${Math.random().toString(36).substring(2, 15)}`;
  @Output() onEnter = new EventEmitter<void>();

  value = signal<string>('');
  disabled = false;
  APP_CONFIG_ICONS = APP_CONFIG_ICONS;
  isPasswordVisible = signal(false);
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

  inputType = computed(() =>
    this.type === 'password' ? (this.isPasswordVisible() ? 'text' : 'password') : this.type,
  );

  private onChange = (value: any) => {};
  private onTouched = () => {};

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

  onInput(event: Event) {
    const input = event.target as HTMLInputElement;
    let val = input.value;

    if (this.noAccent) val = this.removeVietnameseTones(val);
    if (this.noSpace) val = val.replace(/\s+/g, '');
    if (this.lowercase) val = val.toLowerCase();
    else if (this.uppercase) val = val.toUpperCase();

    this.value.set(val);
    this.onChange(val);
  }

  onKeyUp(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.onEnter.emit();
    }
  }

  onBlur() {
    let val = this.value();
    if (this.trim) {
      val = val.trim();
      this.value.set(val);
      this.onChange(val);
    }
    this.onTouched();
  }

  private removeVietnameseTones(str: string) {
    return str
      .normalize('NFD')
      .replace(/\p{Mn}/gu, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D');
  }

  onTogglePassword() {
    this.isPasswordVisible.update((v) => !v);
  }

  clearValue(event?: MouseEvent) {
    event?.preventDefault();
    event?.stopPropagation();
    if (this.disabled || this.readonly) return;
    this.value.set('');
    this.onChange('');
    this.onTouched();
  }

  stepUp() {
    if (this.disabled || this.readonly) return;
    const current = parseFloat(this.value()) || 0;
    const next = current + this.step;
    if (this.max !== undefined && next > this.max) return;
    const result = String(parseFloat(next.toFixed(10)));
    this.value.set(result);
    this.onChange(result);
    this.onTouched();
  }

  stepDown() {
    if (this.disabled || this.readonly) return;
    const current = parseFloat(this.value()) || 0;
    const next = current - this.step;
    if (this.min !== undefined && next < this.min) return;
    const result = String(parseFloat(next.toFixed(10)));
    this.value.set(result);
    this.onChange(result);
    this.onTouched();
  }

  showClearButton() {
    return (
      !this.disabled &&
      !this.readonly &&
      this.type !== 'password' &&
      !this.endIcon &&
      this.value().length > 0
    );
  }

  hasRightSlot() {
    return (
      !!this.endIcon || this.type === 'password' || (this.type !== 'password' && !this.endIcon)
    );
  }

  hasError() {
    const control = this.ngControl?.control;
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
