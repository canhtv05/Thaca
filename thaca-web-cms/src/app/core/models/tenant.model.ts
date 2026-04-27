export interface TenantDTO {
  id?: number;
  code: string;
  name: string;
  domain?: string;
  status: 'ACTIVE' | 'INACTIVE';
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
}
