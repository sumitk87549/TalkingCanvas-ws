export interface User {
  id: number;
  name: string;
  email: string;
  contactNumber: string;
  role: string;
  isActive: boolean;
  addresses: Address[];
  createdAt: string;
  updatedAt: string;
}

export interface Address {
  id?: number;
  street: string;
  city: string;
  state?: string;
  country: string;
  pincode?: string;
  isDefault?: boolean;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  contactNumber: string;
  city: string;
  country: string;
  street?: string;
  state?: string;
  pincode?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  name: string;
  email: string;
  role: string;
  expiresIn: number;
}
