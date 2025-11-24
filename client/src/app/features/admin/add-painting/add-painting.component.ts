import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PaintingService } from '../../../core/services/painting.service';
import { Category, CreatePaintingRequest } from '../../../models/painting.model';

@Component({
    selector: 'app-add-painting',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './add-painting.component.html',
    styleUrls: ['./add-painting.component.scss']
})
export class AddPaintingComponent implements OnInit {
    paintingForm: FormGroup;
    isEditMode = false;
    paintingId: number | null = null;
    categories: Category[] = [];
    selectedFiles: File[] = [];
    loading = false;
    error = '';

    constructor(
        private fb: FormBuilder,
        private paintingService: PaintingService,
        private router: Router,
        private route: ActivatedRoute
    ) {
        this.paintingForm = this.fb.group({
            title: ['', Validators.required],
            artistName: [''],
            price: ['', [Validators.required, Validators.min(0.01)]],
            currency: ['INR', Validators.required],
            height: ['', Validators.required],
            width: ['', Validators.required],
            depth: [0],
            medium: ['', Validators.required],
            yearCreated: [new Date().getFullYear()],
            description: [''],
            isAvailable: [true],
            stockQuantity: [1, [Validators.required, Validators.min(0)]],
            adminRecommendation: [false],
            recommendationText: [''],
            categoryIds: [[]]
        });
    }

    ngOnInit(): void {
        this.loadCategories();
        this.route.params.subscribe(params => {
            if (params['id']) {
                this.isEditMode = true;
                this.paintingId = +params['id'];
                this.loadPainting(this.paintingId);
            }
        });
    }

    loadCategories() {
        this.paintingService.getAllCategories().subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.categories = response.data;
                }
            },
            error: (err) => console.error('Failed to load categories', err)
        });
    }

    loadPainting(id: number) {
        this.loading = true;
        this.paintingService.getPaintingById(id).subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    const painting = response.data;
                    this.paintingForm.patchValue({
                        title: painting.title,
                        artistName: painting.artistName,
                        price: painting.price,
                        currency: painting.currency,
                        height: painting.height,
                        width: painting.width,
                        depth: painting.depth,
                        medium: painting.medium,
                        yearCreated: painting.yearCreated,
                        description: painting.description,
                        isAvailable: painting.isAvailable,
                        stockQuantity: painting.stockQuantity,
                        adminRecommendation: painting.adminRecommendation,
                        recommendationText: painting.recommendationText,
                        categoryIds: painting.categories?.map(c => c.id) || []
                    });
                }
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to load painting details';
                this.loading = false;
            }
        });
    }

    onFileSelected(event: any) {
        this.selectedFiles = Array.from(event.target.files);
    }

    onSubmit() {
        if (this.paintingForm.invalid) {
            return;
        }

        this.loading = true;
        this.error = '';
        const request: CreatePaintingRequest = this.paintingForm.value;

        if (this.isEditMode && this.paintingId) {
            this.paintingService.updatePainting(this.paintingId, request).subscribe({
                next: (response) => {
                    if (this.selectedFiles.length > 0) {
                        this.uploadImages(this.paintingId!);
                    } else {
                        this.router.navigate(['/admin']);
                    }
                },
                error: (err) => {
                    this.error = 'Failed to update painting';
                    this.loading = false;
                }
            });
        } else {
            this.paintingService.createPainting(request).subscribe({
                next: (response) => {
                    if (response.success && response.data && this.selectedFiles.length > 0) {
                        this.uploadImages(response.data.id);
                    } else {
                        this.router.navigate(['/admin']);
                    }
                },
                error: (err) => {
                    this.error = 'Failed to create painting';
                    this.loading = false;
                }
            });
        }
    }

    uploadImages(paintingId: number) {
        this.paintingService.uploadImages(paintingId, this.selectedFiles).subscribe({
            next: () => {
                this.router.navigate(['/admin']);
            },
            error: (err) => {
                this.error = 'Painting saved but failed to upload images';
                this.loading = false;
            }
        });
    }
}
