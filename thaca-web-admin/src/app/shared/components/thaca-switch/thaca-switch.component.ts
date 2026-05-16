import {
  Component,
  Input,
  Output,
  EventEmitter,
  forwardRef,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  inject,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { CommonModule } from '@angular/common';

export type SwitchSize = 'sm' | 'md' | 'lg';
export type SwitchVariant = 'primary' | 'success' | 'danger' | 'warning';
export type SwitchState = 'idle' | 'loading' | 'indeterminate' | 'disabled';

@Component({
  selector: 'thaca-switch',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './thaca-switch.component.html',
  styleUrl: './thaca-switch.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ThacaSwitchComponent),
      multi: true,
    },
  ],
})
export class ThacaSwitchComponent implements ControlValueAccessor {
  private readonly cdr = inject(ChangeDetectorRef);

  @Input() inputId: string = `thaca-switch-${Math.random().toString(36).substring(2, 15)}`;
  @Input() name?: string;
  @Input() size: SwitchSize = 'md';
  @Input() variant: SwitchVariant = 'primary';
  @Input() state: SwitchState = 'idle';
  @Input() label?: string;
  @Input() ariaLabel?: string;

  @Output() checkedChange = new EventEmitter<boolean>();

  checked = false;

  private onChange = (_: boolean) => {};
  private onTouched = () => {};

  get hostClasses(): Record<string, boolean> {
    return {
      [`sw-${this.size}`]: true,
      [`sw-${this.variant}`]: true,
      'sw-checked': this.checked,
      'sw-disabled': this.state === 'disabled',
      'sw-loading': this.state === 'loading',
      'sw-indeterminate': this.state === 'indeterminate',
    };
  }

  onHostClick(event: MouseEvent): void {
    if (this.state === 'disabled' || this.state === 'loading' || this.state === 'indeterminate') {
      return;
    }
    if ((event.target as HTMLElement).tagName === 'INPUT') {
      return;
    }
    this.checked = !this.checked;
    this.onChange(this.checked);
    this.onTouched();
    this.checkedChange.emit(this.checked);
    this.cdr.markForCheck();
  }

  onInputChange(event: Event): void {
    const value = (event.target as HTMLInputElement).checked;
    this.checked = value;
    this.onChange(value);
    this.onTouched();
    this.checkedChange.emit(value);
    this.cdr.markForCheck();
  }

  writeValue(value: boolean): void {
    this.checked = !!value;
    this.cdr.markForCheck();
  }

  registerOnChange(fn: (_: boolean) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.state = isDisabled ? 'disabled' : 'idle';
    this.cdr.markForCheck();
  }
}
