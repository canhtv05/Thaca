export interface IUserDTO {
  id: number;
  fullname?: string;
  username: string;
  email: string;
  isActivated?: boolean;
  isLocked?: boolean;
}
