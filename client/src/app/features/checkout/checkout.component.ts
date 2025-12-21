import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { UserService } from '../../core/services/user.service';
import { OrderService } from '../../core/services/order.service';
import { Cart, CartItem } from '../../models/cart.model';
import { Address } from '../../models/user.model';
import { CreateOrderRequest } from '../../models/order.model';
import { Subscription, forkJoin } from 'rxjs';

interface CheckoutStep {
  id: number;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComponent implements OnInit, OnDestroy {
  // Checkout steps
  steps: CheckoutStep[] = [
    { id: 1, label: 'Cart', icon: '🛒' },
    { id: 2, label: 'Delivery', icon: '📍' },
    { id: 3, label: 'Payment', icon: '💳' },
    { id: 4, label: 'Confirm', icon: '✅' }
  ];
  currentStep = signal(2); // Start at Delivery (cart already reviewed)

  // Form
  checkoutForm: FormGroup;
  submitted = signal(false);

  // Loading states with signals for better performance
  isLoading = signal(true);
  isSubmitting = signal(false);
  error = signal<string | null>(null);

  // Data
  cart = signal<Cart | null>(null);
  cartItems = computed(() => this.cart()?.items || []);
  total = computed(() => this.cart()?.totalAmount || 0);
  itemCount = computed(() => this.cart()?.totalItems || 0);

  addresses = signal<Address[]>([]);
  selectedAddressId = signal<number | null>(null);
  showNewAddressForm = signal(false);

  // New address as default checkbox
  saveNewAddress = signal(true);

  // Contact phone for delivery
  deliveryPhone = signal('');

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
      phone: ['', [Validators.required, Validators.pattern('^[0-9]{10,15}$')]],
      paymentMethod: ['COD', Validators.required],
      saveAddress: [true]
    });
  }

  ngOnInit() {
    this.loadCheckoutData();
  }

  /**
   * Load cart and addresses in PARALLEL using forkJoin for faster checkout page load
   */
  loadCheckoutData() {
    this.isLoading.set(true);
    this.error.set(null);

    forkJoin({
      cart: this.cartService.getCart(),
      addresses: this.userService.getAddresses()
    }).subscribe({
      next: (results) => {
        // Process cart data
        if (results.cart.success && results.cart.data) {
          this.cart.set(results.cart.data);
        } else {
          this.error.set('Failed to load cart data.');
        }

        // Process addresses
        if (results.addresses.success && results.addresses.data) {
          this.addresses.set(results.addresses.data);
          if (results.addresses.data.length > 0) {
            const defaultAddress = results.addresses.data.find(a => a.isDefault);
            this.selectAddress(defaultAddress?.id ?? results.addresses.data[0].id!);
          } else {
            this.showNewAddressForm.set(true);
          }
        }

        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to load checkout data', error);
        this.error.set('Failed to load checkout data. Please try again.');
        this.isLoading.set(false);
      }
    });

    // Subscribe to cart updates
    this.cartSubscription = this.cartService.cart$.subscribe({
      next: (cartData) => {
        if (cartData) {
          this.cart.set(cartData);
        }
      }
    });
  }

  selectAddress(addressId: number) {
    this.selectedAddressId.set(addressId);
    this.showNewAddressForm.set(false);
    this.updateFormValidators();
  }

  toggleNewAddress() {
    this.showNewAddressForm.set(true);
    this.selectedAddressId.set(null);
    this.checkoutForm.reset({ paymentMethod: 'COD', saveAddress: true });
    this.updateFormValidators();
  }

  updateFormValidators() {
    const addressControls = ['street', 'city', 'state', 'country', 'pincode'];

    if (this.showNewAddressForm()) {
      addressControls.forEach(control => {
        this.checkoutForm.get(control)?.setValidators(Validators.required);
        this.checkoutForm.get(control)?.updateValueAndValidity();
      });
      this.checkoutForm.get('phone')?.setValidators([Validators.required, Validators.pattern('^[0-9]{10,15}$')]);
    } else {
      addressControls.forEach(control => {
        this.checkoutForm.get(control)?.clearValidators();
        this.checkoutForm.get(control)?.updateValueAndValidity();
      });
      // Phone is still required for selected address
      this.checkoutForm.get('phone')?.setValidators([Validators.required, Validators.pattern('^[0-9]{10,15}$')]);
    }
    this.checkoutForm.get('phone')?.updateValueAndValidity();
  }

  handleImageError(event: Event) {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement && !imgElement.src.includes('placeholder')) {
      imgElement.src = 'assets/images/placeholder-painting.jpg';
    }
  }

  get f() {
    return this.checkoutForm.controls;
  }

  ngOnDestroy() {
    this.submitted.set(false);
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
  }

  goToStep(step: number) {
    if (step === 1) {
      this.router.navigate(['/cart']);
      return;
    }
    if (step <= this.currentStep() + 1) {
      this.currentStep.set(step);
    }
  }

  proceedToPayment() {
    // If no addresses exist, we must be in new address mode effectively
    const isNewAddressMode = this.showNewAddressForm() || this.addresses().length === 0;

    if (!this.selectedAddressId() && !isNewAddressMode) {
      this.error.set('Please select an address or add a new one.');
      return;
    }

    if (isNewAddressMode) {
      const addressControls = ['street', 'city', 'state', 'country', 'pincode'];
      let hasErrors = false;
      addressControls.forEach(control => {
        if (!this.checkoutForm.get(control)?.value) {
          this.checkoutForm.get(control)?.markAsTouched();
          hasErrors = true;
        }
      });
      if (hasErrors) {
        this.error.set('Please fill in all address fields.');
        return;
      }
    }

    this.error.set(null);
    this.currentStep.set(3);
  }

  proceedToConfirm() {
    if (!this.checkoutForm.get('phone')?.value) {
      this.checkoutForm.get('phone')?.markAsTouched();
      this.error.set('Please enter a contact phone number.');
      return;
    }
    this.error.set(null);
    this.currentStep.set(4);
  }

  onSubmit() {
    this.submitted.set(true);
    const isNewAddressMode = this.showNewAddressForm() || this.addresses().length === 0;

    if (!this.selectedAddressId() && !isNewAddressMode) {
      this.error.set('Please select an address or add a new one.');
      return;
    }

    this.isSubmitting.set(true);
    this.error.set(null);
    const formValue = this.checkoutForm.value;

    const orderRequest: CreateOrderRequest = {
      deliveryAddress: isNewAddressMode ? {
        street: formValue.street,
        city: formValue.city,
        state: formValue.state,
        country: formValue.country,
        pincode: formValue.pincode,
        isDefault: formValue.saveAddress
      } : this.addresses().find(a => a.id === this.selectedAddressId())!,
      paymentMethod: formValue.paymentMethod,
      notes: formValue.phone ? `Contact: ${formValue.phone}` : ''
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
          this.error.set(response.message || 'Failed to place order');
          this.isSubmitting.set(false);
        }
      },
      error: (error) => {
        console.error('Order placement failed', error);
        this.error.set(error.error?.message || 'Failed to place order. Please try again.');
        this.isSubmitting.set(false);
      }
    });
  }

  getSelectedAddress(): Address | null {
    if (this.showNewAddressForm() || this.addresses().length === 0) {
      const formValue = this.checkoutForm.value;
      return {
        street: formValue.street,
        city: formValue.city,
        state: formValue.state,
        country: formValue.country,
        pincode: formValue.pincode
      };
    }
    return this.addresses().find(a => a.id === this.selectedAddressId()) || null;
  }
}
