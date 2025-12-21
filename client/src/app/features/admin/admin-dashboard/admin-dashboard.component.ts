import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PaintingService } from '../../../core/services/painting.service';
import { AdminService, UserProfile, DashboardStats } from '../../../core/services/admin.service';
import { ActuatorService, HealthResponse, MetricData } from '../../../core/services/actuator.service';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Order } from '../../../models/order.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTabsModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  // Paintings
  paintings: any[] = [];
  loading = true;
  totalPaintings = 0;
  totalViews = 0;
  totalPurchases = 0;

  // Orders
  orders: Order[] = [];
  loadingOrders = false;
  orderStatuses = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  // Users
  users: UserProfile[] = [];
  loadingUsers = false;

  // Dashboard Stats
  dashboardStats: DashboardStats | null = null;
  loadingStats = false;

  // Actuator metrics
  healthStatus: any = null;
  jvmMetrics: MetricData[] = [];
  httpMetrics: MetricData[] = [];
  dbMetrics: MetricData[] = [];
  loadingMetrics = false;
  activeTabIndex = 0;

  // Confirmation Modal
  showConfirmModal = false;
  confirmModalTitle = '';
  confirmModalMessage = '';
  confirmModalAction: (() => void) | null = null;
  confirmModalDanger = false;

  constructor(
    private paintingService: PaintingService,
    private adminService: AdminService,
    private actuatorService: ActuatorService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.loadDashboardStats();
    this.loadPaintings();
    this.loadOrders();
    this.loadUsers();
    this.loadActuatorMetrics();
  }

  // Dashboard Stats
  loadDashboardStats() {
    this.loadingStats = true;
    this.adminService.getDashboardStats().subscribe({
      next: (response) => {
        console.log('Dashboard Stats Response:', response);
        if (response && response.success && response.data) {
          this.dashboardStats = response.data;
        } else if (response && !response.success) {
          console.error('Dashboard stats API returned error:', response);
        }
        this.loadingStats = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load dashboard stats:', err);
        this.loadingStats = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Orders
  loadOrders() {
    this.loadingOrders = true;
    this.adminService.getAllOrders(0, 100).subscribe({
      next: (response) => {
        console.log('Orders API Response:', response);
        // Handle different response structures
        if (response && response.success && response.data) {
          // Check if data has content array (PageResponse)
          if (Array.isArray(response.data.content)) {
            this.orders = response.data.content;
          } else if (Array.isArray(response.data)) {
            this.orders = response.data;
          } else {
            this.orders = [];
          }
        } else if (response && Array.isArray(response)) {
          this.orders = response;
        } else {
          this.orders = [];
        }
        console.log('Parsed Orders:', this.orders);
        this.loadingOrders = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load orders:', err);
        this.orders = [];
        this.loadingOrders = false;
        this.cdr.detectChanges();
      }
    });
  }

  updateOrderStatus(orderId: number, newStatus: string) {
    this.showConfirmation(
      'Update Order Status',
      `Are you sure you want to change order status to "${newStatus}"?`,
      () => {
        this.adminService.updateOrderStatus(orderId, newStatus).subscribe({
          next: () => {
            this.loadOrders();
            this.loadDashboardStats();
          },
          error: (err) => console.error('Failed to update order status', err)
        });
      },
      false
    );
  }

  // Users
  loadUsers() {
    this.loadingUsers = true;
    this.adminService.getAllUsers(0, 100).subscribe({
      next: (response) => {
        console.log('Users API Response:', response);
        // Handle different response structures
        if (response && response.success && response.data) {
          // Check if data has content array (PageResponse)
          if (Array.isArray(response.data.content)) {
            this.users = response.data.content;
          } else if (Array.isArray(response.data)) {
            this.users = response.data;
          } else {
            this.users = [];
          }
        } else if (response && Array.isArray(response)) {
          this.users = response;
        } else {
          this.users = [];
        }
        console.log('Parsed Users:', this.users);
        this.loadingUsers = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load users:', err);
        this.users = [];
        this.loadingUsers = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleUserStatus(userId: number, userName: string, currentStatus: boolean) {
    const action = currentStatus ? 'disable' : 'enable';
    this.showConfirmation(
      `${currentStatus ? 'Disable' : 'Enable'} User`,
      `Are you sure you want to ${action} user "${userName}"?`,
      () => {
        this.adminService.toggleUserStatus(userId).subscribe({
          next: () => {
            this.loadUsers();
            this.loadDashboardStats();
          },
          error: (err) => console.error('Failed to toggle user status', err)
        });
      },
      currentStatus
    );
  }

  deleteUser(userId: number, userName: string) {
    this.showConfirmation(
      '⚠️ Delete User',
      `Are you sure you want to delete user "${userName}"? This action will deactivate the account.`,
      () => {
        this.adminService.deleteUser(userId).subscribe({
          next: () => {
            this.loadUsers();
            this.loadDashboardStats();
          },
          error: (err) => console.error('Failed to delete user', err)
        });
      },
      true
    );
  }

  promoteToAdmin(userId: number, userName: string) {
    this.showConfirmation(
      '👑 Promote to Admin',
      `Are you sure you want to grant ADMIN privileges to "${userName}"? This action gives full control over the application.`,
      () => {
        this.adminService.promoteToAdmin(userId).subscribe({
          next: () => {
            this.loadUsers();
          },
          error: (err) => console.error('Failed to promote user', err)
        });
      },
      true
    );
  }

  // Paintings
  loadPaintings() {
    this.loading = true;
    this.paintingService.getAdminPaintings(0, 100).subscribe({
      next: (response) => {
        console.log('Paintings Response:', response);
        if (response && response.success && response.data) {
          if (Array.isArray(response.data.content)) {
            this.paintings = response.data.content;
          } else if (Array.isArray(response.data)) {
            this.paintings = response.data;
          } else {
            this.paintings = [];
          }
          this.calculateStats();
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load paintings', err);
        this.paintings = [];
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  calculateStats() {
    this.totalPaintings = this.paintings.length;
    this.totalViews = this.paintings.reduce((sum, p) => sum + (p.viewCount || 0), 0);
    this.totalPurchases = this.paintings.reduce((sum, p) => sum + (p.purchaseCount || 0), 0);
  }

  deletePainting(id: number, title: string) {
    this.showConfirmation(
      '🗑️ Delete Painting',
      `Are you sure you want to delete "${title}"? This painting will be permanently removed from the database.`,
      () => {
        this.paintingService.deletePainting(id).subscribe({
          next: () => {
            this.loadPaintings();
            this.loadDashboardStats();
          },
          error: (err) => console.error('Failed to delete painting', err)
        });
      },
      true
    );
  }

  editPainting(id: number) {
    this.router.navigate(['/admin/edit', id]);
  }

  // Actuator Metrics
  loadActuatorMetrics() {
    this.loadingMetrics = true;

    this.actuatorService.getHealth().subscribe({
      next: (health) => {
        console.log('Health Response:', health);
        this.healthStatus = health;
        this.loadingMetrics = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load health status', err);
        // Set a default status when health check fails
        this.healthStatus = { status: 'UNKNOWN', components: {} };
        this.loadingMetrics = false;
        this.cdr.detectChanges();
      }
    });

    this.actuatorService.getJvmMetrics().subscribe({
      next: (metrics) => {
        this.jvmMetrics = metrics;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load JVM metrics', err)
    });

    this.actuatorService.getHttpRequestMetrics().subscribe({
      next: (metrics) => {
        this.httpMetrics = metrics;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load HTTP metrics', err)
    });

    this.actuatorService.getDatabaseMetrics().subscribe({
      next: (metrics) => {
        this.dbMetrics = metrics;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load DB metrics', err)
    });
  }

  getHealthComponents(): { name: string, status: string }[] {
    if (!this.healthStatus?.components) return [];
    return Object.entries(this.healthStatus.components).map(([name, component]: [string, any]) => ({
      name,
      status: component.status || 'UNKNOWN'
    }));
  }

  // Calculate effective health status (ignoring mail)
  getEffectiveHealthStatus(): string {
    if (!this.healthStatus) return 'UNKNOWN';

    // If overall status is UP, return UP
    if (this.healthStatus.status === 'UP') return 'UP';

    // Check if only mail is down (which is non-critical)
    if (this.healthStatus.components) {
      const components = Object.entries(this.healthStatus.components);
      const nonMailComponents = components.filter(([name]) => name !== 'mail');
      const allNonMailUp = nonMailComponents.every(([, comp]: [string, any]) => comp.status === 'UP');

      if (allNonMailUp && nonMailComponents.length > 0) {
        return 'UP'; // Mail is down but everything else is UP
      }
    }

    return this.healthStatus.status || 'UNKNOWN';
  }

  formatMetricValue(metric: any): string {
    if (!metric || metric.value === undefined) return 'N/A';

    if (metric.name.toLowerCase().includes('memory')) {
      const valueInMB = metric.value / (1024 * 1024);
      if (valueInMB > 1024) {
        return `${(valueInMB / 1024).toFixed(2)} GB`;
      }
      return `${valueInMB.toFixed(2)} MB`;
    }

    if (metric.name.toLowerCase().includes('time') || metric.name.toLowerCase().includes('duration')) {
      if (metric.value < 1) {
        return `${(metric.value * 1000).toFixed(2)} ms`;
      }
      return `${metric.value.toFixed(2)} s`;
    }

    return metric.value.toLocaleString();
  }

  // Confirmation Modal
  showConfirmation(title: string, message: string, action: () => void, isDanger: boolean = false) {
    this.confirmModalTitle = title;
    this.confirmModalMessage = message;
    this.confirmModalAction = action;
    this.confirmModalDanger = isDanger;
    this.showConfirmModal = true;
  }

  confirmAction() {
    if (this.confirmModalAction) {
      this.confirmModalAction();
    }
    this.closeConfirmModal();
  }

  closeConfirmModal() {
    this.showConfirmModal = false;
    this.confirmModalAction = null;
  }

  // Utility
  formatCurrency(amount: number): string {
    if (amount === undefined || amount === null) return '₹0';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount);
  }
}
