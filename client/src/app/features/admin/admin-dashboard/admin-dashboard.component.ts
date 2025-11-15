import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  stats: any = {};
  loading = false;

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.loading = true;
    // TODO: Implement load stats logic
  }
}
