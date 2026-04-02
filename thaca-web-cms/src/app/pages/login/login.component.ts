import { Component, inject } from '@angular/core';
import { AuthLayoutComponent } from '../../layouts/auth-layout/auth-layout.component';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { ThacaInputComponent } from '../../shared/components/thaca-input/thaca-input.component';
import { APP_CONFIG_ICONS } from '../../core/configs/app-config.icon';
import {
  FormBuilder,
  Validators,
  ɵInternalFormsSharedModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { ValidationMessageComponent } from '../../shared/components/validation-message/validation-message.component';

@Component({
  selector: 'app-login',
  imports: [
    AuthLayoutComponent,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    ThacaInputComponent,
    ɵInternalFormsSharedModule,
    ReactiveFormsModule,
    ValidationMessageComponent,
  ],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  readonly APP_CONFIG_ICONS = APP_CONFIG_ICONS;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  onSubmit(): void {
    console.log(this.form);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
  }
}
