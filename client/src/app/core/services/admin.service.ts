import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/api-response.model';
import { Order } from '../../models/order.model';

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    constructor(private http: HttpClient) { }

    getAllOrders(page: number = 0, size: number = 20, status?: string): Observable<ApiResponse<any>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (status) {
            params = params.set('status', status);
        }

        return this.http.get<ApiResponse<any>>(`${environment.apiUrl}/admin/orders`, { params });
    }

    updateOrderStatus(orderId: number, status: string, trackingInfo?: string): Observable<ApiResponse<Order>> {
        let params = new HttpParams().set('status', status);

        if (trackingInfo) {
            params = params.set('trackingInfo', trackingInfo);
        }

        return this.http.put<ApiResponse<Order>>(`${environment.apiUrl}/admin/orders/${orderId}/status`, null, { params });
    }
}
