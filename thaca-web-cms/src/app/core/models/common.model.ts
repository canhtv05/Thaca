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

export interface IBaseAuditResponse {
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
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
  deviceId: string;
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

export interface IImportError {
  row: number;
  column: string;
  columnKey: string;
  message: string;
  value: string;
}

export interface IImportResult {
  totalRows: number;
  successCount: number;
  errorCount: number;
  hasErrors: boolean;
  errors?: IImportError[];
  preview?: any[];
}
