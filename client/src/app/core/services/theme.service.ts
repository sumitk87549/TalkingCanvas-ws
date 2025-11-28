import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type Theme = 'day' | 'night';
export type ThemeMode = 'auto' | 'manual';

@Injectable({
    providedIn: 'root'
})
export class ThemeService implements OnDestroy {
    private readonly THEME_STORAGE_KEY = 'talkingcanvas-theme';
    private readonly MODE_STORAGE_KEY = 'talkingcanvas-theme-mode';

    private themeSubject: BehaviorSubject<Theme>;
    private themeModeSubject: BehaviorSubject<ThemeMode>;

    public currentTheme$: Observable<Theme>;
    public currentThemeMode$: Observable<ThemeMode>;

    private autoThemeCheckInterval?: number;

    constructor() {
        // Load theme mode preference
        const savedMode = this.loadModeFromStorage();
        this.themeModeSubject = new BehaviorSubject<ThemeMode>(savedMode);
        this.currentThemeMode$ = this.themeModeSubject.asObservable();

        // Load or determine theme
        let initialTheme: Theme;
        if (savedMode === 'auto') {
            initialTheme = this.getThemeBasedOnTime();
        } else {
            initialTheme = this.loadThemeFromStorage();
        }

        this.themeSubject = new BehaviorSubject<Theme>(initialTheme);
        this.currentTheme$ = this.themeSubject.asObservable();

        // Apply initial theme
        this.applyTheme(initialTheme);

        // Start auto-update timer if in auto mode
        if (savedMode === 'auto') {
            this.startAutoThemeUpdate();
        }
    }

    ngOnDestroy(): void {
        this.stopAutoThemeUpdate();
    }

    /**
     * Get the current theme value
     */
    getCurrentTheme(): Theme {
        return this.themeSubject.value;
    }

    /**
     * Get the current theme mode
     */
    getCurrentThemeMode(): ThemeMode {
        return this.themeModeSubject.value;
    }

    /**
     * Check if day mode is active
     */
    isDayMode(): boolean {
        return this.themeSubject.value === 'day';
    }

    /**
     * Check if night mode is active
     */
    isNightMode(): boolean {
        return this.themeSubject.value === 'night';
    }

    /**
     * Check if auto mode is active
     */
    isAutoMode(): boolean {
        return this.themeModeSubject.value === 'auto';
    }

    /**
     * Toggle between day and night modes (and switch to manual mode)
     */
    toggleTheme(): void {
        const newTheme: Theme = this.isDayMode() ? 'night' : 'day';
        // When user manually toggles, switch to manual mode
        this.setThemeMode('manual');
        this.setTheme(newTheme);
    }

    /**
     * Toggle between auto and manual modes
     */
    toggleThemeMode(): void {
        const newMode: ThemeMode = this.isAutoMode() ? 'manual' : 'auto';
        this.setThemeMode(newMode);
    }

    /**
     * Set a specific theme
     */
    setTheme(theme: Theme): void {
        this.themeSubject.next(theme);
        this.applyTheme(theme);
        this.saveThemeToStorage(theme);
    }

    /**
     * Set theme mode (auto or manual)
     */
    setThemeMode(mode: ThemeMode): void {
        const previousMode = this.themeModeSubject.value;
        this.themeModeSubject.next(mode);
        this.saveModeToStorage(mode);

        if (mode === 'auto') {
            // When switching to auto, immediately apply time-based theme
            const timeBasedTheme = this.getThemeBasedOnTime();
            this.setTheme(timeBasedTheme);
            this.startAutoThemeUpdate();
        } else {
            // When switching to manual, stop auto updates
            this.stopAutoThemeUpdate();
        }
    }

    /**
     * Determine theme based on current time
     * Day: 6 AM - 6 PM, Night: 6 PM - 6 AM
     */
    private getThemeBasedOnTime(): Theme {
        const hour = new Date().getHours();
        // Day time: 6 AM (6) to 6 PM (18)
        return (hour >= 6 && hour < 18) ? 'day' : 'night';
    }

    /**
     * Start automatic theme updates based on time
     */
    private startAutoThemeUpdate(): void {
        // Check every minute if theme should change
        this.autoThemeCheckInterval = window.setInterval(() => {
            if (this.isAutoMode()) {
                const timeBasedTheme = this.getThemeBasedOnTime();
                if (timeBasedTheme !== this.getCurrentTheme()) {
                    this.setTheme(timeBasedTheme);
                }
            }
        }, 60000); // Check every 60 seconds
    }

    /**
     * Stop automatic theme updates
     */
    private stopAutoThemeUpdate(): void {
        if (this.autoThemeCheckInterval) {
            window.clearInterval(this.autoThemeCheckInterval);
            this.autoThemeCheckInterval = undefined;
        }
    }

    /**
     * Apply theme by adding/removing CSS class to body
     */
    private applyTheme(theme: Theme): void {
        const body = document.body;

        if (theme === 'night') {
            body.classList.add('night-mode');
            body.classList.remove('day-mode');
        } else {
            body.classList.add('day-mode');
            body.classList.remove('night-mode');
        }
    }

    /**
     * Load theme from localStorage
     */
    private loadThemeFromStorage(): Theme {
        try {
            const savedTheme = localStorage.getItem(this.THEME_STORAGE_KEY);
            if (savedTheme === 'day' || savedTheme === 'night') {
                return savedTheme;
            }
        } catch (error) {
            console.warn('Failed to load theme from localStorage:', error);
        }
        return 'day'; // Default theme
    }

    /**
     * Save theme to localStorage
     */
    private saveThemeToStorage(theme: Theme): void {
        try {
            localStorage.setItem(this.THEME_STORAGE_KEY, theme);
        } catch (error) {
            console.warn('Failed to save theme to localStorage:', error);
        }
    }

    /**
     * Load theme mode from localStorage
     */
    private loadModeFromStorage(): ThemeMode {
        try {
            const savedMode = localStorage.getItem(this.MODE_STORAGE_KEY);
            if (savedMode === 'auto' || savedMode === 'manual') {
                return savedMode;
            }
        } catch (error) {
            console.warn('Failed to load theme mode from localStorage:', error);
        }
        return 'auto'; // Default to auto mode
    }

    /**
     * Save theme mode to localStorage
     */
    private saveModeToStorage(mode: ThemeMode): void {
        try {
            localStorage.setItem(this.MODE_STORAGE_KEY, mode);
        } catch (error) {
            console.warn('Failed to save theme mode to localStorage:', error);
        }
    }
}
