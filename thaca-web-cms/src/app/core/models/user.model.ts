import { IBaseAuditResponse } from './common.model';

export interface IUserDTO extends Partial<IBaseAuditResponse> {
  id: number;
  fullname?: string;
  username: string;
  email: string;
  isActivated?: boolean;
  isLocked?: boolean;
}
