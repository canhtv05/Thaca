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
import { AuthService } from '../../core/services/auth.service';
import { ILoginReq } from '../../core/models/auth.model';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { ThacaButtonComponent } from '../../shared/components/thaca-button/thaca-button.component';

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
    TranslateModule,
    ThacaButtonComponent,
  ],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private router = inject(Router);
  readonly APP_CONFIG_ICONS = APP_CONFIG_ICONS;

  form = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const res = await this.authService.login(this.form.value as ILoginReq);
    if (res.body.status === 'OK') {
      this.router.navigate(['/home']);
    }
  }
}
