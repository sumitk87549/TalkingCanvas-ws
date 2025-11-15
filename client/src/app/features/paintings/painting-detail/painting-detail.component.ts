import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-painting-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './painting-detail.component.html',
  styleUrls: ['./painting-detail.component.scss']
})
export class PaintingDetailComponent implements OnInit {
  painting: any = null;
  loading = false;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.loadPainting(params['id']);
    });
  }

  loadPainting(id: string) {
    this.loading = true;
    // TODO: Implement load painting logic
  }

  addToCart() {
    // TODO: Implement add to cart logic
  }
}
