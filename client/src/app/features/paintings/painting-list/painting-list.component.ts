import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';
import { CartService } from '../../../core/services/cart.service';
import { Painting } from '../../../models/painting.model';
import { AddToCartRequest } from '../../../models/cart.model';
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
  private subscription: Subscription = new Subscription();

  constructor(
    private paintingService: PaintingService,
    private cartService: CartService,
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
          this.loading = false;
          this.cdr.detectChanges();
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

  private updateCartMap(cart: any) {
    this.cartItemsMap.clear();
    if (cart && cart.items) {
      cart.items.forEach((item: any) => {
        this.cartItemsMap.set(item.paintingId, item.id);
      });
    }
    this.cdr.detectChanges();
  }
}
