import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';
import { Painting } from '../../../models/painting.model';
import { ApiResponse, PageResponse } from '../../../models/api-response.model';
import { ChangeDetectorRef, NgZone } from '@angular/core';

@Component({
  selector: 'app-painting-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './painting-list.component.html',
  styleUrls: ['./painting-list.component.scss']
})
export class PaintingListComponent implements OnInit {
  paintings: Painting[] = [];
  loading = false;

  constructor(
    private paintingService: PaintingService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) { }

  ngOnInit() {
    this.loadPaintings();
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
}
