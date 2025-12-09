import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';
import { Painting } from '../../../models/painting.model';
import { ApiResponse } from '../../../models/api-response.model';
import { CartService } from '../../../core/services/cart.service';
import { AddToCartRequest } from '../../../models/cart.model';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AddToWishlistRequest } from '../../../models/wishlist.model';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-painting-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './painting-detail.component.html',
  styleUrls: ['./painting-detail.component.scss']
})
export class PaintingDetailComponent implements OnInit {
  painting: Painting | null = null;
  loading = false;
  cartItemId: number | null = null;
  private currentCart: any = null;
  isInWishlist = false;
  wishlistItemId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private paintingService: PaintingService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.loadPainting(params['id']);
    });

    // Subscribe to cart changes
    this.cartService.cart$.subscribe(cart => {
      this.currentCart = cart;
      this.updateCartStatus();
    });

    // Subscribe to wishlist changes
    this.wishlistService.wishlist$.subscribe(() => {
      this.checkIfInWishlist();
    });
  }

  loadPainting(id: string) {
    console.log('Loading painting with ID:', id);
    this.loading = true;
    const pid = Number(id);
    this.paintingService.getPaintingById(pid).subscribe({
      next: (resp: ApiResponse<Painting>) => {
        this.painting = resp.data || null;
        this.loading = false;
        this.updateCartStatus();
        this.checkIfInWishlist();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading painting:', err);
        this.painting = null;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleCart() {
    if (!this.painting) return;

    if (this.cartItemId) {
      // Remove from cart
      this.cartService.removeCartItem(this.cartItemId).subscribe({
        error: (error) => console.error('Failed to remove item from cart:', error)
      });
    } else {
      // Add to cart
      const req: AddToCartRequest = { paintingId: this.painting.id, quantity: 1 };
      this.cartService.addToCart(req).subscribe({
        error: (error) => console.error('Failed to add item to cart:', error)
      });
    }
  }

  private updateCartStatus() {
    this.cartItemId = null;
    if (this.painting && this.currentCart && this.currentCart.items) {
      const cartItem = this.currentCart.items.find((item: any) => item.paintingId === this.painting?.id);
      if (cartItem) {
        this.cartItemId = cartItem.id;
      }
    }
    this.cdr.detectChanges();
  }

  getPrimaryImageUrl(): string | undefined {
    if (!this.painting?.images?.length) return undefined;
    const primary = this.painting.images.find(i => i.isPrimary) || this.painting.images[0];
    return primary?.imageUrl;
  }

  setPrimaryImage(index: number) {
    if (!this.painting?.images?.length) return;
    // Reset all images to not primary
    this.painting.images.forEach(img => img.isPrimary = false);
    // Set the selected image as primary
    this.painting.images[index].isPrimary = true;
    this.cdr.detectChanges();
  }

  getCategoryNames(): string {
    if (!this.painting?.categories?.length) return '';
    return this.painting.categories.map(cat => cat.name).join(', ');
  }

  toggleWishlist() {
    if (!this.painting) return;

    if (this.isInWishlist && this.wishlistItemId) {
      // Remove from wishlist
      this.wishlistService.removeFromWishlist(this.wishlistItemId).subscribe({
        next: () => {
          this.checkIfInWishlist();
        },
        error: (error) => {
          console.error('Failed to remove from wishlist:', error);
        }
      });
    } else {
      // Add to wishlist
      const req: AddToWishlistRequest = { paintingId: this.painting.id };
      this.wishlistService.addToWishlist(req).subscribe({
        next: () => {
          this.checkIfInWishlist();
        },
        error: (error) => {
          console.error('Failed to add to wishlist:', error);
        }
      });
    }
  }

  private checkIfInWishlist() {
    if (!this.painting?.id) {
      this.isInWishlist = false;
      this.wishlistItemId = null;
      this.cdr.detectChanges();
      return;
    }

    // Check if painting is in wishlist
    this.wishlistService.isInWishlist(this.painting.id).subscribe({
      next: (response) => {
        if (response.success && response.data !== undefined) {
          this.isInWishlist = response.data;
          // If in wishlist, find the item ID
          if (this.isInWishlist) {
            this.wishlistService.getWishlist().subscribe({
              next: (wishlistResponse) => {
                if (wishlistResponse.success && wishlistResponse.data) {
                  const item = wishlistResponse.data.items.find(
                    i => i.paintingId === this.painting!.id
                  );
                  this.wishlistItemId = item ? item.id : null;
                }
                this.cdr.detectChanges();
              }
            });
          } else {
            this.wishlistItemId = null;
            this.cdr.detectChanges();
          }
        } else {
          this.isInWishlist = false;
          this.wishlistItemId = null;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error checking if item is in wishlist:', err);
        this.isInWishlist = false;
        this.wishlistItemId = null;
        this.cdr.detectChanges();
      }
    });
  }
}
