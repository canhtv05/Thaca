export interface IPermissionDTO {
  code: string;
  description: string;
  roleDescription?: string;
  roleCode?: string;
  effect?: 'GRANT' | 'DENY';
}
