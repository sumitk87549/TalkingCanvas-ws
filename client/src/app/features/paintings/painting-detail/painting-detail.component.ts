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
    this.cartService.addToCart(req).subscribe();
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
