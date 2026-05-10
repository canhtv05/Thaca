import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ThacaInputComponent } from '../thaca-input/thaca-input.component';
import { IBaseAuditResponse } from '../../../core/models/common.model';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-audit-detail',
  templateUrl: './audit-detail.component.html',
  standalone: true,
  imports: [ThacaInputComponent, TranslateModule, FormsModule],
})
export class AuditDetailComponent {
  @Input({ required: true }) audit!: Partial<IBaseAuditResponse>;
}
