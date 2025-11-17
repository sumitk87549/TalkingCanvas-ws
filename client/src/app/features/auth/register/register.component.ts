import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  registerForm: FormGroup;
  submitted = false;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder, 
    private router: Router,
    private authService: AuthService
  ) {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      contactNumber: ['', Validators.required],
      street: [''],
      city: ['', Validators.required],
      state: [''],
      country: [''],
      pincode: ['', [Validators.pattern('^[0-9]{5,10}$')]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ 'passwordMismatch': true });
      return { 'passwordMismatch': true };
    }
    return null;
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit() {
    console.log("Registration Form Submit Button Clicked!!")
    console.log(this.registerForm);
    this.submitted = true;
    this.errorMessage = '';

    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    const registerData: RegisterRequest = {
      name: this.registerForm.value.name,
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      contactNumber: this.registerForm.value.contactNumber,
      street: this.registerForm.value.street,
      city: this.registerForm.value.city,
      state: this.registerForm.value.state,
      country: this.registerForm.value.country,
      pincode: this.registerForm.value.pincode
    };
    
    console.log(registerData);
    this.authService.register(registerData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          // Registration successful, navigate to home or login page
          this.router.navigate(['/login'], { 
            queryParams: { registered: 'true' } 
          });
        } else {
          this.errorMessage = response.message || 'Registration failed. Please try again.';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'An error occurred during registration. Please try again.';
      }
    });
  }
}


