import {
  AfterContentInit,
  Component,
  ContentChildren,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
  QueryList,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Tabs, TabList, Tab, TabPanels, TabPanel } from 'primeng/tabs';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { ThacaTabComponent } from './thaca-tab.component';

export type ThacaTabsVariant = 'default' | 'pill' | 'underline';

@Component({
  selector: 'thaca-tabs',
  standalone: true,
  imports: [CommonModule, Tabs, TabList, Tab, TabPanels, TabPanel, TranslateModule],
  templateUrl: './thaca-tabs.component.html',
  styleUrl: './thaca-tabs.component.scss',
})
export class ThacaTabsComponent implements AfterContentInit, OnDestroy {
  @Input() value?: string | number;
  @Output() valueChange = new EventEmitter<string | number>();

  @Input() scrollable = false;
  @Input() lazy = false;
  @Input() selectOnFocus = false;
  @Input() showNavigators = true;
  @Input() styleClass = '';
  @Input() variant: ThacaTabsVariant = 'default';

  @ContentChildren(ThacaTabComponent) tabItems!: QueryList<ThacaTabComponent>;

  tabs: ThacaTabComponent[] = [];
  private tabChangesSub?: Subscription;

  ngAfterContentInit(): void {
    this.syncTabs();
    this.tabChangesSub = this.tabItems.changes.subscribe(() => this.syncTabs());
  }

  ngOnDestroy(): void {
    this.tabChangesSub?.unsubscribe();
  }

  onValueChange(next: string | number | undefined): void {
    if (next === undefined) return;
    this.value = next;
    this.valueChange.emit(next);
  }

  get rootClass(): string {
    return ['thaca-tabs', `thaca-tabs--${this.variant}`, this.styleClass].filter(Boolean).join(' ');
  }

  private syncTabs(): void {
    this.tabs = this.tabItems.toArray();
    if (this.value == null && this.tabs.length > 0) {
      const first = this.tabs[0].value;
      this.value = first;
      this.valueChange.emit(first);
    }
  }
}
