import { Component, Input, OnInit } from '@angular/core';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MenuItem } from 'primeng/api';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss',
  standalone: true,
  imports: [BreadcrumbModule, RouterLink],
})
export class BreadcrumbComponent implements OnInit {
  @Input() items: MenuItem[] | undefined;
  @Input() home: MenuItem | undefined;
  @Input() title: string | undefined;

  ngOnInit() {
    this.items = [
      { label: 'Electronics' },
      { label: 'Computer' },
      { label: 'Accessories' },
      { label: 'Keyboard' },
      { label: 'Wireless' },
    ];
    this.home = { icon: 'pi pi-home' };
  }
}
