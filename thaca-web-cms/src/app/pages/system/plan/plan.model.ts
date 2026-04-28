export interface IPlanDTO {
  id?: number;
  code: string;
  name: string;
  type: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE';
  maxUsers: number;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}
