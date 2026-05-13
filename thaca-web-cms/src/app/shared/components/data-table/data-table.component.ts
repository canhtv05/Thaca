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
  NgZone,
  AfterViewInit,
  HostListener,
  ElementRef,
  OnInit,
  OnDestroy,
  OnChanges,
  SimpleChanges,
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
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import DOMPurify from 'dompurify';
import { AuthService } from '../../../core/services/auth.service';

export interface ITableColumn {
  field: string;
  header: string;
  sortable?: boolean;
  width?: string;
  fixed?: boolean;
  condition?: (data?: any) => boolean;
  render?: (row: any) => string;
  center?: boolean;
}

export interface ITableAction {
  icon?: string;
  key: string;
  titleKey: string;
  color?: 'primary' | 'secondary' | 'success' | 'info' | 'warn' | 'help' | 'danger';
  condition?: (row: any) => boolean;
  permissions?: string[];
}

export interface ITableActionEvent<T = any> {
  key: string;
  row: T;
  action: ITableAction;
}

export interface ITableConfig<T = any> {
  url?: string;
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
export class DataTableComponent implements AfterViewInit, OnInit, OnDestroy, OnChanges {
  private translate = inject(TranslateService);
  private zone = inject(NgZone);
  private sanitizer = inject(DomSanitizer);
  private el = inject(ElementRef);
  private readonly auth = inject(AuthService);
  @Input({ required: true }) config!: ITableConfig;
  @Input() staticRows?: any[];
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
    let cols = [...this.config.columns].filter((c) => !c.condition || c.condition());
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
  activeMoreRow = signal<any | null>(null);
  moreMenuStyles = signal<Record<string, string>>({});

  private scrollListener = () => {
    if (this.activeMoreRow()) {
      this.zone.run(() => this.closeMoreMenu());
    }
  };

  ngOnInit() {
    if (typeof window !== 'undefined') {
      window.addEventListener('scroll', this.scrollListener, true);
      window.addEventListener('resize', this.scrollListener, true);
    }
  }

  ngOnDestroy() {
    if (typeof window !== 'undefined') {
      window.removeEventListener('scroll', this.scrollListener, true);
      window.removeEventListener('resize', this.scrollListener, true);
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.el.nativeElement.contains(event.target)) {
      this.closeMoreMenu();
    }
  }

  closeMoreMenu() {
    this.activeMoreRow.set(null);
  }

  toggleMore(row: any, event: Event) {
    event.stopPropagation();
    if (this.activeMoreRow() === row) {
      this.closeMoreMenu();
    } else {
      this.activeMoreRow.set(row);
      const target = event.currentTarget as HTMLElement;
      const rect = target.getBoundingClientRect();

      this.moreMenuStyles.set({
        position: 'fixed',
        bottom: `${window.innerHeight - rect.top + 8}px`,
        left: `${rect.left + rect.width / 2}px`,
      });
    }
  }

  private readonly DEFAULT_SORT_FIELD = 'updatedAt';

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

  ngAfterViewInit() {
    if (this.isStaticMode()) {
      this.applyStaticLoad(this.pagination());
    } else if (this.config.url) {
      this.load(this.pagination());
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['staticRows'] && this.isStaticMode() && !changes['staticRows'].firstChange) {
      this.applyStaticLoad({ ...this.pagination(), page: 0 });
    }
  }

  private isStaticMode(): boolean {
    return this.staticRows !== undefined;
  }

  private applyStaticLoad(pageReq: IPaginationRequest) {
    this.zone.run(() => {
      this.pagination.set(pageReq);
      const all = [...(this.staticRows ?? [])];
      const start = pageReq.page * pageReq.size;
      this.data.set(all.slice(start, start + pageReq.size));
      this.totalRecords.set(all.length);
      this.loading.set(false);
      if (this.pTable) {
        this.pTable.sortField = pageReq.sortField;
        this.pTable.sortOrder = pageReq.sortOrder === 'DESC' ? -1 : 1;
      }
    });
  }

  hasPermission(permissions: string[]): boolean {
    if (this.auth.isSuperAdmin() || !permissions.length) {
      return true;
    }
    const userRoles = Object.values(this.auth.user()?.roles ?? {});
    return permissions.some((permission) => {
      const isDenied = userRoles.some((role) => role[permission] === 'DENY');
      if (isDenied) return false;
      return userRoles.some((role) => role[permission] === 'GRANT');
    });
  }

  sanitizeHtml(html: string): SafeHtml {
    const clean = DOMPurify.sanitize(html, {
      ALLOWED_TAGS: ['div', 'span', 'img', 'p', 'b', 'i', 'strong', 'em', 'br', 'ul', 'li', 'a'],
      ALLOWED_ATTR: ['class', 'style', 'src', 'alt', 'href', 'target', 'rel'],
    });
    return this.sanitizer.bypassSecurityTrustHtml(clean);
  }

  async load(pageReq: IPaginationRequest) {
    this.zone.run(async () => {
      this.pagination.set(pageReq);
      this.loading.set(true);
      if (this.pTable) {
        this.pTable.sortField = pageReq.sortField;
        this.pTable.sortOrder = pageReq.sortOrder === 'DESC' ? -1 : 1;
      }

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
        if (!this.config.url) {
          return;
        }
        const res = await GlobalHttp.post<IApiPayload<any>>(this.config.url, payload, {
          skipLoading: true,
        });
        if (res?.body?.status === 'OK') {
          this.data.set(res.body.data.data);
          this.totalRecords.set(res.body.data.pagination.total);
          this.onDataLoaded.emit(res.body.data.data);
        }
      } finally {
        this.loading.set(false);
        if (this.pTable) {
          this.pTable.sortField = pageReq.sortField;
          this.pTable.sortOrder = pageReq.sortOrder === 'DESC' ? -1 : 1;
        }
      }
    });
  }

  refresh(filter?: any) {
    if (filter !== undefined) {
      this.externalFilter = filter;
    }
    const cur = this.pagination();
    const sortField = (this.pTable?.sortField as string) || cur.sortField;
    const sortOrder = this.pTable ? (this.pTable.sortOrder === 1 ? 'ASC' : 'DESC') : cur.sortOrder;
    const next = { ...cur, page: 0, sortField, sortOrder };
    if (this.isStaticMode()) {
      this.applyStaticLoad(next);
    } else {
      this.load(next);
    }
  }

  goToPage(page: number) {
    const cur = this.pagination();
    if (page < 0 || page >= this.totalPages() || page === cur.page) return;
    if (this.isStaticMode()) {
      this.applyStaticLoad({ ...cur, page });
      return;
    }
    this.load({ ...cur, page });
  }

  onPageSizeChange(size: number) {
    const cur = this.pagination();
    if (size === cur.size) return;
    if (this.isStaticMode()) {
      this.applyStaticLoad({ ...cur, page: 0, size });
      return;
    }
    this.load({ ...cur, page: 0, size });
  }

  onLazyLoad(event: TableLazyLoadEvent) {
    if (this.isStaticMode()) {
      return;
    }
    const cur = this.pagination();

    const sortField = (event.sortField as string) || this.DEFAULT_SORT_FIELD;
    const sortOrder = event.sortOrder === -1 ? 'DESC' : 'ASC';

    this.load({
      ...cur,
      page: 0,
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

  getSearchRequest(): ISearchRequest<any> {
    return {
      filter: {
        ...this.config.defaultFilter,
        ...this.config.fixedFilter,
        ...this.externalFilter,
      },
      page: this.pagination(),
    };
  }

  min(a: number, b: number) {
    return Math.min(a, b);
  }

  getVisibleActions(row: any): ITableAction[] {
    const all = (this.config.actions || []).filter(
      (a) => (!a.condition || a.condition(row)) && this.hasPermission(a.permissions ?? []),
    );
    return all.length > 2 ? [] : all;
  }

  getHiddenActions(row: any): ITableAction[] {
    const all = (this.config.actions || []).filter(
      (a) => (!a.condition || a.condition(row)) && this.hasPermission(a.permissions ?? []),
    );
    return all.length > 2 ? all : [];
  }
}
