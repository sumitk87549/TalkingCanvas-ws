import { Directive, ElementRef, Input, OnChanges, SimpleChanges, Renderer2 } from '@angular/core';

@Directive({
    selector: '[appDominantColor]',
    standalone: true
})
export class DominantColorDirective implements OnChanges {
    @Input('appDominantColor') imageUrl: string | undefined;

    constructor(private el: ElementRef, private renderer: Renderer2) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['imageUrl'] && this.imageUrl) {
            this.extractColor(this.imageUrl);
        }
    }

    private extractColor(url: string): void {
        const img = new Image();
        img.crossOrigin = 'Anonymous';
        img.src = url;

        img.onload = () => {
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            if (!ctx) return;

            canvas.width = 1;
            canvas.height = 1;

            // Draw image to 1x1 canvas to get average color
            ctx.drawImage(img, 0, 0, 1, 1);
            const [r, g, b] = ctx.getImageData(0, 0, 1, 1).data;

            // Set CSS variable on host element
            this.renderer.setStyle(this.el.nativeElement, '--dominant-color', `${r}, ${g}, ${b}`);
        };

        img.onerror = () => {
            // Fallback to white/default if image fails to load
            this.renderer.setStyle(this.el.nativeElement, '--dominant-color', '255, 255, 255');
        };
    }
}
