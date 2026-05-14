import { Component, inject } from '@angular/core';
import { CommonModule, KeyValuePipe } from '@angular/common';
import { currentUser } from '../../core/stores/app.store';
import { TranslateModule } from '@ngx-translate/core';
import { ThacaInputComponent } from '../../shared/components/thaca-input/thaca-input.component';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, TranslateModule, ThacaInputComponent, ReactiveFormsModule, KeyValuePipe],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent {
  currentUser = currentUser;
  fb = inject(FormBuilder);

  profileForm: FormGroup;

  constructor() {
    const user = this.currentUser();
    this.profileForm = this.fb.group({
      username: [{ value: user?.username, disabled: true }],
      fullname: [user?.fullname, [Validators.required]],
      email: [user?.email, [Validators.required, Validators.email]],
    });
  }

  saveProfile() {
    if (this.profileForm.valid) {
      console.log('Saving profile:', this.profileForm.getRawValue());
      // Implement update logic here
    }
  }
}
