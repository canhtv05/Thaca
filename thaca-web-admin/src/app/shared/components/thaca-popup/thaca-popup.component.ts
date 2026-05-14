import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PopupService } from '../../../core/services/popup.service';
import { TranslateModule } from '@ngx-translate/core';
import { ThacaButtonComponent } from '../thaca-button/thaca-button.component';

@Component({
  selector: 'thaca-popup',
  standalone: true,
  imports: [CommonModule, TranslateModule, ThacaButtonComponent],
  templateUrl: './thaca-popup.component.html',
  styleUrls: ['./thaca-popup.component.scss'],
})
export class ThacaPopupComponent {
  readonly popup = inject(PopupService);
}
