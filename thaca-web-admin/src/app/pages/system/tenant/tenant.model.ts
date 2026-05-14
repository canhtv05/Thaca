import { IPlanDTO } from '../plan/plan.model';

export interface ITenantDTO {
  id?: number;
  code: string;
  name: string;
  domain?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  plan?: {
    id: number;
    name: string;
  };
  expiresAt?: string;
  contactEmail?: string;
  logoUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  version?: number;
  planInfo?: IPlanDTO;
}

export interface IPlanInfoPrj {
  id: number;
  name: string;
  code: string;
  type: string;
}

export interface ITenantInfoPrj {
  id: number;
  code: string;
  name: string;
  logoUrl?: string;
  domain?: string;
  status?: string;
  planId?: number;
  plan?: IPlanInfoPrj;
  expiresAt?: string;
  contactEmail?: string;
  version?: number;
}
