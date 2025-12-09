import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { AddToWishlistRequest, Wishlist } from '../../models/wishlist.model';

@Injectable({
    providedIn: 'root'
})
export class WishlistService {
    private wishlistSubject = new BehaviorSubject<Wishlist | null>(null);
    public wishlist$ = this.wishlistSubject.asObservable();

    constructor(private http: HttpClient) { }

    getWishlist(): Observable<ApiResponse<Wishlist>> {
        return this.http.get<ApiResponse<Wishlist>>(`${environment.apiUrl}/wishlist`)
            .pipe(tap(response => {
                if (response.success && response.data) {
                    this.wishlistSubject.next(response.data);
                }
            }));
    }

    addToWishlist(request: AddToWishlistRequest): Observable<ApiResponse<Wishlist>> {
        return this.http.post<ApiResponse<Wishlist>>(`${environment.apiUrl}/wishlist/add`, request)
            .pipe(tap(response => {
                if (response.success && response.data) {
                    this.wishlistSubject.next(response.data);
                }
            }));
    }

    removeFromWishlist(itemId: number): Observable<ApiResponse<Wishlist>> {
        return this.http.delete<ApiResponse<Wishlist>>(`${environment.apiUrl}/wishlist/items/${itemId}`)
            .pipe(tap(response => {
                if (response.success && response.data) {
                    this.wishlistSubject.next(response.data);
                }
            }));
    }

    isInWishlist(paintingId: number): Observable<ApiResponse<boolean>> {
        return this.http.get<ApiResponse<boolean>>(`${environment.apiUrl}/wishlist/check/${paintingId}`);
    }

    getWishlistItemCount(): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${environment.apiUrl}/wishlist/count`);
    }

    clearWishlist(): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/wishlist`)
            .pipe(tap(response => {
                if (response.success) {
                    this.wishlistSubject.next(null);
                }
            }));
    }

    getWishlistCount(): number {
        const wishlist = this.wishlistSubject.value;
        return wishlist?.totalItems || 0;
    }
}
