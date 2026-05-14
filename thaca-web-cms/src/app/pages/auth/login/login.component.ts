import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthLayoutComponent } from '../../../layouts/auth-layout/auth-layout.component';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import { ɵInternalFormsSharedModule } from '@angular/forms';
import { ValidationMessageComponent } from '../../../shared/components/validation-message/validation-message.component';
import { TranslateModule } from '@ngx-translate/core';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { AuthService } from '../auth.service';
import { ILoginReq } from '../../../core/models/auth.model';
import { ActivatedRoute, Router } from '@angular/router';
import { APP_CONFIG_ICONS } from '../../../core/configs/app-config.icon';
import { ITenantDTO } from '../../system/tenant/tenant.model';
import { CommonModule } from '@angular/common';

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
    CommonModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly APP_CONFIG_ICONS = APP_CONFIG_ICONS;

  form = this.fb.group({
    username: [
      '',
      [
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(50),
        Validators.pattern(/^[a-z0-9._-]+$/),
      ],
    ],
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(100),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&._-]).+$/),
      ],
    ],
    captcha: ['', [Validators.required]],
  });

  captchaImage = signal<string>('');
  captchaId = signal<string>('');
  tenantId = signal<number | null>(null);
  tenantState = signal<ITenantDTO | null>(null);

  readonly captchaInputId = 'captcha-input-' + Math.random().toString(36).substring(2, 9);

  ngOnInit(): void {
    this.generateCaptcha();
    const state = history.state;
    if (state && state.tenant) {
      this.tenantState.set(state.tenant);
      this.tenantId.set(state.tenant.id);
    }
  }

  async generateCaptcha(): Promise<void> {
    const captcha = await this.authService.generateCaptcha();
    if (captcha.body.status === 'OK') {
      this.captchaImage.set(captcha.body.data.image);
      this.captchaId.set(captcha.body.data.captchaId);
    }
  }

  async onReloadCaptcha(): Promise<void> {
    this.generateCaptcha();
    this.form.get('captcha')?.setValue('');
    this.form.get('captcha')?.markAsTouched();
    this.form.get('captcha')?.updateValueAndValidity();
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const loginReq: ILoginReq = {
      ...(this.form.getRawValue() as unknown as ILoginReq),
      captchaId: this.captchaId(),
      tenantId: this.tenantId() ?? undefined,
    };
    const res = await this.authService.login(loginReq);
    const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    if (res.body.status === 'OK') {
      this.router.navigateByUrl(returnUrl);
    } else {
      this.onReloadCaptcha();
    }
  }

  onBackToType(): void {
    if (this.tenantId()) {
      this.router.navigate(['/auth/platform']);
    } else {
      this.router.navigate(['/auth/verify']);
    }
  }
}
