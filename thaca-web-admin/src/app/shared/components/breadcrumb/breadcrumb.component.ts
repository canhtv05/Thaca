import { Location } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MenuItem } from 'primeng/api';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss',
  standalone: true,
  imports: [BreadcrumbModule, RouterLink, TranslateModule],
})
export class BreadcrumbComponent {
  private readonly location = inject(Location);
  private readonly router = inject(Router);

  @Input() items: MenuItem[] = [];
  @Input() home: MenuItem = { icon: 'pi pi-home', routerLink: '/home' };
  @Input() title: string | undefined;
  @Input() showBack = false;
  @Input() backUrl?: string;

  goBack() {
    if (this.backUrl) {
      this.router.navigateByUrl(this.backUrl);
      return;
    }
    this.location.back();
  }
}
