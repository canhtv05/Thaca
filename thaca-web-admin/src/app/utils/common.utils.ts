import { IApiHeader, IApiBody } from '../core/models/common.model';
import { currentLang, currentUser } from '../core/stores/app.store';

const DEVICE_ID_STORAGE_KEY = 'thaca_device_id';

const generateDeviceId = (): string => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  const S4 = () => (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  return `dv-${S4()}${S4()}-${S4()}-${Date.now()}`;
};

const getDeviceId = (): string => {
  const existed = localStorage.getItem(DEVICE_ID_STORAGE_KEY);
  if (existed) return existed;

  const created = generateDeviceId();
  localStorage.setItem(DEVICE_ID_STORAGE_KEY, created);
  return created;
};

export const createHeader = (payload: Partial<IApiHeader> = {}): IApiHeader => {
  const defaultPayload: IApiHeader = {
    username: currentUser()?.username || 'GUEST',
    location: window.location.hostname,
    channel: 'admin',
    language: currentLang() || 'vi',
    deviceId: getDeviceId(),
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
