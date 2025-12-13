import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { Order } from '../../models/order.model';

export interface UserProfile {
    id: number;
    name: string;
    email: string;
    role: string;
    isActive: boolean;
    profileEmoji?: string;
    contactNumber?: string;
    createdAt: string;
}

export interface DashboardStats {
    totalUsers: number;
    activeUsers: number;
    newUsersThisMonth: number;
    totalOrders: number;
    pendingOrders: number;
    confirmedOrders: number;
    shippedOrders: number;
    deliveredOrders: number;
    cancelledOrders: number;
    totalRevenue: number;
    revenueThisMonth: number;
    revenueThisYear: number;
    currency: string;
    totalPaintings: number;
    availablePaintings: number;
    outOfStockPaintings: number;
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
    first: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    constructor(private http: HttpClient) { }

    // Dashboard Stats
    getDashboardStats(): Observable<ApiResponse<DashboardStats>> {
        return this.http.get<ApiResponse<DashboardStats>>(`${environment.apiUrl}/admin/dashboard/stats`);
    }

    // Order Management
    getAllOrders(page: number = 0, size: number = 20, status?: string): Observable<ApiResponse<PageResponse<Order>>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (status) {
            params = params.set('status', status);
        }

        return this.http.get<ApiResponse<PageResponse<Order>>>(`${environment.apiUrl}/admin/orders`, { params });
    }

    updateOrderStatus(orderId: number, status: string, trackingInfo?: string): Observable<ApiResponse<Order>> {
        let params = new HttpParams().set('status', status);

        if (trackingInfo) {
            params = params.set('trackingInfo', trackingInfo);
        }

        return this.http.put<ApiResponse<Order>>(`${environment.apiUrl}/admin/orders/${orderId}/status`, null, { params });
    }

    // User Management
    getAllUsers(page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<UserProfile>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<UserProfile>>>(`${environment.apiUrl}/admin/users`, { params });
    }

    getUserById(userId: number): Observable<ApiResponse<UserProfile>> {
        return this.http.get<ApiResponse<UserProfile>>(`${environment.apiUrl}/admin/users/${userId}`);
    }

    toggleUserStatus(userId: number): Observable<ApiResponse<void>> {
        return this.http.put<ApiResponse<void>>(`${environment.apiUrl}/admin/users/${userId}/status`, null);
    }

    deleteUser(userId: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/admin/users/${userId}`);
    }

    promoteToAdmin(userId: number): Observable<ApiResponse<void>> {
        return this.http.put<ApiResponse<void>>(`${environment.apiUrl}/admin/users/${userId}/promote`, null);
    }
}
