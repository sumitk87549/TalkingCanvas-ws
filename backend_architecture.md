# Backend Architecture Deep Dive

## 1. Overview
The backend is a **Spring Boot 3.5.7** application serving as a REST API for the Angular frontend. It handles authentication, product management, order processing, and admin functions.

**Location:** `src/main/java/com/example/talkingCanvas`

## 2. Core Components

### 2.1 Security (`config/SecurityConfig.java`)
- **Authentication:** Stateless JWT (JSON Web Token) based.
- **Filters:** `JwtAuthenticationFilter` intercepts requests to validate tokens.
- **Access Control:**
    - **Public:** Auth endpoints (`/api/auth/**`), Painting browsing (`GET /api/paintings/**`), Swagger docs.
    - **Admin:** `/api/admin/**` requires `ROLE_ADMIN`.
    - **User:** `/api/cart/**`, `/api/orders/**` require authentication.

### 2.2 Data Model (`model/`)
- **User:** Stores credentials and profile. Role-based (USER, ADMIN).
- **Painting:** The core product entity.
    - **Images:** Stored directly in the database as BLOBs (`byte[]`) in `PaintingImage` table.
    - **Categories:** Many-to-Many relationship.
    - **Certificates:** Stored on disk (referenced by URL) via `PaintingCertificate`.
- **Order:** Represents a purchase.
    - Linked to `User` and `Address`.
    - Contains `OrderItems` which snapshot the price/details of the painting at the time of purchase.
    - Status flow: `PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED` (or `CANCELLED`).

### 2.3 Key Services (`service/`)

#### `OrderService.java`
- **Stock Management:** Checks availability and quantity before creating an order.
- **Concurrency:** Uses `@Transactional` to ensure stock updates and order creation happen atomically.
- **Logic:**
    1. Validates User and Cart.
    2. Checks stock for each item.
    3. Creates `Order` and `OrderItems`.
    4. Decrements stock (sets `isAvailable = false` if stock hits 0).
    5. Clears the user's Cart.
    6. Sends confirmation email.

#### `PaintingService.java`
- **CRUD:** Manages lifecycle of paintings.
- **Search/Filter:** Supports filtering by price, category, and text search via `PaintingRepository` custom queries.
- **Image Handling:** Receives `MultipartFile`, converts to bytes, and stores in `PaintingImage` entity.

#### `PaintingRepository.java`
- **Custom Logic:** Contains a default method `saveWithImages(Painting p)` that ensures the parent-child relationship is correctly set on all image objects before saving, preventing JPA errors.

## 3. Storage Strategy
- **Images (Paintings):** Stored in DB (`painting_images` table, `image_data` column).
- **Certificates:** Stored on filesystem (`uploads/certificates/`), path stored in DB.
- **Static Assets:** Served from `uploads/` directory.

## 4. Initialization (`config/DataInitializer.java`)
- Runs on startup.
- Creates default Admin users if they don't exist.
- Seeds default Categories (Abstract, Landscape, etc.).
- Seeds sample Paintings with placeholder images.

## 5. API Structure
- **Base URL:** `/api`
- **Auth:** `/api/auth/login`, `/api/auth/register`
- **Paintings:** `/api/paintings` (GET public, POST/PUT/DELETE admin)
- **Orders:** `/api/orders` (User context)
- **Admin:** `/api/admin/dashboard`, `/api/admin/users`
