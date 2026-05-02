import { IBaseAuditResponse } from '../../../../core/models/common.model';

export interface IUserLockHistoryDTO extends IBaseAuditResponse {
  id?: number;
  targetUserId?: number;
  action?: AccountStatus;
  reason?: string;
}

export enum AccountStatus {
  LOCK = 'LOCK',
  UNLOCK = 'UNLOCK',
}
