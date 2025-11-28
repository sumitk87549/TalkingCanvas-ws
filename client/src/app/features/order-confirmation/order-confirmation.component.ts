import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Order } from '../../models/order.model';

@Component({
    selector: 'app-order-confirmation',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './order-confirmation.component.html',
    styleUrls: ['./order-confirmation.component.scss']
})
export class OrderConfirmationComponent implements OnInit {
    order: Order | null = null;

    constructor(private router: Router) {
        const navigation = this.router.getCurrentNavigation();
        if (navigation?.extras.state) {
            this.order = navigation.extras.state['order'];
        }
    }

    ngOnInit() {
        if (!this.order) {
            // If no order in state, redirect to home
            this.router.navigate(['/']);
        }
    }
}
