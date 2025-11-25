import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'logout',
    loadComponent: () => import('./features/auth/logout/logout.component').then(m => m.LogoutComponent)
  },
  {
    path: 'paintings',
    loadComponent: () => import('./features/paintings/painting-list/painting-list.component').then(m => m.PaintingListComponent)
  },
  {
    path: 'paintings/:id',
    loadComponent: () => import('./features/paintings/painting-detail/painting-detail.component').then(m => m.PaintingDetailComponent)
  },
  {
    path: 'cart',
    loadComponent: () => import('./features/cart/cart.component').then(m => m.CartComponent)
  },
  {
    path: 'checkout',
    canActivate: [authGuard],
    loadComponent: () => import('./features/checkout/checkout.component').then(m => m.CheckoutComponent)
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () => import('./features/orders/order-list/order-list.component').then(m => m.OrderListComponent)
  },
  {
    path: 'orders/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/orders/order-detail/order-detail.component').then(m => m.OrderDetailComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  {
    path: 'about',
    loadComponent: () => import('./features/static-pages/about/about.component').then(m => m.AboutComponent)
  },
  {
    path: 'contact',
    loadComponent: () => import('./features/static-pages/contact/contact.component').then(m => m.ContactComponent)
  },
  {
    path: 'privacy-policy',
    loadComponent: () => import('./features/static-pages/privacy-policy/privacy-policy.component').then(m => m.PrivacyPolicyComponent)
  },
  {
    path: 'terms-and-conditions',
    loadComponent: () => import('./features/static-pages/terms/terms.component').then(m => m.TermsComponent)
  },
  {
    path: 'shipping-and-delivery',
    loadComponent: () => import('./features/static-pages/shipping/shipping.component').then(m => m.ShippingComponent)
  },
  {
    path: 'return-and-refund',
    loadComponent: () => import('./features/static-pages/return-refund/return-refund.component').then(m => m.ReturnRefundComponent)
  },
  {
    path: 'cookie-policy',
    loadComponent: () => import('./features/static-pages/cookie-policy/cookie-policy.component').then(m => m.CookiePolicyComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
