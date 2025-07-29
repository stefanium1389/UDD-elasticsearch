import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BackendService } from '../../services/backend.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginForm: FormGroup;

    constructor(private fb: FormBuilder, private backend: BackendService) {
      this.loginForm = this.fb.group({
        username: ['', Validators.required],
        password: ['', Validators.required]
      });
    }

    onSubmit() {
      if (this.loginForm.valid) {
        const { username, password } = this.loginForm.value;
        console.log('Logging in with', username, password);
        this.backend.login(username, password).subscribe({
          next: (res) => {
            localStorage.setItem('auth_token', res.accessToken);
          },
          error: err => console.log(err)
        })
      }
    }
}
