import {
  Component,
  Input,
  Output,
  EventEmitter,
  ContentChild,
  TemplateRef,
  inject,
  signal,
  effect,
  untracked,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { SkeletonModule } from 'primeng/skeleton';
import {
  IPaginationRequest,
  IPaginationResponse,
  ISearchRequest,
  IApiPayload,
} from '../../../core/models/common.model';
import { GlobalHttp } from '../../../core/global/global-http';
import { createBody, createHeader } from '../../../utils/common.utils';

export interface ITableColumn {
  field: string;
  header: string;
  sortable?: boolean;
  width?: string;
  condition?: (row: any) => boolean;
  render?: (row: any) => string;
}

export interface ITableAction {
  icon: string;
  titleKey: string;
  color?: 'primary' | 'secondary' | 'success' | 'info' | 'warn' | 'help' | 'danger';
  condition?: (row: any) => boolean;
}

export interface ITableConfig<T = any> {
  url: string;
  columns: ITableColumn[];
  actions?: ITableAction[];
  defaultFilter?: T;
  fixedFilter?: any;
  rows?: number;
  dataKey?: string;
  actionFixed?: boolean;
  showStt?: boolean;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    PaginatorModule,
    InputTextModule,
    ButtonModule,
    TooltipModule,
    SkeletonModule,
  ],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss',
})
export class DataTableComponent {
  @Input({ required: true }) config!: ITableConfig;
  @Input() externalFilter: any = {};

  @Output() onRowClick = new EventEmitter<any>();
  @Output() onDataLoaded = new EventEmitter<any[]>();
  @Output() onAction = new EventEmitter<{ actionKey: string; row: any }>();

  @ContentChild('searchTemplate') searchTemplate?: TemplateRef<any>;
  @ContentChild('headerActions') headerActions?: TemplateRef<any>;

  data = signal<any[]>([]);
  totalRecords = signal(0);
  loading = signal(false);

  pagination = signal<IPaginationRequest>({
    page: 0,
    size: 10,
    sortField: '',
    sortOrder: '',
  });

  constructor() {
    effect(() => {
      if (this.externalFilter) {
        untracked(() => this.refresh());
      }
    });
  }

  async load(pageReq: IPaginationRequest) {
    this.pagination.set(pageReq);
    this.loading.set(true);

    const searchRequest: ISearchRequest<any> = {
      filter: { ...this.config.defaultFilter, ...this.config.fixedFilter, ...this.externalFilter },
      page: pageReq,
    };

    const payload: IApiPayload<ISearchRequest<any>> = {
      header: createHeader(),
      body: createBody(searchRequest),
    };

    try {
      const res = await GlobalHttp.post<IApiPayload<any>>(this.config.url, payload, {
        skipLoading: true,
      });
      if (res.body.status === 'OK') {
        this.data.set(res.body.data.data);
        this.totalRecords.set(res.body.data.pagination.total);
        this.onDataLoaded.emit(res.body.data.data);
      }
    } finally {
      this.loading.set(false);
    }
  }

  refresh() {
    this.load({ ...this.pagination(), page: 0 });
  }

  onLazyLoad(event: TableLazyLoadEvent) {
    const page: IPaginationRequest = {
      page: (event.first || 0) / (event.rows || this.config.rows || 10),
      size: event.rows || this.config.rows || 10,
      sortField: (event.sortField as string) || '',
      sortOrder: event.sortOrder === 1 ? 'ASC' : event.sortOrder === -1 ? 'DESC' : '',
    };
    this.load(page);
  }

  handleAction(actionKey: string, row: any, event: Event) {
    event.stopPropagation();
    this.onAction.emit({ actionKey, row });
  }

  getIndex(rowIndex: number): number {
    const { page, size } = this.pagination();
    return page * size + rowIndex + 1;
  }
}
