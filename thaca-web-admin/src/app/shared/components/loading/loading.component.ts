import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { isLoading } from '../../../core/stores/app.store';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-loading',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss'],
})
export class LoadingComponent {
  readonly isLoading = isLoading;
}
