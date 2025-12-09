import { Component, HostListener, AfterViewInit, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd } from "@angular/router";
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { ThemeService } from '../../../core/services/theme.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthResponse } from '../../../models/user.model';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class Navbar implements AfterViewInit, OnInit {
  isHeaderInView = true;
  private headerSection: HTMLElement | null = null;
  currentUser: AuthResponse | null = null;
  isAdmin = false;
  cartItemCount = 0;
  wishlistItemCount = 0;
  isMobileMenuOpen = false;
  isDayMode = false; // Default to dark theme initially
  isAutoMode = false; // Track current theme mode (auto/manual)

  constructor(
    public authService: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    public themeService: ThemeService
  ) {
    // Close mobile menu on route change
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.closeMobileMenu();
    });
  }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAdmin = this.authService.isAdmin();
      if (user) {
        this.loadCartItemCount();
        this.loadWishlistItemCount();
      } else {
        this.cartItemCount = 0;
        this.wishlistItemCount = 0;
      }
    });

    // Subscribe to cart changes
    this.cartService.cart$.subscribe(cart => {
      this.cartItemCount = cart?.totalItems || 0;
      this.cdr.markForCheck();
    });

    // Subscribe to wishlist changes
    this.wishlistService.wishlist$.subscribe((wishlist: any) => {
      this.wishlistItemCount = wishlist?.totalItems || 0;
      this.cdr.markForCheck();
    });

    // Subscribe to theme changes
    this.themeService.currentTheme$.subscribe(theme => {
      this.isDayMode = theme === 'day';
      this.cdr.markForCheck();
    });

    // Subscribe to theme mode changes
    this.themeService.currentThemeMode$.subscribe((mode: string) => {
      this.isAutoMode = mode === 'auto';
      this.cdr.markForCheck();
    });
  }

  private loadCartItemCount() {
    this.cartService.getTotalItemsCount().subscribe((response: any) => {
      if (response.success && response.data !== undefined) {
        this.cartItemCount = response.data;
      }
    }, (error: any) => {
      console.error('Error loading cart count:', error);
      this.cartItemCount = 0;
    });
  }

  private loadWishlistItemCount() {
    this.wishlistService.getWishlistItemCount().subscribe((response: any) => {
      if (response.success && response.data !== undefined) {
        this.wishlistItemCount = response.data;
      }
    }, (error: any) => {
      console.error('Error loading wishlist count:', error);
      this.wishlistItemCount = 0;
    });
  }

  ngAfterViewInit() {
    // Find the header section
    this.headerSection = document.querySelector('header.hero');
    if (this.headerSection) {
      this.checkHeaderContact();
    }
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.checkHeaderContact();
  }

  private checkHeaderContact() {
    if (!this.headerSection) {
      this.headerSection = document.querySelector('header.hero');
      if (!this.headerSection) {
        // If no header found, show colored background
        this.isHeaderInView = false;
        return;
      }
    }

    const headerRect = this.headerSection.getBoundingClientRect();
    // Check if header is still visible (in contact with navbar)
    // Header is in view (transparent) only if its bottom edge is below the navbar top (> 0)
    // Once header scrolls past top, show colored background
    this.isHeaderInView = headerRect.bottom > 0;
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
    // Prevent body scroll when menu is open
    if (this.isMobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }

  closeMobileMenu() {
    this.isMobileMenuOpen = false;
    document.body.style.overflow = '';
  }

  get navbarClasses() {
    return {
      'transparent': this.isHeaderInView && !this.isMobileMenuOpen,
      'colored': !this.isHeaderInView || this.isMobileMenuOpen,
      'mobile-open': this.isMobileMenuOpen
    };
  }

  toggleTheme() {
    this.themeService.toggleTheme();
  }

  toggleThemeMode() {
    this.themeService.toggleThemeMode();
  }

  getThemeButtonLabel(): string {
    if (this.isAutoMode) {
      return `Auto mode (Currently ${this.isDayMode ? 'Day' : 'Night'}). Click to switch theme.`;
    } else {
      return `Manual mode (${this.isDayMode ? 'Day' : 'Night'}). Click to switch theme.`;
    }
  }
}
