import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { AddToCartRequest, Cart } from '../../models/cart.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<Cart | null>(null);
  public cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient) {}

  getCart(): Observable<ApiResponse<Cart>> {
    return this.http.get<ApiResponse<Cart>>(`${environment.apiUrl}/cart`)
      .pipe(tap(response => {
        if (response.success && response.data) {
          this.cartSubject.next(response.data);
        }
      }));
  }

  addToCart(request: AddToCartRequest): Observable<ApiResponse<Cart>> {
    return this.http.post<ApiResponse<Cart>>(`${environment.apiUrl}/cart/add`, request)
      .pipe(tap(response => {
        if (response.success && response.data) {
          this.cartSubject.next(response.data);
        }
      }));
  }

  updateCartItem(itemId: number, quantity: number): Observable<ApiResponse<Cart>> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<ApiResponse<Cart>>(`${environment.apiUrl}/cart/items/${itemId}`, null, { params })
      .pipe(tap(response => {
        if (response.success && response.data) {
          this.cartSubject.next(response.data);
        }
      }));
  }

  removeCartItem(itemId: number): Observable<ApiResponse<Cart>> {
    return this.http.delete<ApiResponse<Cart>>(`${environment.apiUrl}/cart/items/${itemId}`)
      .pipe(tap(response => {
        if (response.success && response.data) {
          this.cartSubject.next(response.data);
        }
      }));
  }

  clearCart(): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/cart`)
      .pipe(tap(response => {
        if (response.success) {
          this.cartSubject.next(null);
        }
      }));
  }

  getCartItemCount(): number {
    const cart = this.cartSubject.value;
    return cart?.totalItems || 0;
  }
}
