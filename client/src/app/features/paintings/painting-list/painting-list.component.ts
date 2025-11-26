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

@Component({
  selector: 'app-painting-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './painting-list.component.html',
  styleUrls: ['./painting-list.component.scss']
})
export class PaintingListComponent implements OnInit, OnDestroy {
  paintings: Painting[] = [];
  loading = false;
  paintingsInCart: Map<number, boolean> = new Map();
  private subscription: Subscription = new Subscription();

  constructor(
    private paintingService: PaintingService,
    private cartService: CartService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) { }

  ngOnInit() {
    this.loadPaintings();

    // Subscribe to cart changes to update in-cart status
    this.subscription.add(
      this.cartService.cart$.subscribe(() => {
        this.checkIfPaintingsInCart();
      })
    );
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  loadPaintings() {
    console.log("inside loadPaintings()")
    this.loading = true;
    this.paintingService.getAllPaintings(0, 12, 'createdAt', 'desc').subscribe({
      next: (resp: ApiResponse<PageResponse<Painting>>) => {
        console.log("painting service load Paintings success \n", resp);
        this.zone.run(() => {
          this.paintings = resp?.data?.content ?? [];
          this.loading = false;
          this.cdr.detectChanges();
          this.checkIfPaintingsInCart(); // Check cart status after paintings load
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

  addToCart(painting: Painting) {
    const req: AddToCartRequest = { paintingId: painting.id, quantity: 1 };
    this.cartService.addToCart(req).subscribe({
      next: () => {
        this.checkIfPaintingsInCart(); // Update cart status
      },
      error: (error) => {
        console.error('Failed to add item to cart:', error);
      }
    });
  }

  private checkIfPaintingsInCart() {
    // Clear existing cart status
    this.paintingsInCart.clear();

    // Check each painting in the cart
    this.paintings.forEach(painting => {
      this.cartService.getItemCount(painting.id).subscribe({
        next: (response) => {
          if (response.success && response.data !== undefined) {
            this.paintingsInCart.set(painting.id, response.data > 0);
          } else {
            this.paintingsInCart.set(painting.id, false);
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(`Error checking if painting ${painting.id} is in cart:`, err);
          this.paintingsInCart.set(painting.id, false);
          this.cdr.detectChanges();
        }
      });
    });
  }
}
