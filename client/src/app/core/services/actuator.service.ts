import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface HealthResponse {
  status: string;
  components: {
    [key: string]: {
      status: string;
      details?: any;
    };
  };
}

export interface MetricResponse {
  name: string;
  description: string;
  baseUnit: string;
  measurements: Array<{
    statistic: string;
    value: number;
  }>;
  availableTags: Array<{
    tag: string;
    values: string[];
  }>;
}

export interface MetricData {
  name: string;
  value: number;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class ActuatorService {
  private readonly apiUrl = `${environment.apiUrl}/actuator`;

  constructor(private http: HttpClient) {}

  /**
   * Get application health status
   */
  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${this.apiUrl}/health`);
  }

  /**
   * Get all available metrics
   */
  getAllMetrics(): Observable<MetricResponse[]> {
    return this.http.get<MetricResponse[]>(`${this.apiUrl}/metrics`);
  }

  /**
   * Get specific metric by name
   * @param metricName Name of the metric to fetch
   */
  getMetric(metricName: string): Observable<MetricResponse> {
    return this.http.get<MetricResponse>(`${this.apiUrl}/metrics/${metricName}`);
  }

  /**
   * Get metric with specific tag
   * @param metricName Name of the metric
   * @param tag Tag to filter by
   */
  getMetricWithTag(metricName: string, tag: string): Observable<MetricResponse> {
    return this.http.get<MetricResponse>(`${this.apiUrl}/metrics/${metricName}/${tag}`);
  }

  /**
   * Get application information
   */
  getAppInfo(): Observable<any> {
    return this.http.get(`${this.apiUrl}/info`);
  }

  /**
   * Get JVM metrics
   */
  getJvmMetrics(): Observable<MetricData[]> {
    return new Observable(observer => {
      this.getMetric('jvm.memory.used').subscribe({
        next: (data) => {
          const metrics: MetricData[] = [];
          // Process JVM memory metrics
          if (data.measurements && data.measurements.length > 0) {
            metrics.push({
              name: 'JVM Memory Used',
              value: data.measurements[0].value,
              description: 'Amount of used JVM memory in bytes'
            });
          }
          observer.next(metrics);
          observer.complete();
        },
        error: (err) => observer.error(err)
      });
    });
  }

  /**
   * Get HTTP request metrics
   */
  getHttpRequestMetrics(): Observable<MetricData[]> {
    return new Observable(observer => {
      this.getMetric('http.server.requests').subscribe({
        next: (data) => {
          const metrics: MetricData[] = [];
          if (data.measurements && data.measurements.length > 0) {
            data.measurements.forEach(measurement => {
              metrics.push({
                name: `HTTP Requests (${measurement.statistic})`,
                value: measurement.value,
                description: `HTTP request ${measurement.statistic.toLowerCase()}`
              });
            });
          }
          observer.next(metrics);
          observer.complete();
        },
        error: (err) => observer.error(err)
      });
    });
  }

  /**
   * Get database connection pool metrics
   */
  getDatabaseMetrics(): Observable<MetricData[]> {
    return new Observable(observer => {
      this.getMetric('jdbc.connections.active').subscribe({
        next: (data) => {
          const metrics: MetricData[] = [];
          if (data.measurements && data.measurements.length > 0) {
            metrics.push({
              name: 'Active Database Connections',
              value: data.measurements[0].value,
              description: 'Number of active database connections'
            });
          }
          observer.next(metrics);
          observer.complete();
        },
        error: (err) => observer.error(err)
      });
    });
  }
}
