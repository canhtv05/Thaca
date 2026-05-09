import { ITenantInfoPrj } from '../tenant/tenant.model';

export interface ISystemUserDTO {
  id?: number;
  tenantId?: number;
  username: string;
  email: string;
  fullname: string;
  isActivated?: boolean;
  isLocked?: boolean;
  isSuperAdmin?: boolean;
  avatarUrl?: string;
  tenantIds?: number[];
  password?: string;
  lockReason?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantInfo?: ITenantInfoPrj;
  roles?: { [roleCode: string]: { [permCode: string]: 'GRANT' | 'DENY' } };
}
