import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  cartItems: any[] = [];
  total = 0;

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    // TODO: Implement load cart logic
  }

  removeItem(id: string) {
    // TODO: Implement remove item logic
  }

  checkout() {
    // TODO: Implement checkout logic
  }
}
