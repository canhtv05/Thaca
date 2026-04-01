import { Component, Input } from '@angular/core';
import { Select } from 'primeng/select';

@Component({
  selector: 'app-thaca-select',
  imports: [],
  templateUrl: './thaca-select.component.html',
  styleUrl: './thaca-select.component.scss',
})
export class ThacaSelectComponent extends Select {
  @Input() startIcon: string = '';
}
