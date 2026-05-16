import { ECommonStatus, IBaseAuditResponse } from '../../../core/models/common.model';

export interface IMailConfigDTO extends IBaseAuditResponse {
  id?: number;
  tenantId?: string;
  configCode?: string;
  description?: string;
  fromName?: string;
  fromEmail?: string;
  host?: string;
  port?: number;
  username?: string;
  password?: string;
  isAuth?: boolean;
  isStarttls?: boolean;
  status?: ECommonStatus;
  isDefault?: boolean;
}

export interface ITestConnectionReq {
  host: string;
  port: number;
  username: string;
  password: string;
  isAuth?: boolean;
  isStarttls?: boolean;
}

export interface ITestConnectionRes {
  success: boolean;
  messageVi: string;
  messageEn: string;
  titleVi: string;
  titleEn: string;
}
