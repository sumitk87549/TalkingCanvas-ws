import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent implements OnInit {
  orders: any[] = [];
  loading = false;

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.loading = true;
    // TODO: Implement load orders logic
  }
}
