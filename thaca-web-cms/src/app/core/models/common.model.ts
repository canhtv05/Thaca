export interface IPaginationRequest {
  page: number;
  size: number;
  sortField: string;
  sortOrder: string;
}

export interface IPaginationResponse {
  currentPage: number;
  totalPages: number;
  size: number;
  count: number;
  total: number;
}

export interface IApiBody<T> {
  data: T;
  transId: string;
  status: 'OK' | 'FAILED';
  pagination?: IPaginationResponse;
}

export interface IApiHeader {
  username: string;
  location: string;
  channel: string;
  language: string;
  apiKey: string;
  timestamp: number;
}

export interface IApiPayload<T> {
  header: IApiHeader;
  body: IApiBody<T>;
}

export interface ISearchRequest<T> {
  filter: T;
  page: IPaginationRequest;
}

export interface ISearchResponse<T> {
  data: T[];
  pagination: IPaginationResponse;
}

export interface IErrorData {
  code: string;
  titleVi: string;
  titleEn: string;
  messageVi: string;
  messageEn: string;
}
