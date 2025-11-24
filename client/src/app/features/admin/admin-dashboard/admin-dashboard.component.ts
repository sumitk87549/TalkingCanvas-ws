import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  paintings: any[] = [];
  loading = true;
  totalPaintings = 0;
  totalViews = 0;
  totalPurchases = 0;

  constructor(
    private paintingService: PaintingService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.loadPaintings();
  }

  loadPaintings() {
    this.loading = true;
    this.paintingService.getAdminPaintings(0, 100).subscribe({
      next: (response) => {

        if (response.success && response.data) {
          this.paintings = response.data.content;
          this.calculateStats();
          console.log("paintings stats calculated successfully");
          // console.log("Paintings data:", this.paintings);
          this.loading = false;
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load paintings', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  calculateStats() {
    this.totalPaintings = this.paintings.length;
    this.totalViews = this.paintings.reduce((sum, p) => sum + (p.viewCount || 0), 0);
    this.totalPurchases = this.paintings.reduce((sum, p) => sum + (p.purchaseCount || 0), 0);
  }

  deletePainting(id: number) {
    if (confirm('Are you sure you want to delete this painting?')) {
      this.paintingService.deletePainting(id).subscribe({
        next: () => {
          this.loadPaintings();
        },
        error: (err) => console.error('Failed to delete painting', err)
      });
    }
  }

  editPainting(id: number) {
    this.router.navigate(['/admin/edit', id]);
  }
}
