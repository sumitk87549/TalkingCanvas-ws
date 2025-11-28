import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { Cart, CartItem } from '../../models/cart.model';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

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
  cart: Cart | null = null;
  cartItems: CartItem[] = [];
  total = 0;
  isLoading = true;
  error: string | null = null;
  private cartSubscription: any;

  constructor(
    private fb: FormBuilder,
    private cartService: CartService
  ) {
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
    this.loadCart();
  }

  handleImageError(event: Event) {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement && !imgElement.src.includes('placeholder')) {
      imgElement.src = 'assets/images/placeholder-painting.jpg';
    }
  }

  loadCart() {
    this.isLoading = true;
    this.error = null;

    // Unsubscribe from any existing subscription to prevent memory leaks
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }

    // First, get the current cart data
    this.cartService.getCart().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.cart = response.data;
          this.cartItems = response.data.items || [];
          this.total = response.data.totalAmount || 0;
          this.isLoading = false;
        } else {
          this.error = 'Failed to load cart data.';
          this.isLoading = false;
        }
      },
      error: (error) => {
        console.error('Failed to fetch cart', error);
        this.error = 'Failed to load cart. Please try again.';
        this.isLoading = false;
      }
    });

    // Then subscribe to cart updates
    this.cartSubscription = this.cartService.cart$.subscribe({
      next: (cart) => {
        if (cart) {
          this.cart = cart;
          this.cartItems = cart.items || [];
          this.total = cart.totalAmount || 0;
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error in cart subscription', error);
        this.error = 'Error updating cart. Please refresh the page.';
        this.isLoading = false;
      }
    });
  }

  get f() {
    return this.checkoutForm.controls;
  }

  ngOnDestroy() {
    this.submitted = false;
    // Clean up the subscription when the component is destroyed
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
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
