import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
  checkoutForm: FormGroup;
  submitted = false;
  loading = false;
  total = 0;

  constructor(private fb: FormBuilder) {
    this.checkoutForm = this.fb.group({
      street: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      country: ['', Validators.required],
      pincode: ['', Validators.required],
      paymentMethod: ['COD', Validators.required]
    });
  }

  ngOnInit() {
    // TODO: Load cart total
  }

  get f() {
    return this.checkoutForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    if (this.checkoutForm.invalid) {
      return;
    }
    this.loading = true;
    // TODO: Implement checkout logic
  }
}
