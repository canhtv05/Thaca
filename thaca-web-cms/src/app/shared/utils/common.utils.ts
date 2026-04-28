import { logger } from '../../utils/logger';

export class CommonUtils {
  /**
   * Format date sang chuỗi 'dd-MM-yyyy HH:mm:ss' để gửi xuống backend (LocalDateTime)
   */
  static formatDateTime(date: Date | string | null | undefined): string {
    if (!date) return '';
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) return '';

    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');

    return `${day}-${month}-${year} ${hours}:${minutes}:${seconds}`;
  }

  /**
   * Format date sang chuỗi 'dd-MM-yyyy' để gửi xuống backend (LocalDate)
   */
  static formatDate(date: Date | string | null | undefined): string {
    if (!date) return '';
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) return '';

    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();

    return `${day}-${month}-${year}`;
  }

  /**
   * Parse chuỗi 'dd-MM-yyyy' từ backend (LocalDate) về đối tượng Date của JS
   */
  static parseBackendDate(dateStr: string | Date | null | undefined): Date | null {
    if (!dateStr) return null;
    if (dateStr instanceof Date) return dateStr;

    try {
      const [day, month, year] = dateStr.split('-');
      const isoString = `${year}-${month}-${day}T00:00:00`;
      return new Date(isoString);
    } catch {
      return null;
    }
  }

  /**
   * Parse chuỗi 'dd-MM-yyyy HH:mm:ss' từ backend (LocalDateTime) về đối tượng Date của JS
   */
  static parseBackendDateTime(dateStr: string | Date | null | undefined): Date | null {
    if (!dateStr) return null;
    if (dateStr instanceof Date) return dateStr;

    try {
      const [datePart, timePart] = dateStr.split(' ');
      const [day, month, year] = datePart.split('-');
      const isoString = `${year}-${month}-${day}T${timePart || '00:00:00'}`;
      return new Date(isoString);
    } catch {
      return null;
    }
  }

  /**
   * Clone object sạch (loại bỏ null/undefined nếu cần hoặc xử lý date)
   */
  static cleanObject(obj: any): any {
    return JSON.parse(JSON.stringify(obj));
  }

  /**
   * Loại bỏ dấu tiếng Việt
   */
  static removeVietnameseTones(str: string): string {
    if (!str) return '';
    return str
      .normalize('NFD')
      .replace(/\p{Mn}/gu, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D');
  }
}
