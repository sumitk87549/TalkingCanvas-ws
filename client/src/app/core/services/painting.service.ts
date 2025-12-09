import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/api-response.model';
import { Category, CreatePaintingRequest, Painting } from '../../models/painting.model';
import { clearHttpCache } from '../interceptors/cache.interceptor';

@Injectable({
  providedIn: 'root'
})
export class PaintingService {
  constructor(private http: HttpClient) { }

  getAllPaintings(page: number = 0, size: number = 12, sortBy: string = 'createdAt', sortDirection: string = 'desc'): Observable<ApiResponse<PageResponse<Painting>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    return this.http.get<ApiResponse<PageResponse<Painting>>>(`${environment.apiUrl}/paintings`, { params });
  }

  getPaintingById(id: number): Observable<ApiResponse<Painting>> {
    return this.http.get<ApiResponse<Painting>>(`${environment.apiUrl}/paintings/${id}`);
  }

  getFeaturedPaintings(page: number = 0, size: number = 12): Observable<ApiResponse<PageResponse<Painting>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponse<Painting>>>(`${environment.apiUrl}/paintings/featured`, { params });
  }

  searchPaintings(query: string, page: number = 0, size: number = 12): Observable<ApiResponse<PageResponse<Painting>>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponse<Painting>>>(`${environment.apiUrl}/paintings/search`, { params });
  }

  filterByPriceRange(minPrice: number, maxPrice: number, page: number = 0, size: number = 12): Observable<ApiResponse<PageResponse<Painting>>> {
    const params = new HttpParams()
      .set('minPrice', minPrice.toString())
      .set('maxPrice', maxPrice.toString())
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponse<Painting>>>(`${environment.apiUrl}/paintings/filter/price`, { params });
  }

  getAllCategories(): Observable<ApiResponse<Category[]>> {
    return this.http.get<ApiResponse<Category[]>>(`${environment.apiUrl}/paintings/categories`);
  }

  // Admin endpoints
  getAdminPaintings(page: number = 0, size: number = 12, sortBy: string = 'createdAt', sortDirection: string = 'desc'): Observable<ApiResponse<PageResponse<Painting>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    return this.http.get<ApiResponse<PageResponse<Painting>>>(`${environment.apiUrl}/admin/paintings`, { params });
  }

  createPainting(request: CreatePaintingRequest): Observable<ApiResponse<Painting>> {
    return this.http.post<ApiResponse<Painting>>(`${environment.apiUrl}/admin/paintings`, request).pipe(
      tap(() => clearHttpCache())
    );
  }

  updatePainting(id: number, request: CreatePaintingRequest): Observable<ApiResponse<Painting>> {
    return this.http.put<ApiResponse<Painting>>(`${environment.apiUrl}/admin/paintings/${id}`, request).pipe(
      tap(() => clearHttpCache())
    );
  }

  deletePainting(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/admin/paintings/${id}`).pipe(
      tap(() => clearHttpCache())
    );
  }

  uploadImages(paintingId: number, files: File[]): Observable<ApiResponse<void>> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return this.http.post<ApiResponse<void>>(`${environment.apiUrl}/admin/paintings/${paintingId}/images`, formData).pipe(
      tap(() => clearHttpCache())
    );
  }

  deleteImage(paintingId: number, imageId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/admin/paintings/${paintingId}/images/${imageId}`).pipe(
      tap(() => clearHttpCache())
    );
  }

  uploadCertificate(paintingId: number, file: File, title: string, issuer?: string, issueDate?: string, description?: string): Observable<ApiResponse<void>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (issuer) formData.append('issuer', issuer);
    if (issueDate) formData.append('issueDate', issueDate);
    if (description) formData.append('description', description);
    return this.http.post<ApiResponse<void>>(`${environment.apiUrl}/admin/paintings/${paintingId}/certificates`, formData);
  }

  createCategory(name: string, description?: string): Observable<ApiResponse<Category>> {
    const params = new HttpParams()
      .set('name', name)
      .set('description', description || '');
    return this.http.post<ApiResponse<Category>>(`${environment.apiUrl}/admin/categories`, null, { params });
  }
}
