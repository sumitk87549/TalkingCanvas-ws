import { Component, OnInit, OnDestroy, HostListener, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { OrderService } from '../../core/services/order.service';
import { User, Address } from '../../models/user.model';
import { Order } from '../../models/order.model';
import { Subscription, finalize } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {
  user: User | null = null;
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  addressForm!: FormGroup;
  isSaving = false;
  isChangingPassword = false;
  showChangePasswordModal = false;
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  // Emoji selector
  showEmojiPicker = false;
  availableEmojis = [
    '😊', '😎', '🤓', '🥳', '🎨', '🖼️', '🎭', '🌟', '💎', '🦄', '🐼', '🦊', '🐱', '🐶', '🚀'
  ];

  // Order History
  orders: Order[] = [];
  loadingOrders = false;

  // Address Book
  addresses: Address[] = [];
  showAddressModal = false;
  isEditMode = false;
  editingAddressId: number | null = null;
  loadingAddresses = false;

  // Feedback messages
  success: string | null = null;
  error: string | null = null;
  saveSuccess = false;

  private userSubscription: Subscription | undefined;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private orderService: OrderService,
    private router: Router
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.loadUserProfile();
    this.loadOrders();
    this.loadAddresses();
  }

  ngOnDestroy(): void {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  private initializeForms(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.pattern(/^[0-9]{10,15}$/)]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });

    this.addressForm = this.fb.group({
      street: ['', [Validators.required]],
      city: ['', [Validators.required]],
      state: [''],
      country: ['', [Validators.required]],
      pincode: [''],
      isDefault: [false]
    });
  }

  private passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'mismatch': true };
  }

  private loadUserProfile(): void {
    this.userSubscription = this.authService.currentUser$.subscribe(user => {
      this.userService.getProfile().subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.user = response.data;
            this.patchProfileForm(this.user);
          }
        },
        error: (err) => console.error('Failed to load profile', err)
      });
    });
  }

  private patchProfileForm(user: User) {
    this.profileForm.patchValue({
      name: user.name || '',
      email: user.email || '',
      phone: user.contactNumber || ''
    });
  }

  updateProfile(): void {
    if (this.profileForm.valid) {
      this.isSaving = true;
      this.saveSuccess = false;
      this.error = null;

      const formValue = this.profileForm.value;
      const updateData = {
        name: formValue.name,
        contactNumber: formValue.phone,
        profileEmoji: this.user?.profileEmoji
      };

      this.userService.updateProfile(updateData)
        .pipe(finalize(() => this.isSaving = false))
        .subscribe({
          next: (response) => {
            if (response.success && response.data) {
              this.user = response.data;
              this.authService.updateUser(this.user); // Update auth state
              this.profileForm.markAsPristine();
              this.saveSuccess = true;
              setTimeout(() => this.saveSuccess = false, 3000);
            } else {
              this.error = response.message || 'Failed to update profile';
              setTimeout(() => this.error = null, 3000);
            }
          },
          error: (error) => {
            console.error('Error updating profile:', error);
            this.error = error.error?.message || 'Failed to update profile';
            setTimeout(() => this.error = null, 3000);
          }
        });
    }
  }

  @ViewChild('emojiPicker') emojiPickerElement!: ElementRef;

  // Handle clicks outside the emoji picker
  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    if (this.showEmojiPicker &&
      this.emojiPickerElement &&
      !this.emojiPickerElement.nativeElement.contains(event.target) &&
      !(event.target as HTMLElement).closest('.emoji-circle, .edit-emoji-btn')) {
      this.showEmojiPicker = false;
    }
  }

  // Emoji Selector Methods
  toggleEmojiPicker(event: Event): void {
    event.stopPropagation();
    this.showEmojiPicker = !this.showEmojiPicker;
  }

  selectEmoji(emoji: string): void {
    if (this.user) {
      this.user.profileEmoji = emoji;
      this.showEmojiPicker = false;

      // Update profile with new emoji
      const updateData = {
        name: this.user.name,
        contactNumber: this.user.contactNumber,
        profileEmoji: emoji
      };

      this.userService.updateProfile(updateData).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.user = response.data;
            this.authService.updateUser(this.user); // Update auth state
            this.success = 'Emoji updated successfully!';
            setTimeout(() => this.success = null, 3000);
          }
        },
        error: (error) => {
          console.error('Error updating emoji:', error);
          this.error = 'Failed to update emoji';
          setTimeout(() => this.error = null, 3000);
        }
      });
    }
  }

  // Password Change Methods
  openChangePassword(): void {
    this.showChangePasswordModal = true;
    this.passwordForm.reset();
  }

  closeChangePassword(): void {
    this.showChangePasswordModal = false;
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;

    this.isChangingPassword = true;
    this.error = null;

    const { currentPassword, newPassword } = this.passwordForm.value;

    this.userService.changePassword({ currentPassword, newPassword })
      .pipe(finalize(() => this.isChangingPassword = false))
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.showChangePasswordModal = false;
            this.passwordForm.reset();
            this.success = 'Password changed successfully!';
            setTimeout(() => this.success = null, 3000);
          }
        },
        error: (error: Error) => {
          console.error('Error changing password:', error);
          this.error = 'Failed to change password. Please check your current password.';
        }
      });
  }

  togglePasswordVisibility(field: 'current' | 'new' | 'confirm'): void {
    switch (field) {
      case 'current':
        this.showCurrentPassword = !this.showCurrentPassword;
        break;
      case 'new':
        this.showNewPassword = !this.showNewPassword;
        break;
      case 'confirm':
        this.showConfirmPassword = !this.showConfirmPassword;
        break;
    }
  }

  // Order History Methods
  loadOrders(): void {
    this.loadingOrders = true;
    this.orderService.getUserOrders(0, 20).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.orders = response.data;
        }
        this.loadingOrders = false;
      },
      error: (error) => {
        console.error('Failed to load orders:', error);
        this.loadingOrders = false;
      }
    });
  }

  getOrderStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'status-pending',
      'CONFIRMED': 'status-confirmed',
      'SHIPPED': 'status-shipped',
      'DELIVERED': 'status-delivered',
      'CANCELLED': 'status-cancelled'
    };
    return statusMap[status] || 'status-pending';
  }

  // Address Book Methods
  loadAddresses(): void {
    this.loadingAddresses = true;
    this.userService.getAddresses().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.addresses = response.data;
        }
        this.loadingAddresses = false;
      },
      error: (error) => {
        console.error('Failed to load addresses:', error);
        this.loadingAddresses = false;
      }
    });
  }

  openAddAddress(): void {
    this.isEditMode = false;
    this.editingAddressId = null;
    this.addressForm.reset({ isDefault: false });
    this.showAddressModal = true;
  }

  editAddress(address: Address): void {
    this.isEditMode = true;
    this.editingAddressId = address.id || null;
    this.addressForm.patchValue({
      street: address.street,
      city: address.city,
      state: address.state,
      country: address.country,
      pincode: address.pincode,
      isDefault: address.isDefault
    });
    this.showAddressModal = true;
  }

  saveAddress(): void {
    if (this.addressForm.invalid) return;

    const addressData = this.addressForm.value;

    if (this.isEditMode && this.editingAddressId) {
      this.userService.updateAddress(this.editingAddressId, addressData).subscribe({
        next: (response) => {
          if (response.success) {
            this.loadAddresses();
            this.closeAddressModal();
            this.success = 'Address updated successfully!';
            setTimeout(() => this.success = null, 3000);
          }
        },
        error: (error) => {
          console.error('Error updating address:', error);
          this.error = 'Failed to update address';
          setTimeout(() => this.error = null, 3000);
        }
      });
    } else {
      this.userService.addAddress(addressData).subscribe({
        next: (response) => {
          if (response.success) {
            this.loadAddresses();
            this.closeAddressModal();
            this.success = 'Address added successfully!';
            setTimeout(() => this.success = null, 3000);
          }
        },
        error: (error) => {
          console.error('Error adding address:', error);
          this.error = 'Failed to add address';
          setTimeout(() => this.error = null, 3000);
        }
      });
    }
  }

  deleteAddress(id: number | undefined): void {
    if (!id) return;

    if (confirm('Are you sure you want to delete this address?')) {
      this.userService.deleteAddress(id).subscribe({
        next: (response) => {
          if (response.success) {
            this.loadAddresses();
            this.success = 'Address deleted successfully!';
            setTimeout(() => this.success = null, 3000);
          }
        },
        error: (error) => {
          console.error('Error deleting address:', error);
          this.error = 'Failed to delete address';
          setTimeout(() => this.error = null, 3000);
        }
      });
    }
  }

  setDefaultAddress(id: number | undefined): void {
    if (!id) return;

    this.userService.setDefaultAddress(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.loadAddresses();
          this.success = 'Default address set successfully!';
          setTimeout(() => this.success = null, 3000);
        }
      },
      error: (error) => {
        console.error('Error setting default address:', error);
        this.error = 'Failed to set default address';
        setTimeout(() => this.error = null, 3000);
      }
    });
  }

  closeAddressModal(): void {
    this.showAddressModal = false;
    this.addressForm.reset();
    this.isEditMode = false;
    this.editingAddressId = null;
  }
}
