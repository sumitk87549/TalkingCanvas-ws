import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { User, Address } from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient) { }

  getProfile(): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${environment.apiUrl}/users/profile`);
  }

  updateProfile(data: { name: string; contactNumber: string }): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${environment.apiUrl}/users/profile`, data);
  }

  changePassword(data: { currentPassword: string; newPassword: string }): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${environment.apiUrl}/users/change-password`, data);
  }

  // Address Management Methods
  getAddresses(): Observable<ApiResponse<Address[]>> {
    return this.http.get<ApiResponse<Address[]>>(`${environment.apiUrl}/users/addresses`);
  }

  addAddress(address: Address): Observable<ApiResponse<Address>> {
    return this.http.post<ApiResponse<Address>>(`${environment.apiUrl}/users/addresses`, address);
  }

  updateAddress(id: number, address: Address): Observable<ApiResponse<Address>> {
    return this.http.put<ApiResponse<Address>>(`${environment.apiUrl}/users/addresses/${id}`, address);
  }

  deleteAddress(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/users/addresses/${id}`);
  }

  setDefaultAddress(id: number): Observable<ApiResponse<Address>> {
    return this.http.put<ApiResponse<Address>>(`${environment.apiUrl}/users/addresses/${id}/set-default`, {});
  }
}
