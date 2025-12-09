import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';
import { CartService } from '../../../core/services/cart.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { Painting } from '../../../models/painting.model';
import { AddToCartRequest } from '../../../models/cart.model';
import { AddToWishlistRequest } from '../../../models/wishlist.model';
import { ApiResponse, PageResponse } from '../../../models/api-response.model';
import { ChangeDetectorRef, NgZone } from '@angular/core';
import { Subscription } from 'rxjs';
import { DominantColorDirective } from '../../../shared/directives/dominant-color.directive';

@Component({
  selector: 'app-painting-list',
  standalone: true,
  imports: [CommonModule, RouterModule, DominantColorDirective],
  templateUrl: './painting-list.component.html',
  styleUrls: ['./painting-list.component.scss']
})
export class PaintingListComponent implements OnInit, OnDestroy {
  paintings: Painting[] = [];
  loading = false;
  cartItemsMap: Map<number, number> = new Map(); // paintingId -> cartItemId
  paintingsInWishlist: Map<number, boolean> = new Map();
  wishlistItemIds: Map<number, number> = new Map(); // painting ID -> wishlist item ID
  currentImageIndexMap: Map<number, number> = new Map(); // paintingId -> current image index

  private subscription: Subscription = new Subscription();

  constructor(
    private paintingService: PaintingService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) { }

  ngOnInit() {
    this.loadPaintings();
    this.loadCart();

    // Subscribe to cart changes to update map
    this.subscription.add(
      this.cartService.cart$.subscribe(cart => {
        this.updateCartMap(cart);
      })
    );

    // Subscribe to wishlist changes
    this.subscription.add(
      this.wishlistService.wishlist$.subscribe(() => {
        this.checkIfPaintingsInWishlist();
      })
    );
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  loadCart() {
    this.cartService.getCart().subscribe();
  }

  loadPaintings() {
    this.loading = true;
    this.paintingService.getAllPaintings(0, 12, 'createdAt', 'desc').subscribe({
      next: (resp: ApiResponse<PageResponse<Painting>>) => {
        this.zone.run(() => {
          this.paintings = resp?.data?.content ?? [];
          // Initialize image index map for all paintings
          this.paintings.forEach(painting => {
            this.currentImageIndexMap.set(painting.id, 0);
          });
          this.loading = false;
          this.cdr.detectChanges();
          this.checkIfPaintingsInWishlist();
        });
      },
      error: (err) => {
        console.log("Error encountered\n" + err)
        this.zone.run(() => {
          this.paintings = [];
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  getPrimaryImageUrl(painting: Painting): string | undefined {
    if (!painting?.images?.length) return undefined;
    const primary = painting.images.find(i => i.isPrimary) || painting.images[0];
    return primary?.imageUrl;
  }

  getCurrentImageUrl(painting: Painting): string | undefined {
    if (!painting?.images?.length) return undefined;
    const currentIndex = this.currentImageIndexMap.get(painting.id) || 0;
    return painting.images[currentIndex]?.imageUrl;
  }

  nextImage(paintingId: number, imageCount: number, event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    const currentIndex = this.currentImageIndexMap.get(paintingId) || 0;
    const nextIndex = (currentIndex + 1) % imageCount;
    this.currentImageIndexMap.set(paintingId, nextIndex);
  }

  prevImage(paintingId: number, imageCount: number, event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    const currentIndex = this.currentImageIndexMap.get(paintingId) || 0;
    const prevIndex = currentIndex === 0 ? imageCount - 1 : currentIndex - 1;
    this.currentImageIndexMap.set(paintingId, prevIndex);
  }

  setImage(paintingId: number, index: number, event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    this.currentImageIndexMap.set(paintingId, index);
  }

  trackByPaintingId(index: number, item: Painting) { return item.id; }

  toggleCart(painting: Painting) {
    const cartItemId = this.cartItemsMap.get(painting.id);

    if (cartItemId) {
      // Remove from cart
      this.cartService.removeCartItem(cartItemId).subscribe({
        next: () => {
          // Cart subscription will update the map
        },
        error: (error) => {
          console.error('Failed to remove item from cart:', error);
        }
      });
    } else {
      // Add to cart
      const req: AddToCartRequest = { paintingId: painting.id, quantity: 1 };
      this.cartService.addToCart(req).subscribe({
        next: () => {
          // Cart subscription will update the map
        },
        error: (error) => {
          console.error('Failed to add item to cart:', error);
        }
      });
    }
  }

  toggleWishlist(painting: Painting) {
    const isInWishlist = this.paintingsInWishlist.get(painting.id) || false;
    const wishlistItemId = this.wishlistItemIds.get(painting.id);

    if (isInWishlist && wishlistItemId) {
      // Remove from wishlist
      this.wishlistService.removeFromWishlist(wishlistItemId).subscribe({
        next: () => {
          this.checkIfPaintingsInWishlist();
        },
        error: (error) => {
          console.error('Failed to remove from wishlist:', error);
        }
      });
    } else {
      // Add to wishlist
      const req: AddToWishlistRequest = { paintingId: painting.id };
      this.wishlistService.addToWishlist(req).subscribe({
        next: () => {
          this.checkIfPaintingsInWishlist();
        },
        error: (error) => {
          console.error('Failed to add to wishlist:', error);
        }
      });
    }
  }

  private updateCartMap(cart: any) {
    this.cartItemsMap.clear();
    if (cart && cart.items) {
      cart.items.forEach((item: any) => {
        this.cartItemsMap.set(item.paintingId, item.id);
      });
    }
    this.cdr.detectChanges();
  }

  private checkIfPaintingsInWishlist() {
    // Clear existing wishlist status
    this.paintingsInWishlist.clear();
    this.wishlistItemIds.clear();

    // Get the wishlist to find item IDs
    this.wishlistService.getWishlist().subscribe({
      next: (wishlistResponse) => {
        if (wishlistResponse.success && wishlistResponse.data) {
          const wishlist = wishlistResponse.data;
          // Create a map of painting IDs to wishlist item IDs
          wishlist.items.forEach(item => {
            this.paintingsInWishlist.set(item.paintingId, true);
            this.wishlistItemIds.set(item.paintingId, item.id);
          });
        }
        // Mark paintings not in wishlist as false
        this.paintings.forEach(painting => {
          if (!this.paintingsInWishlist.has(painting.id)) {
            this.paintingsInWishlist.set(painting.id, false);
          }
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error checking wishlist status:', err);
        this.paintings.forEach(painting => {
          this.paintingsInWishlist.set(painting.id, false);
        });
        this.cdr.detectChanges();
      }
    });
  }
}
