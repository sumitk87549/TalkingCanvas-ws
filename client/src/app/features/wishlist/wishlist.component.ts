import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { WishlistService } from '../../core/services/wishlist.service';
import { Wishlist } from '../../models/wishlist.model';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-wishlist',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './wishlist.component.html',
    styleUrls: ['./wishlist.component.scss']
})
export class WishlistComponent implements OnInit, OnDestroy {
    wishlist: Wishlist | null = null;
    private subscription: Subscription = new Subscription();

    constructor(
        private wishlistService: WishlistService,
        private router: Router
    ) { }

    ngOnInit() {
        // Load wishlist data
        this.loadWishlist();

        // Subscribe to wishlist changes
        this.subscription.add(
            this.wishlistService.wishlist$.subscribe(wishlist => {
                this.wishlist = wishlist;
            })
        );
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    private loadWishlist() {
        this.wishlistService.getWishlist().subscribe({
            next: (response) => {
                // Wishlist data is handled by the wishlist$.next() in WishlistService
            },
            error: (error) => {
                console.error('Failed to load wishlist', error);
                this.wishlist = null;
                if (error.status === 401) {
                    this.router.navigate(['/login']);
                }
            }
        });
    }

    removeItem(id: number) {
        this.wishlistService.removeFromWishlist(id).subscribe({
            next: () => {
                this.loadWishlist();
            },
            error: (error) => {
                console.error('Failed to remove item from wishlist', error);
                if (error.status === 401) {
                    this.router.navigate(['/login']);
                }
            }
        });
    }

    viewPainting(paintingId: number) {
        this.router.navigate(['/paintings', paintingId]);
    }
}
