export type PopupType = 'info' | 'success' | 'warning' | 'error';

export interface PopupOptions {
  title?: string;
  message?: string;
  type?: PopupType;
  icon?: string;

  showClose?: boolean;
  showCancel?: boolean;
  showAccept?: boolean;

  acceptText?: string;
  cancelText?: string;
}
