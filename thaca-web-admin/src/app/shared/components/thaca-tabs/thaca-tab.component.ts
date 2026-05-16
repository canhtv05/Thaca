import { Component, Input, TemplateRef, ViewChild } from '@angular/core';

@Component({
  selector: 'thaca-tab',
  standalone: true,
  template: `
    <ng-template #body>
      <ng-content />
    </ng-template>
  `,
})
export class ThacaTabComponent {
  @Input({ required: true }) value!: string | number;
  @Input() label = '';
  @Input() icon?: string;
  @Input() disabled = false;

  @ViewChild('body', { static: true }) bodyTpl!: TemplateRef<unknown>;
}
