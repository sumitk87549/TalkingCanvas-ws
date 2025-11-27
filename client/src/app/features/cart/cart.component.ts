import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { PaintingService } from '../../core/services/painting.service';
import { Cart } from '../../models/cart.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit, OnDestroy {
  cart: Cart | null = null;
  artistNames: Map<number, string> = new Map();
  paintingMediums: Map<number, string> = new Map();
  loadingDetails = new Set<number>(); // Track which painting details are being loaded
  private subscription: Subscription = new Subscription();

  constructor(
    private cartService: CartService,
    private paintingService: PaintingService,
    private router: Router
  ) {}

  ngOnInit() {
    // Load cart data
    this.loadCart();

    // Subscribe to cart changes and load painting details
    this.subscription.add(
      this.cartService.cart$.subscribe(cart => {
        this.cart = cart;
        if (cart?.items && cart.items.length > 0) {
          this.loadPaintingDetails(cart.items);
        }
      })
    );
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  private loadPaintingDetails(cartItems: any[]) {
    if (!cartItems || cartItems.length === 0) return;

    // Only fetch details for paintings that we don't already have or are not currently loading
    const paintingsToFetch = cartItems.filter(item => 
      !this.loadingDetails.has(item.paintingId) && 
      (!this.artistNames.has(item.paintingId) || !this.paintingMediums.has(item.paintingId))
    );

    paintingsToFetch.forEach(item => {
      this.loadingDetails.add(item.paintingId);
      
      this.paintingService.getPaintingById(item.paintingId).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.artistNames.set(item.paintingId, response.data.artistName || 'Not specified');
            this.paintingMediums.set(item.paintingId, response.data.medium || 'Not specified');
          } else {
            this.artistNames.set(item.paintingId, 'Not specified');
            this.paintingMediums.set(item.paintingId, 'Not specified');
          }
          this.loadingDetails.delete(item.paintingId);
        },
        error: (error) => {
          console.error(`Failed to load painting details for painting ${item.paintingId}`, error);
          this.artistNames.set(item.paintingId, 'Not specified');
          this.paintingMediums.set(item.paintingId, 'Not specified');
          this.loadingDetails.delete(item.paintingId);
        }
      });
    });
  }

  private loadCart() {
    this.cartService.getCart().subscribe({
      next: (response) => {
        // Cart data is handled by the cart$.next() in CartService
      },
      error: (error) => {
        console.error('Failed to load cart', error);
        this.cart = null;
        if (error.status === 401) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  removeItem(id: number) {
    this.cartService.removeCartItem(id).subscribe({
      next: () => {
        this.loadCart();
      },
      error: (error) => {
        console.error('Failed to remove item', error);
        if (error.status === 401) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  checkout() {
    this.cartService.clearCart().subscribe({
      next: () => {
        // Cart cleared, now navigate to checkout
        // this.router.navigate(['/checkout']); // Commented out since checkout still requires auth
      },
      error: (error) => {
        console.error('Failed to clear cart for checkout', error);
        if (error.status === 401) {
          this.router.navigate(['/login']);
        }
      }
    });
  }
}

// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { RouterModule, Router } from '@angular/router';
// import { HttpClient } from '@angular/common/http';
// import { HttpClientModule } from '@angular/common/http';
// import { environment } from '../../../environments/environment';
//
// @Component({
//   selector: 'app-cart',
//   standalone: true,
//   imports: [CommonModule, RouterModule, HttpClientModule],
//   templateUrl: './cart.component.html',
//   styleUrls: ['./cart.component.scss']
// })
// export class CartComponent implements OnInit {
//   cartItems: any[] = [];
//   total = 0;
//
//   constructor(private http: HttpClient, private router: Router) { }
//
//   ngOnInit() {
//     this.loadCart();
//   }
//
//   loadCart() {
//     this.http.get<any>(`${environment.apiUrl}/cart`).subscribe({
//       next: (response) => {
//         if (response && response.success && response.data) {
//           this.cartItems = response.data.items || [];
//           this.total = response.data.totalAmount || 0;
//         } else {
//           this.cartItems = [];
//           this.total = 0;
//         }
//       },
//       error: (error) => {
//         console.error('Failed to load cart', error);
//         this.cartItems = [];
//         this.total = 0;
//       }
//     });
//   }
//
//   removeItem(id: string) {
//     const fullUrl = `${environment.apiUrl}/cart/items/${id}`;
//
//     this.http.delete<any>(fullUrl).subscribe({
//       next: () => {
//         this.loadCart();
//       },
//       error: (error) => {
//         console.error('Failed to remove item', error);
//         if (error.status === 401) {
//           alert('Please login to modify your cart');
//         }
//       }
//     });
//   }
//
//   checkout() {
//     const fullUrl = `${environment.apiUrl}/cart`;
//
//     this.http.delete<any>(fullUrl).subscribe({
//       next: () => {
//         // Cart cleared, now navigate to checkout
//         // this.router.navigate(['/checkout']); // Commented out since checkout still requires auth
//       },
//       error: (error) => {
//         console.error('Failed to clear cart for checkout', error);
//         if (error.status === 401) {
//           // Redirect to login since checkout requires authentication
//           this.router.navigate(['/login']);
//         }
//       }
//     });
//   }
// }
