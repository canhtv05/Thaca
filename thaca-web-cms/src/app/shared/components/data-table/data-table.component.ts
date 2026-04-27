import {
  Component,
  Input,
  Output,
  EventEmitter,
  ContentChild,
  TemplateRef,
  signal,
  computed,
  inject,
  ChangeDetectorRef,
  NgZone,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { IPaginationRequest, ISearchRequest, IApiPayload } from '../../../core/models/common.model';
import { GlobalHttp } from '../../../core/global/global-http';
import { createBody, createHeader } from '../../../utils/common.utils';
import { TranslateModule, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ViewChild } from '@angular/core';
import {
  ThacaDropdownComponent,
  IDropdownOption,
} from '../thaca-dropdown/thaca-dropdown.component';

export interface ITableColumn {
  field: string;
  header: string;
  sortable?: boolean;
  width?: string;
  fixed?: boolean;
  condition?: (row: any) => boolean;
  render?: (row: any) => string;
}

export interface ITableAction {
  icon?: string;
  key: string;
  titleKey: string;
  color?: 'primary' | 'secondary' | 'success' | 'info' | 'warn' | 'help' | 'danger';
  condition?: (row: any) => boolean;
}

export interface ITableActionEvent<T = any> {
  key: string;
  row: T;
  action: ITableAction;
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
  withAudit?: boolean;
  autoLoad?: boolean;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    TooltipModule,
    TranslateModule,
    TranslatePipe,
    ThacaDropdownComponent,
  ],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss',
})
export class DataTableComponent {
  private translate = inject(TranslateService);
  private cdr = inject(ChangeDetectorRef);
  private zone = inject(NgZone);
  @Input({ required: true }) config!: ITableConfig;
  @Input() externalFilter: any = {};

  @Output() onRowClick = new EventEmitter<any>();
  @Output() onDataLoaded = new EventEmitter<any[]>();
  @Output() onAction = new EventEmitter<ITableActionEvent>();

  private auditColumns: ITableColumn[] = [
    { field: 'createdAt', header: 'common.createdAt', width: '180px', sortable: true },
    { field: 'createdBy', header: 'common.createdBy', width: '150px', sortable: true },
    { field: 'updatedAt', header: 'common.updatedAt', width: '180px', sortable: true },
    { field: 'updatedBy', header: 'common.updatedBy', width: '150px', sortable: true },
  ];

  displayColumns = computed(() => {
    let cols = [...this.config.columns];
    if (this.config.withAudit) {
      cols = [...cols, ...this.auditColumns];
    }
    return cols;
  });

  @ViewChild('table') pTable!: Table;
  @ContentChild('searchTemplate') searchTemplate?: TemplateRef<any>;
  @ContentChild('headerActions') headerActions?: TemplateRef<any>;

  data = signal<any[]>([]);
  totalRecords = signal(0);
  loading = signal(false);

  pagination = signal<IPaginationRequest>({
    page: 0,
    size: 10,
    sortField: 'updatedAt',
    sortOrder: 'DESC',
  });

  pageSizeOptions: IDropdownOption[] = [
    { label: '10 / ' + this.translate.instant('common.page'), value: 10 },
    { label: '20 / ' + this.translate.instant('common.page'), value: 20 },
    { label: '50 / ' + this.translate.instant('common.page'), value: 50 },
    { label: '100 / ' + this.translate.instant('common.page'), value: 100 },
  ];

  totalPages = computed(() => Math.max(1, Math.ceil(this.totalRecords() / this.pagination().size)));

  visiblePages = computed(() => {
    const total = this.totalPages();
    const cur = this.pagination().page;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const pages: number[] = [];
    if (cur <= 3) {
      pages.push(0, 1, 2, 3, 4, -1, total - 1);
    } else if (cur >= total - 4) {
      pages.push(0, -1, total - 5, total - 4, total - 3, total - 2, total - 1);
    } else {
      pages.push(0, -1, cur - 1, cur, cur + 1, -1, total - 1);
    }
    return pages;
  });

  first = computed(() => this.pagination().page * this.pagination().size);

  async load(pageReq: IPaginationRequest) {
    this.zone.run(async () => {
      this.pagination.set(pageReq);
      this.loading.set(true);
      this.cdr.detectChanges();

      const searchRequest: ISearchRequest<any> = {
        filter: {
          ...this.config.defaultFilter,
          ...this.config.fixedFilter,
          ...this.externalFilter,
        },
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
        this.cdr.detectChanges();
      }
    });
  }

  refresh(filter?: any) {
    if (filter !== undefined) {
      this.externalFilter = filter;
    }
    if (this.pTable) {
      if (this.pTable.first === 0) {
        this.onLazyLoad({
          first: 0,
          rows: this.pTable.rows,
          sortField: this.pTable.sortField,
          sortOrder: this.pTable.sortOrder,
        });
      } else {
        this.pTable.first = 0;
      }
    } else {
      this.load({ ...this.pagination(), page: 0 });
    }
  }

  goToPage(page: number) {
    const cur = this.pagination();
    if (page < 0 || page >= this.totalPages() || page === cur.page) return;
    if (this.pTable) {
      this.pTable.first = page * cur.size; // This will trigger onLazyLoad
    } else {
      this.load({ ...cur, page });
    }
  }

  onPageSizeChange(size: number) {
    const cur = this.pagination();
    if (size === cur.size) return;
    if (this.pTable) {
      this.pTable.rows = size;
      this.pTable.reset(); // Trigger onLazyLoad
    } else {
      this.load({ ...cur, page: 0, size });
    }
  }

  onLazyLoad(event: TableLazyLoadEvent) {
    const sortField = (event.sortField as string) || '';
    const sortOrder = event.sortOrder === 1 ? 'ASC' : event.sortOrder === -1 ? 'DESC' : '';
    const cur = this.pagination();
    const page = (event.first || 0) / (event.rows || cur.size);

    this.load({
      ...cur,
      page,
      size: event.rows || cur.size,
      sortField,
      sortOrder,
    });
  }

  handleAction(action: ITableAction, row: any, event: Event) {
    event.stopPropagation();
    const finalKey = action.key || action.titleKey;
    this.onAction.emit({ key: finalKey, row, action });
  }

  getIndex(rowIndex: number): number {
    const { page, size } = this.pagination();
    return page * size + rowIndex + 1;
  }

  min(a: number, b: number) {
    return Math.min(a, b);
  }
}
