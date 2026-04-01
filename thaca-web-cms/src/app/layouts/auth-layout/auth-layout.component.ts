import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SelectModule } from 'primeng/select';
import { ThacaSelectComponent } from '../../shared/components/thaca-select/thaca-select.component';

@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet, SelectModule, ThacaSelectComponent],
  templateUrl: './auth-layout.component.html',
})
export class AuthLayoutComponent {
  cities = [
    { name: 'New York', code: 'NY' },
    { name: 'Rome', code: 'RM' },
  ];
  selectedCity = signal<string>('NY');
}
