import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';
import { Painting } from '../../../models/painting.model';
import { ApiResponse } from '../../../models/api-response.model';
import { CartService } from '../../../core/services/cart.service';
import { AddToCartRequest } from '../../../models/cart.model';
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

  constructor(
    private route: ActivatedRoute,
    private paintingService: PaintingService,
    private cartService: CartService,
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
}
