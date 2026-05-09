import {
  Component,
  Input,
  Output,
  EventEmitter,
  ContentChild,
  TemplateRef,
  HostListener,
  inject,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ThacaButtonComponent } from '../thaca-button/thaca-button.component';
import { TranslateModule } from '@ngx-translate/core';
import { EscapeStackService } from '../../../core/services/escape-stack.service';
import { isLoading } from '../../../core/stores/app.store';

export type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';

@Component({
  selector: 'thaca-modal',
  standalone: true,
  imports: [
    CommonModule,
    DialogModule,
    ThacaButtonComponent,
    TranslateModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  templateUrl: './thaca-modal.component.html',
  styleUrl: './thaca-modal.component.scss',
})
export class ThacaModalComponent {
  private readonly escapeStack = inject(EscapeStackService);
  private readonly _escapeHandler = () => this.handleCancel();

  @Input() title: string = '';
  @Input() description: string = '';
  @Input() cancelText: string = 'common.button.cancel';
  @Input() submitText: string = 'common.button.save';
  @Input() size: ModalSize = 'lg';
  @Input() styleClass: string = '';
  @Input() draggable: boolean = false;
  @Input() resizable: boolean = false;
  @Input() modal: boolean = true;
  @Input() closable: boolean = true;
  @Input() dismissableMask: boolean = true;
  @Input() appendTo: any = 'body';
  @Input() formGroup: FormGroup = new FormGroup({});
  @Input() loading: boolean = isLoading();
  @Input() showFooter: boolean = true;
  @Input() headerIcon: string = '';
  @Input() maximizable: boolean = false;
  @Input() blockScroll: boolean = true;
  @Input() contentStyle: any;
  @Input() disableSubmit: boolean = false;
  @Input() transitionOptions: string = '180ms ease';

  _visible = false;

  private readonly cdr = inject(ChangeDetectorRef);

  private _canHide = false;

  @Output() onSubmit = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();
  @Output() onHide = new EventEmitter<void>();
  @Output() onShow = new EventEmitter<void>();
  @Output() visibleChange = new EventEmitter<boolean>();

  @ContentChild('customFooter') customFooterTemplate?: TemplateRef<any>;

  show() {
    if (this._visible) return;
    this._canHide = false;
    this._visible = true;
    this.visibleChange.emit(true);
  }

  hide() {
    if (!this._visible) return;
    this.escapeStack.unregister(this._escapeHandler);
    this._canHide = false;
    this._visible = false;
    this.cdr.detectChanges();
    this.visibleChange.emit(false);
    this.onHide.emit();
  }

  pDialogOnShow() {
    this._canHide = true;
    this.escapeStack.register(this._escapeHandler);
    this.onShow.emit();
  }

  handleHide() {
    if (!this._canHide || !this._visible) return;
    this.escapeStack.unregister(this._escapeHandler);
    this._canHide = false;
    this._visible = false;
    this.visibleChange.emit(false);
    this.onHide.emit();
  }

  handleCancel() {
    this.onCancel.emit();
    this.hide();
  }

  handleSubmit() {
    if (this.isSubmitDisabled) {
      this.formGroup?.markAllAsTouched();
      return;
    }
    this.onSubmit.emit();
  }

  onEnterKey(event: KeyboardEvent) {
    const target = event.target as HTMLElement;
    if (target.tagName === 'TEXTAREA') return;
    event.preventDefault();
    this.handleSubmit();
  }

  get isSubmitDisabled(): boolean {
    if (this.disableSubmit) return true;
    if (this.loading) return true;
    if (this.formGroup && this.formGroup.invalid) return true;
    return false;
  }
}
