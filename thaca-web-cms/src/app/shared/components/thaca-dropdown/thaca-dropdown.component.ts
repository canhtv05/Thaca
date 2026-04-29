import {
  Component,
  Input,
  Output,
  EventEmitter,
  signal,
  HostListener,
  forwardRef,
  ElementRef,
  inject,
  OnDestroy,
  NgZone,
  PLATFORM_ID,
  AfterViewChecked,
  Injector,
} from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, NgControl } from '@angular/forms';
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
export class ThacaDropdownComponent implements ControlValueAccessor, OnDestroy, AfterViewChecked {
  @Input() options: IDropdownOption[] = [];
  @Input() placeholder = 'Chọn...';
  @Input() disabled = false;
  @Input() readonly = false;
  @Input() clearable = true;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() label?: string;
  @Input() required = false;
  @Input() multiple = false;
  @Input() id: string = `thaca-dropdown-${Math.random().toString(36).substring(2, 15)}`;
  /**
   * 'body' → panel được move ra document.body sau khi Angular render,
   *           thoát hoàn toàn khỏi overflow/z-index/modal trap.
   * null (default) → panel render inline bên trong component (absolute).
   */
  @Input() appendTo: 'body' | null = null;

  @Output() onChange = new EventEmitter<any>();

  private el = inject(ElementRef);
  private zone = inject(NgZone);
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
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

  open = signal(false);
  value = signal<any>(null);
  panelStyle = signal<Record<string, string>>({});

  // Track xem panel đã được move ra body chưa
  private panelMovedToBody = false;
  private movedPanelEl: HTMLElement | null = null;

  private scrollHandler = () => this.zone.run(() => this.updatePortalPosition());
  private resizeHandler = () => this.zone.run(() => this.updatePortalPosition());

  private _onChange: (v: any) => void = () => {};
  private _onTouched: () => void = () => {};

  get selectedLabel(): string | null {
    if (this.multiple) {
      const vals = this.value();
      if (!Array.isArray(vals) || vals.length === 0) return null;
      const labels = this.options.filter((o) => vals.includes(o.value)).map((o) => o.label);
      return labels.join(', ');
    } else {
      const opt = this.options.find((o) => o.value === this.value());
      return opt ? opt.label : null;
    }
  }

  // ── AfterViewChecked: move panel ra body ngay sau khi Angular render nó ──

  ngAfterViewChecked() {
    if (!this.isBrowser || this.appendTo !== 'body') return;

    const host = this.el.nativeElement as HTMLElement;
    const panel = host.querySelector('.tdd-portal-panel') as HTMLElement | null;

    if (panel && !this.panelMovedToBody) {
      // Move DOM node ra thẳng body — thoát hoàn toàn khỏi mọi stacking context
      document.body.appendChild(panel);
      this.panelMovedToBody = true;
      this.movedPanelEl = panel;
      this.updatePortalPosition();
    }

    if (!panel && this.panelMovedToBody) {
      // Angular đã xóa panel (open = false) → reset tracking
      this.panelMovedToBody = false;
      this.movedPanelEl = null;
    }
  }

  // ── Portal positioning ────────────────────────────────────

  private updatePortalPosition() {
    if (!this.isBrowser) return;
    const trigger = (this.el.nativeElement as HTMLElement).querySelector(
      '.tdd-trigger',
    ) as HTMLElement | null;
    if (!trigger) return;

    const rect = trigger.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const spaceAbove = rect.top;
    const openUp = spaceBelow < 200 && spaceAbove > spaceBelow;

    const styles: Record<string, string> = {
      position: 'fixed',
      left: `${rect.left}px`,
      minWidth: `${rect.width}px`,
      zIndex: '99999',
    };

    if (openUp) {
      styles['bottom'] = `${window.innerHeight - rect.top + 4}px`;
      styles['top'] = 'auto';
    } else {
      styles['top'] = `${rect.bottom + 4}px`;
      styles['bottom'] = 'auto';
    }

    this.panelStyle.set(styles);

    // Nếu panel đã bị move ra body, apply style trực tiếp lên DOM node
    if (this.movedPanelEl) {
      Object.assign(this.movedPanelEl.style, styles);
    }
  }

  // ── Actions ──────────────────────────────────────────────

  toggle() {
    if (this.disabled || this.readonly) return;
    const next = !this.open();
    this.open.set(next);
    this._onTouched();

    if (next && this.appendTo === 'body' && this.isBrowser) {
      // Position sẽ được set sau khi ngAfterViewChecked move panel ra body
      window.addEventListener('scroll', this.scrollHandler, true);
      window.addEventListener('resize', this.resizeHandler);
    } else {
      this.removeGlobalListeners();
    }
  }

  select(opt: IDropdownOption) {
    if (opt.disabled || this.disabled || this.readonly) return;
    if (this.multiple) {
      let current = this.value();
      if (!Array.isArray(current)) current = [];
      const index = current.indexOf(opt.value);
      if (index > -1) {
        current = current.filter((v: any) => v !== opt.value);
      } else {
        current = [...current, opt.value];
      }
      this.value.set(current);
      this._onChange(current);
      this.onChange.emit(current);
    } else {
      this.value.set(opt.value);
      this.open.set(false);
      this.removeGlobalListeners();
      this._onChange(opt.value);
      this.onChange.emit(opt.value);
    }
  }

  close() {
    this.open.set(false);
    this.removeGlobalListeners();
  }

  clear(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    if (!this.clearable) return;
    const val = this.multiple ? [] : null;
    this.value.set(val);
    this._onChange(val);
    this.onChange.emit(val);
    this._onTouched();
    if (!this.multiple) this.close();
  }

  showClearButton() {
    const val = this.value();
    if (this.multiple) {
      return (
        this.clearable && !this.disabled && !this.readonly && Array.isArray(val) && val.length > 0
      );
    }
    return (
      this.clearable &&
      !this.disabled &&
      !this.readonly &&
      val !== null &&
      val !== undefined &&
      val !== ''
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

  isSelected(opt: IDropdownOption) {
    const val = this.value();
    if (this.multiple) {
      return Array.isArray(val) && val.includes(opt.value);
    }
    return val === opt.value;
  }

  writeValue(val: any) {
    if (this.multiple && !Array.isArray(val)) {
      this.value.set(val ? [val] : []);
    } else {
      this.value.set(val);
    }
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

  hasError() {
    const control = this.ngControl?.control;
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  ngOnDestroy() {
    this.removeGlobalListeners();
    // Dọn panel nếu còn trên body khi component bị destroy
    if (this.movedPanelEl && document.body.contains(this.movedPanelEl)) {
      document.body.removeChild(this.movedPanelEl);
    }
  }
}
