export interface ILoginReq {
  username: string;
  password: string;
}

export interface IAuthUserDTO {
  id: number;
  tenantId?: number;
  username: string;
  email: string;
  fullname?: string;
  isActivated?: boolean;
  isLocked?: boolean;
  isSuperAdmin?: boolean;
  roles?: { [key: string]: { [key: string]: string } };
  avatarUrl?: string;
}

export interface IAuthenticateRes {
  authenticate: boolean;
  info: IAuthUserDTO;
}
