import { IApiHeader, IApiBody } from '../core/models/common.model';
import { currentLang, currentUser } from '../core/stores/app.store';

export const createHeader = (payload: Partial<IApiHeader> = {}): IApiHeader => {
  const defaultPayload: IApiHeader = {
    username: currentUser()?.username || 'GUEST',
    location: window.location.hostname,
    channel: 'WEB',
    language: currentLang() || 'vi',
    apiKey: '',
    timestamp: Date.now(),
  };

  return { ...defaultPayload, ...payload };
};

export const createBody = <T = any>(data: T): IApiBody<T> => {
  return {
    data,
    transId: generateTransId(),
    status: 'OK',
  };
};

export const generateTransId = (): string => {
  const S4 = () => (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  return S4() + S4() + '-' + S4() + '-' + S4() + '-' + Date.now();
};
