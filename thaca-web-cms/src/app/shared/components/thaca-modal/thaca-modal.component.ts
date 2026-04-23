import { Component, Input, Output, EventEmitter, ContentChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { FormGroup } from '@angular/forms';
import { ThacaButtonComponent } from '../thaca-button/thaca-button.component';
import { TranslateModule } from '@ngx-translate/core';

export type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';

@Component({
  selector: 'thaca-modal',
  standalone: true,
  imports: [CommonModule, DialogModule, ThacaButtonComponent, TranslateModule],
  templateUrl: './thaca-modal.component.html',
  styleUrl: './thaca-modal.component.scss',
})
export class ThacaModalComponent {
  @Input() title: string = '';
  @Input() description: string = '';
  @Input() cancelText: string = 'common.button.cancel';
  @Input() submitText: string = 'common.button.save';
  @Input() size: ModalSize = 'md';
  @Input() styleClass: string = '';
  @Input() draggable: boolean = false;
  @Input() resizable: boolean = false;
  @Input() modal: boolean = true;
  @Input() closable: boolean = true;
  @Input() dismissableMask: boolean = true;
  @Input() appendTo: any = 'body';
  @Input() formGroup?: FormGroup;
  @Input() loading: boolean = false;
  @Input() showFooter: boolean = true;
  @Input() headerIcon: string = '';
  @Input() maximizable: boolean = false;
  @Input() blockScroll: boolean = true;
  @Input() contentStyle: any;
  @Input() disableSubmit: boolean = false;
  @Input() transitionOptions: string = '180ms ease';

  /** Internal visible state — bound one-way to p-dialog [visible] */
  _visible = false;

  /**
   * PrimeNG fires (onHide) spuriously on component init (when visible=false).
   * This flag acts as a gate: only allow handleHide() after PrimeNG confirms
   * the dialog is truly open via its own (onShow) event.
   */
  private _canHide = false;

  @Output() onSubmit = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();
  @Output() onHide = new EventEmitter<void>();
  @Output() onShow = new EventEmitter<void>();
  @Output() visibleChange = new EventEmitter<boolean>();

  @ContentChild('customFooter') customFooterTemplate?: TemplateRef<any>;

  /** Call via @ViewChild: myModal.show() */
  show() {
    if (this._visible) return;
    this._canHide = false;
    this._visible = true;
    this.visibleChange.emit(true);
  }

  /** Call via @ViewChild: myModal.hide() */
  hide() {
    if (!this._visible) return;
    this._canHide = false;
    this._visible = false;
    this.visibleChange.emit(false);
    this.onHide.emit();
  }

  /**
   * Called when PrimeNG dialog fully opens and fires its own (onShow).
   * This is the moment we open the gate for handleHide() to work.
   */
  pDialogOnShow() {
    this._canHide = true;
    this.onShow.emit(); // ✅ chuyển vào đây
  }

  /**
   * Called by PrimeNG's (onHide).
   * Only runs if _canHide is true, meaning the dialog was truly shown first.
   * This blocks the spurious (onHide) PrimeNG fires during initialization.
   */
  handleHide() {
    if (!this._canHide) return;
    this._canHide = false;
    if (!this._visible) return;
    this._visible = false;
    this.visibleChange.emit(false);
    this.onHide.emit();
  }

  handleCancel() {
    this.onCancel.emit();
    this.hide();
  }

  handleSubmit() {
    if (this.isSubmitDisabled) return;
    this.onSubmit.emit();
  }

  get isSubmitDisabled(): boolean {
    if (this.disableSubmit) return true;
    if (this.loading) return true;
    if (this.formGroup) return this.formGroup.invalid;
    return false;
  }
}
