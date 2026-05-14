import { environment } from '../../environments/environment';

export const logger = {
  log: (...args: any[]): void => {
    if (!environment.production) {
      console.log(...args);
    }
  },
  warn: (...args: any[]): void => {
    if (!environment.production) {
      console.warn(...args);
    }
  },
  error: (...args: any[]): void => {
    console.error(...args);
  },
  info: (...args: any[]): void => {
    if (!environment.production) {
      console.info(...args);
    }
  },
};
