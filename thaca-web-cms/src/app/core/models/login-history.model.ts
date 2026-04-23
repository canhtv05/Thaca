export interface ILoginHistoryDTO {
  id: string;
  userId?: number;
  username: string;
  ipAddress: string;
  country?: string;
  city?: string;
  device?: string;
  deviceType?: string;
  os?: string;
  browser?: string;
  channel: string;
  loginTime: string;
  status: 'SUCCESS' | 'FAILED';
  failureReason?: string;
  riskScore?: number;
  isVpn?: boolean;
  isNewDevice?: boolean;
  requestId?: string;
}
