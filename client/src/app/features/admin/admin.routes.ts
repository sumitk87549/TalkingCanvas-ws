import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
  },
  {
    path: 'add',
    loadComponent: () => import('./add-painting/add-painting.component').then(m => m.AddPaintingComponent)
  },
  {
    path: 'edit/:id',
    loadComponent: () => import('./add-painting/add-painting.component').then(m => m.AddPaintingComponent)
  }
];
