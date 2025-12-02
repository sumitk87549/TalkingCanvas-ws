import { Painting } from './painting.model';

export interface WishlistItem {
    id: number;
    paintingId: number;
    painting: Painting;
    addedAt: string;
}

export interface Wishlist {
    id?: number;
    items: WishlistItem[];
    totalItems: number;
}

export interface AddToWishlistRequest {
    paintingId: number;
}
