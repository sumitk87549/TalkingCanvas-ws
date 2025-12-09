Here is a basic structure of my project 
===>>>

---

## 1. TECH STACK MATRIX
| Layer | Technology | Version | Key Libraries |
|------|------------|---------|----------------|
| Backend | Java / Spring Boot | 3.5.7 (Java 21) | Lombok, Spring Security 6, JJWT 0.12.5, Spring Data JPA, PostgreSQL, SpringDoc OpenAPI, Caffeine Cache |
| Frontend | Angular | 20.3 | Standalone Components, RxJS, Angular Material 19.2, TailwindCSS (for styling) |
| Build | Maven / NPM | – | Docker‑Compose support |

---

## 2. ARCHITECTURAL INVARIANTS (must‑keep)
- **API Wrapper:** Every REST endpoint returns `ApiResponse<T>`.
- **Stateless Auth:** JWT in `Authorization: Bearer <token>` header; no server‑side sessions.
- **DTO Discipline:** Controllers never expose JPA `@Entity`; always map to DTOs.
- **Image Storage:** Paintings → DB BLOB (`byte[]`), Certificates → filesystem (`uploads/certificates/`).
- **Profile Customization:** Users have a `profileEmoji` and `contactNumber`.
- **Theme Management:** dedicated `ThemeService` handles Day/Night mode (defaulting to Dark).
- **Address Book:** Users can manage multiple shipping addresses (`Address` entity).
- **Caching Strategy:** Multi-layer caching (Backend Caffeine + Frontend HTTP cache) to mitigate slow DB/network.

---

## 3. DIRECTORY MAP (key packages only)
```
backend/src/main/java/com/example/talkingCanvas/
├─ config/          # SecurityConfig, WebConfig, CacheConfig, DataInitializer
├─ controller/      # @RestController classes (Auth, Painting, Order, Admin, User, Cart)
├─ dto/             # Request/Response records (auth, painting, order, user, admin)
├─ exception/       # GlobalExceptionHandler, custom exceptions
├─ model/           # JPA @Entity classes (User, Painting, Order, Image, Address, Cart, ContactMessage)
├─ repository/      # JpaRepository interfaces
├─ security/        # JwtAuthenticationFilter, JwtProvider
└─ service/         # Business logic (OrderService, PaintingService, AuthService, ThemeService, AdminService)

frontend/src/app/
├─ core/            # interceptors (auth, error, cache), services (auth, theme, cart, admin), guards
├─ features/        # lazy‑loaded modules: admin, auth, cart, checkout, paintings, profile, orders
└─ shared/          # reusable UI components
```

---

## 4. DATA SCHEMA (compact signatures)
### Java DTOs (selected)
```java
public record UserDTO(Long id, String email, String name, String profileEmoji, 
                      String contactNumber, Set<String> roles) {}
                      
public record PaintingDTO(Long id, String title, BigDecimal price, Integer stockQuantity,
                         Boolean isAvailable, List<PaintingImageDTO> images,
                         List<CategoryDTO> categories) {}
                         
public record OrderDTO(Long id, String status, List<OrderItemDTO> items,
                       BigDecimal totalAmount, UserDTO user, String trackingInfo) {}

public record AddressDTO(Long id, String street, String city, String state, 
                         String zipCode, String country, Boolean isDefault) {}
```

### TypeScript Interfaces (mirroring DTOs)
```typescript
interface User { 
  id: number; 
  email: string; 
  name: string;
  profileEmoji: string;
  contactNumber: string;
  roles: ('USER'|'ADMIN')[]; 
}

interface Painting { 
  id: number; 
  title: string; 
  price: number; 
  stockQuantity: number;
  isAvailable: boolean; 
  images: PaintingImage[]; 
  categories: Category[]; 
}

interface Order { 
  id: number; 
  status: 'PENDING'|'CONFIRMED'|'SHIPPED'|'DELIVERED'|'CANCELLED';
  items: OrderItem[]; 
  totalAmount: number; 
  user: User; 
  trackingInfo?: string;
}

interface Address {
  id: number;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  isDefault: boolean;
}
```

---

## 5. API CONTRACT SUMMARY (endpoint groups)
| Group | Method | Path | Request DTO | Response DTO |
|------|--------|------|-------------|--------------|
| Auth | POST | /api/auth/login | LoginRequest | JwtResponse |
| Auth | POST | /api/auth/register | RegisterRequest | UserDTO |
| User | GET | /api/users/profile | – | UserProfileResponse |
| User | PUT | /api/users/profile | UpdateProfileRequest | UserDTO |
| Address | POST | /api/users/addresses | AddressDTO | AddressDTO |
| Paintings | GET | /api/paintings | – | PageResponse<PaintingDTO> |
| Orders | POST | /api/orders | CreateOrderRequest | OrderDTO |
| Cart | POST | /api/cart/add | AddToCartRequest | CartDTO |
| Admin | GET | /api/admin/dashboard/stats | – | DashboardStatsResponse |
| Admin | GET | /api/admin/users | – | PageResponse<UserProfileResponse> |
| Admin | GET | /api/admin/orders | – | PageResponse<OrderResponse> |
| Admin | PUT | /api/admin/orders/{id}/status | – | OrderResponse |

---

## 6. CORE WORKFLOWS (pseudocode, token‑light)
### A. Order Creation (`OrderService.createOrder`)
```
validateUserAndCart()
for each cartItem:
  lock painting (SELECT … FOR UPDATE)
  if stockQuantity < qty -> throw InsufficientStock
  decrement stockQuantity
  if stockQuantity == 0 -> isAvailable = false
create Order + OrderItems (snapshot painting data)
clear user cart
send confirmation email
commit transaction
evict caches: 'paintings', 'dashboard-stats'
```

### B. Theme Toggling (`ThemeService` - Frontend)
```
init() -> check localStorage 'theme' -> else default 'dark'
toggle() -> switch 'dark' <-> 'light'
save to localStorage
update document.body class ('dark-mode' / 'light-mode')
```

### C. Caching Strategy (Performance Optimization)
**Backend (Caffeine In-Memory):**
- `paintings` (List pages): 15 min TTL, 500 max size.
- `painting-details`: 10 min TTL.
- `categories`: 1 hour TTL.
- `dashboard-stats`: 5 min TTL.
- *Eviction:* On painting create/update/delete and order creation/cancellation.

**Frontend (Angular Interceptor):**
- Caches `GET /api/paintings*` (5 min) and `GET /api/categories` (30 min).
- `CacheInterceptor` intercepts GETs, stores in `Map`.
- Manual clear via `clearHttpCache()` on Admin mutations.

---

## 7. COMMON PITFALLS & GOTCHAS
- **Frontend:** forgetting `standalone: true` in component metadata.
- **Backend:** returning JPA entities directly → infinite recursion / lazy‑load errors.
- **Security:** new public endpoints must be added to `SecurityConfig` whitelist.
- **Transactions:** always annotate order creation with `@Transactional`.
- **Theme:** Ensure styles use CSS variables (mostly defined in `index.scss`) for proper day/night switching.
- **Caching:** Ensure `@CacheEvict` is applied to ALL methods that modify state (create, update, delete, upload images) to prevent stale data.

---
<<<===
