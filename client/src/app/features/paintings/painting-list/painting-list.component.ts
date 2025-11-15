import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-painting-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './painting-list.component.html',
  styleUrls: ['./painting-list.component.scss']
})
export class PaintingListComponent implements OnInit {
  paintings: any[] = [];
  loading = false;

  ngOnInit() {
    this.loadPaintings();
  }

  loadPaintings() {
    this.loading = true;
    // TODO: Implement load paintings logic
  }
}
