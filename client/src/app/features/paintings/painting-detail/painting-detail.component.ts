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
  isInCart = false;

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

    // Subscribe to cart changes to update in-cart status
    this.cartService.cart$.subscribe(() => {
      this.checkIfInCart();
    });
  }

  loadPainting(id: string) {
    console.log('Loading painting with ID:', id);
    this.loading = true;
    const pid = Number(id);
    console.log('Converted to number:', pid);
    this.paintingService.getPaintingById(pid).subscribe({
      next: (resp: ApiResponse<Painting>) => {
        console.log('Painting API response:', resp);
        this.painting = resp.data || null;
        this.loading = false;
        this.cdr.detectChanges();
        this.checkIfInCart();
        console.log('Painting loaded:', this.painting);
      },
      error: (err) => {
        console.error('Error loading painting:', err);
        this.painting = null;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  addToCart() {
    if (!this.painting) return;
    const req: AddToCartRequest = { paintingId: this.painting.id, quantity: 1 };
    this.cartService.addToCart(req).subscribe({
      next: () => {
        this.checkIfInCart(); // Update local cart status
      },
      error: (error) => {
        console.error('Failed to add item to cart:', error);
      }
    });
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

  private checkIfInCart() {
    if (!this.painting?.id) {
      this.isInCart = false;
      this.cdr.detectChanges();
      return;
    }

    this.cartService.getItemCount(this.painting.id).subscribe({
      next: (response) => {
        if (response.success && response.data !== undefined) {
          this.isInCart = response.data > 0;
        } else {
          this.isInCart = false;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error checking if item is in cart:', err);
        this.isInCart = false;
        this.cdr.detectChanges();
      }
    });
  }
}
