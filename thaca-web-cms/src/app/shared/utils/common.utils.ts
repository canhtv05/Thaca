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
   * Parse chuỗi 'dd-MM-yyyy HH:mm:ss' hoặc 'dd-MM-yyyy' từ backend về đối tượng Date của JS
   */
  static parseBackendDate(dateStr: string | null | undefined): Date | null {
    if (!dateStr) return null;
    try {
      const parts = dateStr.trim().split(' ');
      const dateParts = parts[0].split('-');

      const day = parseInt(dateParts[0], 10);
      const month = parseInt(dateParts[1], 10) - 1;
      const year = parseInt(dateParts[2], 10);

      if (parts.length > 1) {
        const timeParts = parts[1].split(':');
        return new Date(
          year,
          month,
          day,
          parseInt(timeParts[0] || '0', 10),
          parseInt(timeParts[1] || '0', 10),
          parseInt(timeParts[2] || '0', 10),
        );
      }

      return new Date(year, month, day);
    } catch (e) {
      logger.error('Error parsing backend date:', dateStr, e);
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
