import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';
import { ActuatorService, HealthResponse, MetricData } from '../../../core/services/actuator.service';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    MatTabsModule, 
    MatCardModule, 
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  paintings: any[] = [];
  loading = true;
  totalPaintings = 0;
  totalViews = 0;
  totalPurchases = 0;
  
  // Actuator metrics
  healthStatus: any;
  jvmMetrics: MetricData[] = [];
  httpMetrics: MetricData[] = [];
  dbMetrics: MetricData[] = [];
  loadingMetrics = false;
  activeTabIndex = 0;

  constructor(
    private paintingService: PaintingService,
    private actuatorService: ActuatorService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.loadPaintings();
    this.loadActuatorMetrics();
  }

  loadActuatorMetrics() {
    this.loadingMetrics = true;
    
    // Load health status
    this.actuatorService.getHealth().subscribe({
      next: (health) => {
        this.healthStatus = health;
        this.loadingMetrics = false;
      },
      error: (err) => {
        console.error('Failed to load health status', err);
        this.loadingMetrics = false;
      }
    });

    // Load JVM metrics
    this.actuatorService.getJvmMetrics().subscribe({
      next: (metrics) => this.jvmMetrics = metrics,
      error: (err) => console.error('Failed to load JVM metrics', err)
    });

    // Load HTTP metrics
    this.actuatorService.getHttpRequestMetrics().subscribe({
      next: (metrics) => this.httpMetrics = metrics,
      error: (err) => console.error('Failed to load HTTP metrics', err)
    });

    // Load DB metrics
    this.actuatorService.getDatabaseMetrics().subscribe({
      next: (metrics) => this.dbMetrics = metrics,
      error: (err) => console.error('Failed to load DB metrics', err)
    });
  }

  loadPaintings() {
    this.loading = true;
    this.paintingService.getAdminPaintings(0, 100).subscribe({
      next: (response) => {

        if (response.success && response.data) {
          this.paintings = response.data.content;
          this.calculateStats();
          console.log("paintings stats calculated successfully");
          // console.log("Paintings data:", this.paintings);
          this.loading = false;
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load paintings', err);
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

  // Helper method to convert health components to array
  getHealthComponents(): {name: string, status: string}[] {
    if (!this.healthStatus?.components) return [];
    return Object.entries(this.healthStatus.components).map(([name, component]: [string, any]) => ({
      name,
      status: component.status || 'UNKNOWN'
    }));
  }

  // Format metric values for display
  formatMetricValue(metric: any): string {
    if (!metric || metric.value === undefined) return 'N/A';
    
    // Format memory in MB or GB
    if (metric.name.toLowerCase().includes('memory')) {
      const valueInMB = metric.value / (1024 * 1024);
      if (valueInMB > 1024) {
        return `${(valueInMB / 1024).toFixed(2)} GB`;
      }
      return `${valueInMB.toFixed(2)} MB`;
    }
    
    // Format time in ms or s
    if (metric.name.toLowerCase().includes('time') || metric.name.toLowerCase().includes('duration')) {
      if (metric.value < 1) {
        return `${(metric.value * 1000).toFixed(2)} ms`;
      }
      return `${metric.value.toFixed(2)} s`;
    }
    
    // Format counts
    return metric.value.toLocaleString();
  }

  deletePainting(id: number) {
    if (confirm('Are you sure you want to delete this painting?')) {
      this.paintingService.deletePainting(id).subscribe({
        next: () => {
          this.loadPaintings();
        },
        error: (err) => console.error('Failed to delete painting', err)
      });
    }
  }

  editPainting(id: number) {
    this.router.navigate(['/admin/edit', id]);
  }
}
