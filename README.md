# Talking Canvas

An e-commerce platform for buying and selling original handmade paintings. Built for a real art business targeting Indian customers, it supports browsing, cart management, wishlist, Cash-on-Delivery ordering, and a full admin dashboard for inventory and order management.

---

## Key Features

> All features listed here are verified in the source code.

### Customer-Facing
- User registration and login with JWT authentication
- Browse all paintings with pagination, sorting, and price-range filtering
- Full-text search across title, description, artist name, and medium
- Featured paintings section (admin-curated)
- Painting detail view with multiple images and authenticity certificates
- Shopping cart management (add, update quantity, remove, clear)
- Wishlist (add, remove, clear, check membership)
- Checkout with delivery address selection (Cash on Delivery only)
- Order placement, history, and cancellation
- User profile management (name, contact, emoji avatar)
- Address book management (add, edit, delete, set default)
- Password change

### Admin Panel
- Dashboard with aggregate statistics
- Full painting CRUD (create, update, soft-delete)
- Multi-image upload and deletion per painting
- Authenticity certificate upload per painting
- Category creation and listing
- User listing, status toggle (enable/disable), soft-delete, and role promotion
- Order listing with optional status filter, and status update with optional tracking info

### Static Pages
- About, Contact (with form submission stored to DB), Privacy Policy, Terms & Conditions, Shipping Policy, Return & Refund Policy, Cookie Policy

---

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| **Backend framework** | Spring Boot | 3.5.7 |
| **Language** | Java | 21 |
| **Security** | Spring Security + JJWT | 0.12.5 |
| **ORM** | Spring Data JPA / Hibernate | (via Spring Boot) |
| **Database** | PostgreSQL | 17 (Docker) |
| **Validation** | Spring Validation (Jakarta) | (via Spring Boot) |
| **Email** | Spring Mail (SMTP/Gmail) | (via Spring Boot) |
| **Caching** | Spring Cache + Caffeine | (via Spring Boot) |
| **File upload** | Apache Commons IO | 2.18.0 |
| **API documentation** | springdoc-openapi (Swagger UI) | 2.6.0 |
| **Boilerplate reduction** | Lombok | (via Spring Boot) |
| **Frontend framework** | Angular | 20.3 |
| **UI components** | Angular Material + CDK | 19.2 |

---

## System Architecture

The application is a decoupled SPA + REST API architecture:

- The **Angular SPA** (frontend) is a standalone static build served by Nginx (Docker) or Vercel's CDN. It communicates with the backend exclusively through REST API calls.
- The **Spring Boot API** (backend) is a stateless JSON REST service. It manages all business logic, database access, file storage, email dispatch, and authentication.
- **PostgreSQL** is the single relational database used by the backend.
- **File uploads** (painting images, certificates) are stored on the local filesystem of the backend host at an `uploads/` directory. In the production profile, this resolves to `/tmp/uploads` — a non-persistent ephemeral path on Render's free tier.
- **In-memory caching** via Caffeine reduces database load for frequently read resources (paintings, categories, dashboard stats, cart, addresses).
- **JWT tokens** are issued on login and carried by the Angular HTTP interceptor on every authenticated request.

### Request Flow

```
Browser
  │
  ▼
Angular SPA (Vercel CDN / Nginx)
  │  HTTP + JWT Bearer token
  ▼
Spring Boot REST API  (:8088 local / Render cloud)
  │
  ├── Spring Security filter chain (JWT validation)
  │
  ├── Caffeine in-memory cache  ◄──┐
  │                                │ cache miss
  └── PostgreSQL database ─────────┘
        (Hibernate / JPA)
```

---

## Project Structure

```
TalkingCanvas-ws/
│
├── src/                                  # Spring Boot backend
│   └── main/
│       ├── java/com/example/talkingCanvas/
│       │   ├── TalkingCanvasApplication.java   # Entry point
│       │   ├── config/                         # Spring configuration
│       │   │   ├── SecurityConfig.java         # JWT filter chain, CORS, auth rules
│       │   │   ├── CacheConfig.java            # Caffeine cache regions
│       │   │   ├── CorsConfig.java             # CORS origin rules (env-driven)
│       │   │   ├── DataInitializer.java        # Seed: admin user, categories, sample paintings
│       │   │   ├── DatabaseConfig.java         # Render DATABASE_URL URL parser
│       │   │   ├── FileUploadConfig.java       # Multipart + static resource mapping
│       │   │   ├── OpenApiConfig.java          # Swagger / springdoc config
│       │   │   └── WebConfig.java              # Static resource handler (uploads/)
│       │   ├── controller/                     # REST Controllers
│       │   │   ├── AuthController.java         # POST /api/auth/**
│       │   │   ├── PaintingController.java     # GET /api/paintings/** (public)
│       │   │   ├── AdminController.java        # /api/admin/ users & orders
│       │   │   ├── AdminPaintingController.java# /api/admin/paintings (CRUD + uploads)
│       │   │   ├── CartController.java         # /api/cart/**
│       │   │   ├── WishlistController.java     # /api/wishlist/**
│       │   │   ├── OrderController.java        # /api/orders/**
│       │   │   ├── UserController.java         # /api/users/** (profile, addresses)
│       │   │   ├── ContactController.java      # POST /api/contact/submit
│       │   │   ├── ImageController.java        # GET /api/images/** (file serving)
│       │   │   ├── HomeController.java         # GET /
│       │   │   └── ActuatorController.java     # Health check wrapper
│       │   ├── model/                          # JPA entities
│       │   ├── dto/                            # Request/response DTOs
│       │   ├── repository/                     # Spring Data JPA repositories
│       │   ├── service/                        # Business logic
│       │   ├── security/                       # JWT filter, UserPrincipal, UserDetailsService
│       │   ├── exception/                      # Global exception handling
│       │   └── util/                           # Utility classes
│       └── resources/
│           ├── application.properties          # Default / local config
│           └── application-prod.properties     # Production config (Render)
│
├── client/                               # Angular frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.routes.ts                   # All client-side routes
│   │   │   ├── core/
│   │   │   │   ├── guards/                     # auth.guard, admin.guard
│   │   │   │   ├── interceptors/               # auth (JWT), error, cache interceptors
│   │   │   │   └── services/                   # auth, painting, cart, order, wishlist, user, admin, theme
│   │   │   ├── features/
│   │   │   │   ├── auth/                       # login, register, logout
│   │   │   │   ├── home/                       # Landing page
│   │   │   │   ├── paintings/                  # List and detail views
│   │   │   │   ├── cart/                       # Shopping cart
│   │   │   │   ├── wishlist/                   # Wishlist
│   │   │   │   ├── checkout/                   # Checkout flow
│   │   │   │   ├── order-confirmation/         # Post-order confirmation
│   │   │   │   ├── orders/                     # Order list and detail
│   │   │   │   ├── profile/                    # User profile
│   │   │   │   ├── admin/                      # Admin dashboard + painting management
│   │   │   │   └── static-pages/               # About, Contact, Privacy, Terms, etc.
│   │   │   ├── models/                         # TypeScript interfaces
│   │   │   └── shared/                         # Shared components
│   │   ├── environments/
│   │   │   ├── environment.ts                  # Dev: http://localhost:8088/api
│   │   │   └── environment.prod.ts             # Prod: https://talkingcanvas-ws.onrender.com/api
│   │   ├── index.html                          # SEO meta tags, Open Graph, JSON-LD
│   │   ├── styles.scss                         # Global styles
│   │   ├── sitemap.xml
│   │   └── robots.txt
│   ├── nginx.conf                        # Nginx config for Docker/SPA routing
│   ├── vercel.json                       # Vercel rewrites + API proxy
│   ├── Dockerfile                        # Multi-stage: Node build → Nginx serve
│   └── package.json
│
├── Dockerfile                            # Multi-stage: Maven build → JRE runtime
├── docker-compose.yml                    # PostgreSQL + Backend + Frontend
├── render.yaml                           # Render.com service + DB definition
├── Procfile                              # Fallback process file (Heroku-style)
├── system.properties                     # Java 21 / Maven 3.9.9 version pin
├── keep_alive.py                         # Utility to prevent Render free-tier sleep
├── .env.example                          # Environment variable template
└── pom.xml                               # Maven dependencies
```

---

## How It Works

### Authentication Flow

1. User submits credentials to `POST /api/auth/login`.
2. Spring Security validates credentials against the BCrypt-hashed password in the DB.
3. `JwtTokenProvider` issues a signed JWT (default expiry: 24 hours, configurable via `jwt.expiration`).
4. The Angular `AuthInterceptor` attaches the token as `Authorization: Bearer <token>` on every subsequent request.
5. `JwtAuthenticationFilter` validates the token on each request before it reaches any controller.

### Order Placement Flow

1. Customer adds paintings to cart (`POST /api/cart/add`).
2. Customer selects or creates a delivery address at checkout.
3. `POST /api/orders` creates the order, deducts stock from each painting, clears the cart, and records the order with status `PENDING`.
4. Admin updates the order status (CONFIRMED → SHIPPED → DELIVERED) with optional tracking info via `PUT /api/admin/orders/{id}/status`.
5. Customer can cancel an order while its status has not yet been set to SHIPPED or DELIVERED.

### Data Initialization

On first startup, `DataInitializer` (a `CommandLineRunner` bean) seeds the database with:
- An admin user (credentials from `admin.default.email` / `admin.default.password` properties)
- A set of painting categories (Abstract, Landscape, Portrait, etc.)
- Sample paintings with embedded Base64 images stored to the `uploads/` directory

This runs only if the admin user does not already exist.

### Caching

Caffeine in-memory caches (backend) reduce PostgreSQL queries for read-heavy resources:

| Cache name | Max entries | TTL |
|---|---|---|
| `paintings` | 500 | 600 min |
| `painting-details` | 200 | 600 min |
| `categories` | 50 | 180 min |
| `dashboard-stats` | 10 | 120 min |
| `user-addresses` | 200 | 20 min |
| `user-cart` | 200 | 10 min |

---

## API Documentation

The backend exposes interactive API documentation via Swagger UI at:

```
http://localhost:8088/api/swagger-ui.html   (local)
http://localhost:8088/api/docs              (OpenAPI JSON)
```

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new user |
| `POST` | `/api/auth/login` | Public | Login, returns JWT |

### Paintings (Public)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/paintings` | Public | Paginated list (`page`, `size`, `sortBy`, `sortDirection`) |
| `GET` | `/api/paintings/{id}` | Public | Painting detail |
| `GET` | `/api/paintings/featured` | Public | Admin-curated featured paintings |
| `GET` | `/api/paintings/search?query=` | Public | Full-text search |
| `GET` | `/api/paintings/filter/price?minPrice=&maxPrice=` | Public | Price-range filter |
| `GET` | `/api/paintings/categories` | Public | All categories |

### Cart

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/cart` | Optional | Returns empty cart for anonymous users |
| `POST` | `/api/cart/add` | Required | Add painting to cart |
| `PUT` | `/api/cart/items/{itemId}?quantity=` | Required | Update item quantity |
| `DELETE` | `/api/cart/items/{itemId}` | Required | Remove item |
| `DELETE` | `/api/cart` | Required | Clear cart |
| `GET` | `/api/cart/count` | Required | Total items count |
| `GET` | `/api/cart/count/{paintingId}` | Required | Quantity of a specific painting |

### Wishlist

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/wishlist` | Optional | Returns empty wishlist for anonymous users |
| `POST` | `/api/wishlist/add` | Required | Add painting to wishlist |
| `DELETE` | `/api/wishlist/items/{itemId}` | Required | Remove from wishlist |
| `DELETE` | `/api/wishlist` | Required | Clear wishlist |
| `GET` | `/api/wishlist/check/{paintingId}` | Optional | Check if in wishlist |
| `GET` | `/api/wishlist/count` | Optional | Wishlist item count |

### Orders

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/orders` | Required | Create order from cart |
| `GET` | `/api/orders` | Required | User's order list (`page`, `size`) |
| `GET` | `/api/orders/{orderId}` | Required | Order detail |
| `POST` | `/api/orders/{orderId}/cancel` | Required | Cancel order |

### User Profile

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/users/profile` | Required | Get profile |
| `PUT` | `/api/users/profile` | Required | Update profile |
| `POST` | `/api/users/change-password` | Required | Change password |
| `GET` | `/api/users/orders` | Required | Order history |
| `GET` | `/api/users/addresses` | Required | List addresses |
| `POST` | `/api/users/addresses` | Required | Add address |
| `PUT` | `/api/users/addresses/{id}` | Required | Update address |
| `DELETE` | `/api/users/addresses/{id}` | Required | Delete address |
| `PUT` | `/api/users/addresses/{id}/set-default` | Required | Set default address |

### Admin — Users & Orders

All `/api/admin/**` endpoints require `ADMIN` role.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/dashboard/stats` | Aggregate dashboard statistics |
| `GET` | `/api/admin/users` | Paginated user list |
| `GET` | `/api/admin/users/{userId}` | User detail |
| `PUT` | `/api/admin/users/{userId}/status` | Toggle user active/inactive |
| `DELETE` | `/api/admin/users/{userId}` | Soft-delete user |
| `PUT` | `/api/admin/users/{userId}/promote` | Grant ADMIN role to user |
| `GET` | `/api/admin/orders` | Paginated orders (`status` filter optional) |
| `PUT` | `/api/admin/orders/{orderId}/status?status=&trackingInfo=` | Update order status |

### Admin — Painting Management

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/paintings` | All paintings incl. unavailable |
| `POST` | `/api/admin/paintings` | Create painting (JSON body) |
| `PUT` | `/api/admin/paintings/{id}` | Update painting |
| `DELETE` | `/api/admin/paintings/{id}` | Soft-delete painting |
| `POST` | `/api/admin/paintings/{id}/images` | Upload images (`multipart/form-data`, field: `files`) |
| `DELETE` | `/api/admin/paintings/{id}/images/{imageId}` | Delete specific image |
| `POST` | `/api/admin/paintings/{id}/certificates` | Upload certificate (`multipart/form-data`, fields: `file`, `title`, `issuer`, `issueDate`, `description`) |
| `POST` | `/api/admin/categories?name=&description=` | Create category |

### Other

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/contact/submit` | Public | Submit contact form (stored to DB) |
| `GET` | `/api/images/{filename}` | Public | Serve uploaded file |
| `GET` | `/api/actuator/health` | Public | Health check |

---

## Database Design

Schema is managed by Hibernate `ddl-auto=update`. The following tables are created automatically:

| Table | Description |
|---|---|
| `users` | User accounts — id, name, email, password (BCrypt), contact_number, profile_emoji, role (USER/ADMIN), is_active |
| `addresses` | User delivery addresses — street, city, state, country, pincode, is_default, FK → users |
| `paintings` | Painting listings — title, description, artist_name, price (INR), currency, height/width/depth (cm), medium, year_created, is_available, stock_quantity, admin_recommendation, seo_title/description/keywords, view_count, purchase_count |
| `painting_images` | Images per painting — file_path, display_order, FK → paintings |
| `painting_certificates` | Authenticity certificates — file_path, title, issuer, issue_date, description, FK → paintings |
| `painting_categories` | Category lookup — name, description |
| `painting_category_mapping` | Many-to-many join — painting_id, category_id |
| `carts` | One cart per user — FK → users |
| `cart_items` | Line items in a cart — quantity, unit_price, FK → carts, paintings |
| `orders` | Customer orders — order_number (unique), total_amount, currency (INR), payment_method (COD), order_status (PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED), tracking_info, FK → users, addresses |
| `order_items` | Line items in an order — quantity, unit_price, FK → orders, paintings |
| `wishlists` | One wishlist per user — FK → users |
| `wishlist_items` | Items in a wishlist — FK → wishlists, paintings |
| `contact_messages` | Contact form submissions |
| `site_config` | Key/value site configuration store |

**Key relationships:**
- `User` 1-to-1 `Cart`, `User` 1-to-1 `Wishlist`
- `User` 1-to-many `Address`, `User` 1-to-many `Order`
- `Painting` many-to-many `PaintingCategory`
- `Painting` 1-to-many `PaintingImage`, `Painting` 1-to-many `PaintingCertificate`
- `Order` has a `delivery_address_id` FK to the address used at time of order

---

## Setup and Installation

### Prerequisites

| Tool | Required version |
|---|---|
| Java | 21+ |
| Maven | 3.8+ (or use `./mvnw` wrapper) |
| Node.js | 20+ |
| npm | 10+ |
| PostgreSQL | 17+ (or use Docker Compose) |

---

### Option A — Docker Compose (Recommended for local dev)

Starts PostgreSQL, the Spring Boot backend, and the Angular frontend together.

```bash
# 1. Clone the repository
git clone <repository-url>
cd TalkingCanvas-ws

# 2. Create your environment file
cp .env.example .env
# Edit .env to set DB_PASSWORD, JWT_SECRET, and optionally MAIL_* and CORS_ORIGINS

# 3. Build and start all three services
docker-compose up --build

# Run in the background
docker-compose up -d
```

Accessible at:

| Service | URL |
|---|---|
| Angular frontend | http://localhost:4200 |
| Spring Boot API | http://localhost:8088/api |
| Swagger UI | http://localhost:8088/api/swagger-ui.html |

> **Note:** The backend container maps port `8088` on the host (not `8080`). The PostgreSQL container maps to host port `5433`.

---

### Option B — Manual local setup

#### 1. Database

```bash
psql -U postgres
CREATE DATABASE talkingcanvas;
\q
```

#### 2. Backend

```bash
# From project root
./mvnw spring-boot:run
```

The application starts on port **8088** (see `server.port=${PORT:8088}` in `application.properties`).

Alternatively, build a JAR first:

```bash
./mvnw clean package -DskipTests
java -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar
```

#### 3. Frontend

```bash
cd client
npm install
npm start          # Starts ng serve on http://localhost:4200
```

The dev environment is configured to call `http://localhost:8088/api`.

---

## Environment Variables

Copy `.env.example` to `.env` and fill in the values below. The backend reads these at startup; the Docker Compose file passes them into the container.

| Variable | Description | Required |
|---|---|---|
| `DB_PASSWORD` | PostgreSQL password | Yes |
| `JWT_SECRET` | JWT signing key — use a random string of at least 64 characters. Generate with: `openssl rand -base64 64` | Yes |
| `MAIL_USERNAME` | Gmail address for outgoing email (e.g. password reset) | Optional |
| `MAIL_PASSWORD` | Gmail [App Password](https://support.google.com/accounts/answer/185833) (not your account password) | Optional |
| `CORS_ORIGINS` | Comma-separated list of allowed frontend origins | Optional |
| `SPRING_DATASOURCE_URL` | Full JDBC URL — set automatically by Docker Compose; override for external DBs | Optional |
| `SPRING_DATASOURCE_USERNAME` | DB user | Optional |
| `ADMIN_EMAIL` | Email for the auto-created admin account | Optional |
| `ADMIN_PASSWORD` | Password for the auto-created admin account | Optional |
| `ADMIN_NAME` | Display name for the admin account | Optional |

> **Security:** Never commit `.env` to version control. `.env` is already listed in `.gitignore`.


---

## Deployment

### Production architecture

| Component | Platform | Notes |
|---|---|---|
| Angular SPA | **Vercel** | Built from `client/`, output: `dist/client/browser/`, config: `client/vercel.json` |
| Spring Boot API | **Render** (free tier, Java runtime) | Config: `render.yaml` |
| PostgreSQL | **Render** managed database | Provisioned automatically via `render.yaml` |

### Vercel (Frontend)

The `client/vercel.json` configures:
- All `/api/*` requests are proxied to `https://talkingcanvas-ws.onrender.com/api/*`
- All other routes fall back to `index.html` for Angular's client-side routing

Deploy by connecting the repository to Vercel and setting the **root directory** to `client/`.

### Render (Backend)

`render.yaml` defines:
- A Java web service built with `./mvnw clean package -DskipTests` and started with `java -Xmx512m -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar`
- A managed PostgreSQL database (`talkingcanvas-db`) whose connection string is injected as `DATABASE_URL`
- `JWT_SECRET` is auto-generated by Render on first deploy
- `ADMIN_PASSWORD` and `MAIL_*` must be set manually in the Render dashboard

Health check endpoint: `GET /api/actuator/health`

### Production profile

When `spring.profiles.active=prod`, `application-prod.properties` is active:
- SQL logging is disabled
- File uploads go to `/tmp/uploads` (ephemeral — files do not survive restarts)
- CORS origins default to the Vercel deployment URL

