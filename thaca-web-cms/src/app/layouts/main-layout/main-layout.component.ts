import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Sidebar } from '../../shared/components/sidebar/sidebar.component';
import { HeaderLayoutComponent } from '../header-layout/header-layout.component';
import { LayoutService } from '../../shared/components/sidebar/sidebar.service';

@Component({
  selector: 'app-main-layout',
  imports: [CommonModule, RouterOutlet, Sidebar, HeaderLayoutComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss',
})
export class MainLayoutComponent {
  layoutService = inject(LayoutService);
}
