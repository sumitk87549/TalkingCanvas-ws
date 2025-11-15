import { Component, HostListener, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class Navbar implements AfterViewInit {
  isHeaderInView = true; // Start as true (transparent) on page load
  private headerSection: HTMLElement | null = null;

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
