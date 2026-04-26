export interface TenantDTO {
  id?: number;
  code: string;
  name: string;
  domain?: string;
  status: 'ACTIVE' | 'INACTIVE';
  plan?: number;
  planType?: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE';
  expiresAt?: string;
  contactEmail?: string;
  logoUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}
