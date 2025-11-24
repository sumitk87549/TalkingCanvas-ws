export interface Painting {
  id: number;
  title: string;
  description: string;
  artistName: string;
  price: number;
  currency: string;
  height: number;
  width: number;
  depth?: number;
  medium: string;
  yearCreated?: number;
  isAvailable: boolean;
  stockQuantity: number;
  adminRecommendation: boolean;
  recommendationText?: string;
  images: PaintingImage[];
  certificates: PaintingCertificate[];
  categories: Category[];
  seoTitle?: string;
  seoDescription?: string;
  seoKeywords?: string;
  viewCount: number;
  purchaseCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PaintingImage {
  id: number;
  imageUrl: string;
  fileName?: string;
  contentType?: string;
  size?: number;
  displayOrder: number;
  isPrimary: boolean;
}

export interface PaintingCertificate {
  id: number;
  certificateUrl: string;
  title: string;
  issuer?: string;
  issueDate?: string;
  description?: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface CreatePaintingRequest {
  title: string;
  description?: string;
  artistName?: string;
  price: number;
  currency?: string;
  height: number;
  width: number;
  depth?: number;
  medium: string;
  yearCreated?: number;
  isAvailable?: boolean;
  stockQuantity?: number;
  adminRecommendation?: boolean;
  recommendationText?: string;
  categoryIds?: number[];
  seoTitle?: string;
  seoDescription?: string;
  seoKeywords?: string;
}
