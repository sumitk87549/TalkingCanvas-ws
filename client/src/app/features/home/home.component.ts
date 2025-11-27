import { Component, OnInit, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PaintingService } from '../../core/services/painting.service';
import { Painting } from '../../models/painting.model';
import { ApiResponse, PageResponse } from '../../models/api-response.model';

export interface Artist {
  id: number;
  name: string;
  specialty: string;
  bio: string;
  image: string;
}

export interface Testimonial {
  id: number;
  name: string;
  role: string;
  text: string;
  avatar: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  featuredPaintings: Painting[] = [];
  featuredArtists: Artist[] = [];
  testimonials: Testimonial[] = [];
  isLoading = true;

  constructor(
    public authService: AuthService,
    private paintingService: PaintingService,
    private el: ElementRef
  ) { }

  ngOnInit(): void {
    this.loadFeaturedPaintings();
    this.initializeStaticData();
  }

  @HostListener('mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    const heroContent = this.el.nativeElement.querySelector('.hero-content');
    if (heroContent) {
      const rect = heroContent.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
      heroContent.style.setProperty('--x', `${x}px`);
      heroContent.style.setProperty('--y', `${y}px`);
    }
  }

  loadFeaturedPaintings(): void {
    this.isLoading = true;
    this.paintingService.getFeaturedPaintings(0, 6).subscribe({
      next: (resp: ApiResponse<PageResponse<Painting>>) => {
        this.featuredPaintings = resp.data?.content || [];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load featured paintings', err);
        this.isLoading = false;
      }
    });
  }

  getPrimaryImageUrl(painting: Painting): string | undefined {
    if (!painting?.images?.length) return undefined;
    const primary = painting.images.find(i => i.isPrimary) || painting.images[0];
    return primary?.imageUrl;
  }

  // Handle image loading errors
  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement.src.includes('placeholder')) {
      imgElement.style.display = 'none';
      if (imgElement.parentElement) {
        imgElement.parentElement.style.backgroundColor = '#f0f0f0';
      }
    } else {
      imgElement.src = 'assets/images/placeholder-painting.jpg';
    }
  }

  // Handle newsletter subscription
  onSubscribe(event: Event): void {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const emailInput = form.querySelector('input[type="email"]') as HTMLInputElement;

    if (emailInput?.value) {
      alert(`Thank you for subscribing with ${emailInput.value}! We'll keep you updated.`);
      form.reset();
    }
  }

  private initializeStaticData(): void {
    // Initialize featured artists
    this.featuredArtists = [
      {
        id: 1,
        name: 'Elena Petrova',
        specialty: 'Impressionist Landscapes',
        bio: 'Elena captures the essence of natural landscapes with her unique impressionist style.',
        image: 'assets/images/placeholder-avatar.jpg' // Using a placeholder
      },
      {
        id: 2,
        name: 'James Wilson',
        specialty: 'Abstract Expressionism',
        bio: 'James creates bold, emotional pieces that challenge traditional perspectives.',
        image: 'assets/images/placeholder-avatar.jpg'
      },
      {
        id: 3,
        name: 'Sophia Chen',
        specialty: 'Contemporary Realism',
        bio: 'Sophia\'s hyper-realistic paintings blur the line between photography and art.',
        image: 'assets/images/placeholder-avatar.jpg'
      }
    ];

    // Initialize testimonials
    this.testimonials = [
      {
        id: 1,
        name: 'Alex Johnson',
        role: 'Art Collector',
        text: 'The quality of the paintings I\'ve purchased has been exceptional. Each piece tells a unique story.',
        avatar: 'assets/images/placeholder-avatar.jpg' // Using a placeholder
      },
      {
        id: 2,
        name: 'Maria Garcia',
        role: 'Interior Designer',
        text: 'My clients are always impressed with the artwork I source from Talking Canvas. The artists are incredibly talented.',
        avatar: 'assets/images/placeholder-avatar.jpg'
      },
      {
        id: 3,
        name: 'David Kim',
        role: 'Gallery Owner',
        text: 'The platform makes it easy to discover emerging artists with unique styles. Highly recommended for art lovers.',
        avatar: 'assets/images/placeholder-avatar.jpg'
      }
    ];
  }
}
