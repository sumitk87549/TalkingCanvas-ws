import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

export interface Painting {
  id: number;
  title: string;
  artist: string;
  price: string;
  image: string;
}

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

  ngOnInit(): void {
    // Simulate API call with timeout
    setTimeout(() => {
      this.initializeData();
      this.isLoading = false;
    }, 1000);
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

  private initializeData(): void {
    // Initialize featured paintings
    this.featuredPaintings = [
      {
        id: 1,
        title: 'Whispers of the Sea',
        artist: 'Elena Petrova',
        price: '$1,200',
        image: 'assets/images/placeholder-painting.jpg' // Using a placeholder
      },
      {
        id: 2,
        title: 'Mountain Serenity',
        artist: 'James Wilson',
        price: '$950',
        image: 'asse'
      },
      {
        id: 3,
        title: 'Urban Reflections',
        artist: 'Sophia Chen',
        price: '$1,500',
        image: 'assets/images/placeholder-painting.jpg'
      },
      {
        id: 4,
        title: 'Autumn Whispers',
        artist: 'Michael Brown',
        price: '$1,100',
        image: 'assets/images/placeholder-painting.jpg'
      }
    ];

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
