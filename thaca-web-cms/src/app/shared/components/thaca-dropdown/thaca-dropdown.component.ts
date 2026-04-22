import {
  Component,
  Input,
  Output,
  EventEmitter,
  signal,
  computed,
  HostListener,
  forwardRef,
  ElementRef,
  inject,
  OnDestroy,
  NgZone,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

export interface IDropdownOption {
  label: string;
  value: any;
  icon?: string;
  disabled?: boolean;
}

@Component({
  selector: 'thaca-dropdown',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ThacaDropdownComponent),
      multi: true,
    },
  ],
  templateUrl: './thaca-dropdown.component.html',
  styleUrl: './thaca-dropdown.component.scss',
})
export class ThacaDropdownComponent implements ControlValueAccessor, OnDestroy {
  @Input() options: IDropdownOption[] = [];
  @Input() placeholder = 'Chọn...';
  @Input() disabled = false;
  @Input() readonly = false;
  @Input() clearable = true;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() label?: string;
  @Input() id: string = `thaca-dropdown-${Math.random().toString(36).substring(2, 15)}`;
  /**
   * 'body' → panel renders via fixed positioning, escapes overflow/z-index traps.
   * null (default) → panel renders inline inside the component (absolute).
   */
  @Input() appendTo: 'body' | null = null;

  @Output() onChange = new EventEmitter<any>();

  private el = inject(ElementRef);
  private zone = inject(NgZone);
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  open = signal(false);
  value = signal<any>(null);

  panelStyle = signal<Record<string, string>>({});

  private scrollHandler = () => this.zone.run(() => this.updatePortalPosition());
  private resizeHandler = () => this.zone.run(() => this.updatePortalPosition());

  private _onChange: (v: any) => void = () => {};
  private _onTouched: () => void = () => {};

  selectedLabel = computed(() => {
    const opt = this.options.find((o) => o.value === this.value());
    return opt ? opt.label : null;
  });

  private updatePortalPosition() {
    if (!this.isBrowser) return;
    const trigger = (this.el.nativeElement as HTMLElement).querySelector('.tdd-trigger');
    if (!trigger) return;
    const rect = trigger.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const spaceAbove = rect.top;
    const openUp = spaceBelow < 200 && spaceAbove > spaceBelow;

    if (openUp) {
      this.panelStyle.set({
        position: 'fixed',
        bottom: `${window.innerHeight - rect.top + 4}px`,
        top: 'auto',
        left: `${rect.left}px`,
        minWidth: `${rect.width}px`,
      });
    } else {
      this.panelStyle.set({
        position: 'fixed',
        top: `${rect.bottom + 4}px`,
        bottom: 'auto',
        left: `${rect.left}px`,
        minWidth: `${rect.width}px`,
      });
    }
  }

  toggle() {
    if (this.disabled || this.readonly) return;
    const next = !this.open();
    this.open.set(next);
    this._onTouched();

    if (next && this.appendTo === 'body' && this.isBrowser) {
      this.updatePortalPosition();
      window.addEventListener('scroll', this.scrollHandler, true);
      window.addEventListener('resize', this.resizeHandler);
    } else {
      this.removeGlobalListeners();
    }
  }

  select(opt: IDropdownOption) {
    if (opt.disabled || this.disabled || this.readonly) return;
    this.value.set(opt.value);
    this.open.set(false);
    this.removeGlobalListeners();
    this._onChange(opt.value);
    this.onChange.emit(opt.value);
  }

  close() {
    this.open.set(false);
    this.removeGlobalListeners();
  }

  clear(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    if (!this.clearable) return;
    this.value.set(null);
    this._onChange(null);
    this.onChange.emit(null);
    this._onTouched();
    this.close();
  }

  showClearButton() {
    return (
      this.clearable &&
      !this.disabled &&
      !this.readonly &&
      this.value() !== null &&
      this.value() !== undefined &&
      this.value() !== ''
    );
  }

  private removeGlobalListeners() {
    if (!this.isBrowser) return;
    window.removeEventListener('scroll', this.scrollHandler, true);
    window.removeEventListener('resize', this.resizeHandler);
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent) {
    if (!(this.el.nativeElement as HTMLElement).contains(e.target as Node)) {
      const clickedPanel = (e.target as HTMLElement).closest('.tdd-portal-panel');
      if (!clickedPanel) this.close();
    }
  }

  writeValue(val: any) {
    this.value.set(val);
  }
  registerOnChange(fn: any) {
    this._onChange = fn;
  }
  registerOnTouched(fn: any) {
    this._onTouched = fn;
  }
  setDisabledState(d: boolean) {
    this.disabled = d;
  }

  ngOnDestroy() {
    this.removeGlobalListeners();
  }
}
