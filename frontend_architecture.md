# Frontend Architecture Deep Dive

## 1. Overview
The frontend is a **Angular 20.3** application using **Standalone Components**. It interacts with the Spring Boot backend via REST APIs.

**Location:** `client/src/app`

## 2. Core Architecture

### 2.1 State Management
The application uses **RxJS BehaviorSubjects** in services for state management, rather than a heavy library like NgRx.
- **AuthService:** Holds `currentUser$` state.
- **CartService:** Holds `cart$` state.

### 2.2 Authentication Flow
1.  **Login/Register:** User submits credentials via `AuthService`.
2.  **Token Storage:** JWT token is stored in `localStorage` key `auth_token`.
3.  **Interceptor:** `auth.interceptor.ts` reads the token and attaches `Authorization: Bearer <token>` to every outgoing HTTP request.
4.  **Guards:** `AuthGuard` and `AdminGuard` protect routes based on the token presence and user role.

### 2.3 Component Strategy
- **Standalone Components:** All components are standalone (`standalone: true`), reducing `NgModule` boilerplate.
- **Change Detection:** Some components (e.g., `PaintingListComponent`) use manual `ChangeDetectorRef` and `NgZone`, suggesting a need for explicit control over rendering updates.

## 3. Key Modules (`features/`)

### 3.1 Paintings
- **List:** Fetches paginated data from `PaintingService`.
- **Detail:** Shows full details, image gallery, and "Add to Cart" functionality.
- **Images:** Handles base64/blob images or URL-based images depending on the backend response.

### 3.2 Cart & Checkout
- **Cart:** Displays items from `CartService`. Updates are optimistic or reactive based on API success.
- **Checkout:** Collects shipping address and places the order.

### 3.3 Admin
- **Lazy Loaded:** The admin module is lazy-loaded to reduce initial bundle size.
- **Dashboard:** Provides CRUD capabilities for paintings and order management.

## 4. Shared Utilities (`shared/`)
- **Components:** Reusable UI elements like `Navbar`, `Footer`, and likely some card components.
- **Models:** TypeScript interfaces mirroring the backend DTOs (`User`, `Painting`, `Order`, `Cart`).

## 5. Configuration
- **Environment:** `src/environments/environment.ts` contains the API base URL (`apiUrl`).
- **Proxy:** Likely configured (or CORS handled on backend) to allow communication between port 4200 and 8080.

## 6. Key Observations
- **Modern Angular:** Uses latest features like functional interceptors and standalone components.
- **Reactive:** Heavy use of RxJS for data flow.
- **Manual Zone Management:** Explicit `NgZone.run()` usage in some components is a specific pattern used here, possibly to ensure UI updates sync with async operations.
