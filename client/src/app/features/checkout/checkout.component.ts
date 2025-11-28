import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { UserService } from '../../core/services/user.service';
import { OrderService } from '../../core/services/order.service';
import { Cart, CartItem } from '../../models/cart.model';
import { Address } from '../../models/user.model';
import { CreateOrderRequest } from '../../models/order.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit, OnDestroy {
  checkoutForm: FormGroup;
  submitted = false;
  loading = false;
  cart: Cart | null = null;
  cartItems: CartItem[] = [];
  total = 0;
  isLoading = true;
  error: string | null = null;

  addresses: Address[] = [];
  selectedAddressId: number | null = null;
  showNewAddressForm = false;

  private cartSubscription: Subscription | null = null;

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private userService: UserService,
    private orderService: OrderService,
    private router: Router
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
    this.loadAddresses();
  }

  loadAddresses() {
    this.userService.getAddresses().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.addresses = response.data;
          // If user has addresses, select the default one or the first one
          if (this.addresses.length > 0) {
            const defaultAddress = this.addresses.find(a => a.isDefault);
            this.selectAddress(defaultAddress ? defaultAddress.id! : this.addresses[0].id!);
          } else {
            this.showNewAddressForm = true;
          }
        }
      },
      error: (error) => {
        console.error('Failed to load addresses', error);
      }
    });
  }

  selectAddress(addressId: number) {
    this.selectedAddressId = addressId;
    this.showNewAddressForm = false;

    // Update form validators based on selection
    this.updateFormValidators();
  }

  toggleNewAddress() {
    this.showNewAddressForm = true;
    this.selectedAddressId = null;
    this.checkoutForm.reset({ paymentMethod: 'COD' });
    this.updateFormValidators();
  }

  updateFormValidators() {
    const addressControls = ['street', 'city', 'state', 'country', 'pincode'];

    if (this.showNewAddressForm) {
      addressControls.forEach(control => {
        this.checkoutForm.get(control)?.setValidators(Validators.required);
        this.checkoutForm.get(control)?.updateValueAndValidity();
      });
    } else {
      addressControls.forEach(control => {
        this.checkoutForm.get(control)?.clearValidators();
        this.checkoutForm.get(control)?.updateValueAndValidity();
      });
    }
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

    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }

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
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
  }

  onSubmit() {
    this.submitted = true;

    if (this.checkoutForm.invalid) {
      return;
    }

    if (!this.selectedAddressId && !this.showNewAddressForm) {
      this.error = 'Please select an address or add a new one.';
      return;
    }

    this.loading = true;
    const formValue = this.checkoutForm.value;

    const orderRequest: CreateOrderRequest = {
      deliveryAddress: this.showNewAddressForm ? {
        street: formValue.street,
        city: formValue.city,
        state: formValue.state,
        country: formValue.country,
        pincode: formValue.pincode,
        isDefault: false
      } : this.addresses.find(a => a.id === this.selectedAddressId)!,
      paymentMethod: formValue.paymentMethod,
      notes: ''
    };

    this.orderService.createOrder(orderRequest).subscribe({
      next: (response) => {
        if (response.success) {
          // Refresh cart count/data
          this.cartService.getCart().subscribe();
          this.router.navigate(['/order-confirmation'], {
            state: { order: response.data }
          });
        } else {
          this.error = response.message || 'Failed to place order';
          this.loading = false;
        }
      },
      error: (error) => {
        console.error('Order placement failed', error);
        this.error = error.error?.message || 'Failed to place order. Please try again.';
        this.loading = false;
      }
    });
  }
}
