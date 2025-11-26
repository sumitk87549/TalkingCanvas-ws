import { Component, HostListener, AfterViewInit, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from "@angular/router";
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { AuthResponse } from '../../../models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class Navbar implements AfterViewInit, OnInit {
  isHeaderInView = true; // Start as true (transparent) on page load
  private headerSection: HTMLElement | null = null;
  currentUser: AuthResponse | null = null;
  isAdmin = false;
  cartItemCount = 0;

  constructor(public authService: AuthService, private cartService: CartService) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAdmin = this.authService.isAdmin();
      if (user) {
        this.loadCartItemCount();
      } else {
        this.cartItemCount = 0;
      }
    });

    this.cartService.cart$.subscribe(cart => {
      this.cartItemCount = cart?.totalItems || 0;
    });
  }

  private loadCartItemCount() {
    this.cartService.getTotalItemsCount().subscribe(response => {
      if (response.success && response.data !== undefined) {
        this.cartItemCount = response.data;
      }
    }, error => {
      console.error('Error loading cart count:', error);
      this.cartItemCount = 0;
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



  get navbarClasses() {
    return {
      'transparent': this.isHeaderInView,
      'colored': !this.isHeaderInView
    };
  }
}
