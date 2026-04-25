import {
  Component,
  forwardRef,
  HostBinding,
  Input,
  signal,
  computed,
  Injector,
  inject,
  OnInit,
  effect,
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  NgControl,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { NgIcon } from '@ng-icons/core';
import { APP_CONFIG_ICONS } from '../../../core/configs/app-config.icon';
import { CommonModule } from '@angular/common';
import { currentLang } from '../../../core/stores/app.store';
import { PrimeNG } from 'primeng/config';

export type DatepickerMode = 'single' | 'range' | 'multiple';
export type DatepickerView = 'date' | 'month' | 'year';

const LOCALE_MAP: Record<string, any> = {
  vi: {
    startsWith: 'Bắt đầu bằng',
    contains: 'Bao gồm',
    notContains: 'Không bao gồm',
    endsWith: 'Kết thúc bằng',
    equals: 'Bằng',
    notEquals: 'Khác',
    noFilter: 'Không lọc',
    lt: 'Nhỏ hơn',
    lte: 'Nhỏ hơn hoặc bằng',
    gt: 'Lớn hơn',
    gte: 'Lớn hơn hoặc bằng',
    dateIs: 'Ngày là',
    dateIsNot: 'Ngày không là',
    dateBefore: 'Trước ngày',
    dateAfter: 'Sau ngày',
    clear: 'Xóa',
    apply: 'Áp dụng',
    matchAll: 'Khớp tất cả',
    matchAny: 'Khớp bất kỳ',
    addRule: 'Thêm quy tắc',
    removeRule: 'Bỏ quy tắc',
    accept: 'Có',
    reject: 'Không',
    choose: 'Chọn',
    upload: 'Tải lên',
    cancel: 'Hủy',
    completed: 'Hoàn thành',
    pending: 'Đang chờ',
    dayNames: ['Chủ nhật', 'Thứ hai', 'Thứ ba', 'Thứ tư', 'Thứ năm', 'Thứ sáu', 'Thứ bảy'],
    dayNamesShort: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
    dayNamesMin: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
    monthNames: [
      'Tháng 1',
      'Tháng 2',
      'Tháng 3',
      'Tháng 4',
      'Tháng 5',
      'Tháng 6',
      'Tháng 7',
      'Tháng 8',
      'Tháng 9',
      'Tháng 10',
      'Tháng 11',
      'Tháng 12',
    ],
    monthNamesShort: [
      'Th1',
      'Th2',
      'Th3',
      'Th4',
      'Th5',
      'Th6',
      'Th7',
      'Th8',
      'Th9',
      'Th10',
      'Th11',
      'Th12',
    ],
    chooseYear: 'Chọn năm',
    chooseMonth: 'Chọn tháng',
    chooseDate: 'Chọn ngày',
    prevDecade: 'Thập kỷ trước',
    nextDecade: 'Thập kỷ sau',
    prevYear: 'Năm trước',
    nextYear: 'Năm sau',
    prevMonth: 'Tháng trước',
    nextMonth: 'Tháng sau',
    prevHour: 'Giờ trước',
    nextHour: 'Giờ sau',
    prevMinute: 'Phút trước',
    nextMinute: 'Phút sau',
    prevSecond: 'Giây trước',
    nextSecond: 'Giây sau',
    am: 'am',
    pm: 'pm',
    today: 'Hôm nay',
    weekHeader: 'Tuần',
    firstDayOfWeek: 1,
    dateFormat: 'dd/mm/yy',
    weak: 'Yếu',
    medium: 'Trung bình',
    strong: 'Mạnh',
    passwordPrompt: 'Nhập mật khẩu',
    emptyFilterMessage: 'Không tìm thấy kết quả',
    emptyMessage: 'Không có tùy chọn sẵn có',
    aria: {
      trueLabel: 'Đúng',
      falseLabel: 'Sai',
      nullLabel: 'Chưa chọn',
      star: '1 sao',
      stars: '{star} sao',
      selectAll: 'Chọn tất cả',
      unselectAll: 'Bỏ chọn tất cả',
      close: 'Đóng',
      previous: 'Quay lại',
      next: 'Tiếp theo',
      navigation: 'Điều hướng',
    },
  },
  en: {
    startsWith: 'Starts with',
    contains: 'Contains',
    notContains: 'Not contains',
    endsWith: 'Ends with',
    equals: 'Equals',
    notEquals: 'Not equals',
    noFilter: 'No Filter',
    lt: 'Less than',
    lte: 'Less than or equal to',
    gt: 'Greater than',
    gte: 'Greater than or equal to',
    dateIs: 'Date is',
    dateIsNot: 'Date is not',
    dateBefore: 'Date is before',
    dateAfter: 'Date is after',
    clear: 'Clear',
    apply: 'Apply',
    today: 'Today',
    weekHeader: 'Wk',
    firstDayOfWeek: 0,
    dateFormat: 'mm/dd/yy',
    prevMonth: 'Previous Month',
    nextMonth: 'Next Month',
    prevYear: 'Previous Year',
    nextYear: 'Next Year',
    chooseYear: 'Choose Year',
    chooseMonth: 'Choose Month',
    chooseDate: 'Choose Date',
    dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
    dayNamesMin: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
    monthNames: [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ],
    monthNamesShort: [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ],
    aria: {
      close: 'Close',
      previous: 'Previous',
      next: 'Next',
    },
  },
};

@Component({
  selector: 'thaca-datepicker',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, DatePickerModule, NgIcon, CommonModule],
  templateUrl: './thaca-datepicker.component.html',
  styleUrls: ['./thaca-datepicker.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ThacaDatepickerComponent),
      multi: true,
    },
  ],
})
export class ThacaDatepickerComponent implements ControlValueAccessor {
  private primeng = inject(PrimeNG);
  // ── Layout ─────────────────────────────────────────────────────────
  @Input() size: 'sm' | 'md' | 'lg' = 'md';

  @HostBinding('class')
  get hostClass(): string {
    return `w-full thd--${this.size}`;
  }

  // ── Label / meta ───────────────────────────────────────────────────
  @Input() label?: string;
  @Input() required?: boolean;
  @Input() placeholder = 'Select date';
  @Input() id: string = `thaca-dp-${Math.random().toString(36).substring(2, 9)}`;

  // ── Icons ──────────────────────────────────────────────────────────
  @Input() startIcon?: (typeof APP_CONFIG_ICONS)[keyof typeof APP_CONFIG_ICONS];

  // ── Datepicker behaviour ───────────────────────────────────────────
  @Input() mode: DatepickerMode = 'single';
  @Input() view: DatepickerView = 'date';
  @Input() dateFormat = 'dd/mm/yy';
  @Input() showTime = false;
  @Input() hourFormat: '12' | '24' = '24';
  @Input() showButtonBar = true;
  @Input() inline = false;
  @Input() animate = false;
  @Input() minDate?: Date;
  @Input() maxDate?: Date;
  @Input() disabledDates?: Date[];
  @Input() disabledDays?: number[];
  @Input() firstDayOfWeek = 1;
  @Input() showWeek = false;
  @Input() numberOfMonths = 1;
  @Input() readonly = false;

  // ── State ──────────────────────────────────────────────────────────
  value = signal<Date | Date[] | null>(null);
  disabled = false;
  APP_CONFIG_ICONS = APP_CONFIG_ICONS;

  constructor() {
    effect(() => {
      const lang = currentLang() || 'vi';
      const locale = LOCALE_MAP[lang] || LOCALE_MAP['vi'];
      // Sử dụng đúng API của PrimeNG 18+
      this.primeng.setTranslation(locale);
    });
  }

  // ── Locale config cho PrimeNG ──────────────────────────────────────
  localeConfig = computed(() => {
    const lang = currentLang() || 'vi';
    return LOCALE_MAP[lang] || LOCALE_MAP['vi'];
  });

  // ── Derived ────────────────────────────────────────────────────────
  isRange = computed(() => this.mode === 'range');
  isMultiple = computed(() => this.mode === 'multiple');

  hasValue = computed(() => {
    const v = this.value();
    if (!v) return false;
    if (Array.isArray(v)) return v.some((d) => d != null);
    return true;
  });

  rangePlaceholder = computed(() =>
    this.mode === 'range' ? `${this.placeholder} – ${this.placeholder}` : this.placeholder,
  );

  // ── CVA ────────────────────────────────────────────────────────────
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
    this.value.set(value ?? null);
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

  onDateSelect(value: Date | Date[] | null): void {
    this.value.set(value);
    this.onChange(value);
  }

  onBlur(): void {
    this.onTouched();
  }

  clearValue(event?: MouseEvent): void {
    event?.preventDefault();
    event?.stopPropagation();
    if (this.disabled || this.readonly) return;
    this.value.set(null);
    this.onChange(null);
    setTimeout(() => {
      this.value.set(null);
    });
    this.onTouched();
  }

  hasError(): boolean {
    const control = this.ngControl?.control;
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}
