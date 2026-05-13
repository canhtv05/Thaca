import { IBaseAuditResponse } from '../../core/models/common.model';
import { ITenantInfoPrj } from '../system/tenant/tenant.model';

export interface IUserDTO extends Partial<IBaseAuditResponse> {
  id: number;
  username: string;
  email: string;
  isActivated?: boolean;
  isLocked?: boolean;
  tenantIds?: number[];
  tenants?: ITenantInfoPrj[];
  tenantInfos?: ITenantInfoPrj[];
}
