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
  roles?: { [key: string]: { [key: string]: string } };
  tenantIds?: number[];
  password?: string;
  lockReason?: string;
  createdAt?: string;
  updatedAt?: string;
  tenantInfo?: ITenantInfoPrj;
}
