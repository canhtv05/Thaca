export interface ILoginReq {
  username: string;
  password: string;
  captcha: string;
  captchaId: string;
  tenantId?: number;
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
  isAuthenticate: boolean;
  info: IAuthUserDTO;
  accessToken: string;
}
