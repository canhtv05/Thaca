import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthLayoutComponent } from '../../../layouts/auth-layout/auth-layout.component';
import { ThacaButtonComponent } from '../../../shared/components/thaca-button/thaca-button.component';
import { ThacaInputComponent } from '../../../shared/components/thaca-input/thaca-input.component';
import { APP_CONFIG_ICONS } from '../../../core/configs/app-config.icon';
import { AuthService } from '../auth.service';
import { ThacaInputOtpComponent } from '../../../shared/components/thaca-input-otp/thaca-input-otp.component';

@Component({
  selector: 'app-email-verify',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    AuthLayoutComponent,
    ThacaButtonComponent,
    ThacaInputComponent,
    ThacaInputOtpComponent,
  ],
  templateUrl: './email-verify.component.html',
})
export class EmailVerifyComponent {
  form: FormGroup;
  isLoading = signal(false);
  isCodeSent = signal(false);
  countdown = signal(300);
  timer: any;
  APP_CONFIG_ICONS = APP_CONFIG_ICONS;

  private readonly authService = inject(AuthService);

  constructor(
    private fb: FormBuilder,
    public router: Router,
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      code: ['', []],
    });
  }

  async onSendCode() {
    if (this.form.get('email')?.invalid) return;

    this.isLoading.set(true);
    const res = await this.authService.sendAuthenticateOtp(this.form.get('email')?.value);
    if (res.body.status === 'OK') {
      this.isLoading.set(false);
      this.isCodeSent.set(true);
      this.startTimer();
      this.form.get('code')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.form.get('code')?.updateValueAndValidity();
    } else {
      this.isLoading.set(false);
    }
  }

  startTimer() {
    this.countdown.set(600);
    if (this.timer) clearInterval(this.timer);
    this.timer = setInterval(() => {
      if (this.countdown() > 0) {
        this.countdown.update((v) => v - 1);
      } else {
        clearInterval(this.timer);
      }
    }, 1000);
  }

  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  onVerify() {
    if (this.form.invalid) return;

    this.isLoading.set(true);
    setTimeout(() => {
      this.isLoading.set(false);
      const email = this.form.get('email')?.value;
      this.authService.verifiedEmail.set(email);
      this.router.navigate(['/auth/platform'], {
        state: { email },
      });
    }, 1500);
  }
}
