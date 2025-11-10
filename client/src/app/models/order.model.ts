import { Address } from './user.model';

export interface Order {
  id: number;
  orderNumber: string;
  items: OrderItem[];
  totalAmount: number;
  currency: string;
  deliveryAddress: Address;
  orderStatus: string;
  paymentMethod: string;
  trackingInfo?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  adminContact: AdminContact;
}

export interface OrderItem {
  id: number;
  paintingId: number;
  paintingTitle: string;
  artistName: string;
  quantity: number;
  priceAtPurchase: number;
  subtotal: number;
}

export interface AdminContact {
  name: string;
  email: string;
  phone: string;
}

export interface CreateOrderRequest {
  deliveryAddress: Address;
  notes?: string;
}
