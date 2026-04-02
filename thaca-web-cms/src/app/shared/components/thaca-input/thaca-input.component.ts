import {
  Component,
  forwardRef,
  HostBinding,
  Input,
  signal,
  computed,
  OnInit,
  Injector,
  inject,
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
  @HostBinding('class') class = 'w-full';

  @Input() placeholder = '';
  @Input() startIcon?: (typeof APP_CONFIG_ICONS)[keyof typeof APP_CONFIG_ICONS];
  @Input() endIcon?: (typeof APP_CONFIG_ICONS)[keyof typeof APP_CONFIG_ICONS];
  @Input() type: string = 'text';
  @Input() label?: string;
  @Input() required?: boolean;
  @Input() id: string = `thaca-input-${Math.random().toString(36).substring(2, 15)}`;

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
    this.value.set(input.value);
    this.onChange(input.value);
  }

  onBlur() {
    this.onTouched();
  }

  onTogglePassword() {
    this.isPasswordVisible.update((v) => !v);
  }

  hasError() {
    const control = this.ngControl?.control;
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
