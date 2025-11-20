import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
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
      city: [''],
      state: [''],
      country: [''],
      pincode: ['', [pincodeValidator]]
    }, { validators: passwordMatchValidator });
  }
  
  get f() {
    return this.registerForm.controls;
  }

  onSubmit() {
    console.log("Registration Form Submit Button Clicked!!")
    this.submitted = true;
    this.errorMessage = '';
    console.log("registerForm.value: " + JSON.stringify(this.registerForm.value));

    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach(key => {
        const controlErrors = this.registerForm.get(key)?.errors;
        if(key === 'confirmPassword' || key === 'password' || key === 'name' || key === 'email' || key === 'contactNumber' ){
          if (controlErrors != null) {
            console.log("Registration form is invalid.");
            console.log('Control: ' + key + ', Errors: ' + JSON.stringify(controlErrors));
          }
          return;
        }
      });

    }

    console.log("Registration form has passed validation.");
    console.log("registerForm.value: " + JSON.stringify(this.registerForm.value));

    this.loading = true;
    const registerData: RegisterRequest = {
      name: this.registerForm.value.name,
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      contactNumber: this.registerForm.value.contactNumber,
      street: this.registerForm.value.street ? this.registerForm.value.street.name : '',
      city: this.registerForm.value.city ? this.registerForm.value.city.name : '',
      state: this.registerForm.value.state ? this.registerForm.value.state.name : '',
      country: this.registerForm.value.country ? this.registerForm.value.country.name : '',
      pincode: this.registerForm.value.pincode ? this.registerForm.value.pincode : "000000" 
    };
    console.log("registerData.value: " + JSON.stringify(registerData));

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

export function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password');
  const confirmPassword = control.get('confirmPassword');

  if (password && confirmPassword && password.value !== confirmPassword.value) {
    confirmPassword.setErrors({ passwordMismatch: true });
    return { passwordMismatch: true };
  }
  // Important: clear the error if they match and the error was previously set.
  if (confirmPassword?.hasError('passwordMismatch')) {
    confirmPassword.setErrors(null);
  }
  return null;
}

export function pincodeValidator(control: AbstractControl): ValidationErrors | null {
  if (control.value && !/^[0-9]{5,10}$/.test(control.value)) {
    return { pattern: true };
  }
  return null;
}
