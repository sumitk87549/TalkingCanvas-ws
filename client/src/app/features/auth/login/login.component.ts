import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  submitted = false;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit() {
    console.log("Login Form Submit Button Clicked!!")
    this.submitted = true;
    this.errorMessage = '';
    console.log("loginForm.value: " + JSON.stringify(this.loginForm.value));

    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        const controlErrors = this.loginForm.get(key)?.errors;
        if(key === 'password' || key === 'email'){
          if (controlErrors != null) {
            console.log("Login form is invalid.");
            console.log('Control: ' + key + ', Errors: ' + JSON.stringify(controlErrors));
          }
          return;
        }
      });

    }

    console.log("Login form has passed validation.");
    console.log("loginForm.value: " + JSON.stringify(this.loginForm.value));

    this.loading = true;
    const loginData: LoginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };
    console.log("loginData.value: " + JSON.stringify(loginData));

    this.authService.login(loginData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          console.log("Login successful....!!!YAYYYYYY")

          // Persist JWT token and user data in browser localStorage
          localStorage.setItem('auth_token', response.data.token);
          localStorage.setItem('current_user', JSON.stringify(response.data));

          this.router.navigate(['/']);
        } else {
          this.errorMessage = response.message || 'Login failed. Please try again.';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'An error occurred during login. Please try again.';
      }
    });
  }
}
