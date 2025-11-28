export interface Cart {
  id: number;
  items: CartItem[];
  totalAmount: number;
  currency: string;
  totalItems: number;
  updatedAt: string;
}

export interface CartItem {
  id: number;
  paintingId: number;
  paintingTitle: string;
  artistName?: string;  // Made optional with ?
  medium?: string;      // Added optional medium property
  primaryImage: string;
  price: number;
  currency: string;
  quantity: number;
  subtotal: number;
  isAvailable: boolean;
  stockQuantity: number;
}

export interface AddToCartRequest {
  paintingId: number;
  quantity: number;
}
