import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { HttpClientModule } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, HttpClientModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  cartItems: any[] = [];
  total = 0;

  constructor(private http: HttpClient, private router: Router) { }

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    this.http.get<any>(`${environment.apiUrl}/cart`).subscribe({
      next: (response) => {
        if (response && response.success && response.data) {
          this.cartItems = response.data.items || [];
          this.total = response.data.totalAmount || 0;
        } else {
          this.cartItems = [];
          this.total = 0;
        }
      },
      error: (error) => {
        console.error('Failed to load cart', error);
        this.cartItems = [];
        this.total = 0;
      }
    });
  }

  removeItem(id: string) {
    const fullUrl = `${environment.apiUrl}/cart/items/${id}`;

    this.http.delete<any>(fullUrl).subscribe({
      next: () => {
        this.loadCart();
      },
      error: (error) => {
        console.error('Failed to remove item', error);
        if (error.status === 401) {
          alert('Please login to modify your cart');
        }
      }
    });
  }

  checkout() {
    const fullUrl = `${environment.apiUrl}/cart`;

    this.http.delete<any>(fullUrl).subscribe({
      next: () => {
        // Cart cleared, now navigate to checkout
        // this.router.navigate(['/checkout']); // Commented out since checkout still requires auth
      },
      error: (error) => {
        console.error('Failed to clear cart for checkout', error);
        if (error.status === 401) {
          // Redirect to login since checkout requires authentication
          this.router.navigate(['/login']);
        }
      }
    });
  }
}
