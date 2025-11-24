Here is a basic structure of my project 
===>>>

---

## 1. TECH STACK MATRIX
| Layer | Technology | Version | Key Libraries |
|------|------------|---------|----------------|
| Backend | Java / Spring Boot | 3.5.7 (Java 21) | Lombok, Spring Security 6, JJWT 0.12.5, Spring Data JPA, PostgreSQL |
| Frontend | Angular | 20.3 | Standalone Components, RxJS, Angular Material 19.2 |
| Build | Maven / NPM | – | Docker‑Compose support |

---

## 2. ARCHITECTURAL INVARIANTS (must‑keep)
- **API Wrapper:** Every REST endpoint returns `ApiResponse<T>`.
- **Stateless Auth:** JWT in `Authorization: Bearer <token>` header; no server‑side sessions.
- **DTO Discipline:** Controllers never expose JPA `@Entity`; always map to DTOs.
- **Image Storage:** Paintings → DB BLOB (`byte[]`), Certificates → filesystem (`uploads/certificates/`).
- **Angular Async Pattern:** All HTTP calls wrapped in `NgZone.run(() => { … }); cdr.detectChanges();` to avoid change‑detection glitches.

---

## 3. DIRECTORY MAP (key packages only)
```
backend/src/main/java/com/example/talkingCanvas/
├─ config/          # SecurityConfig, WebConfig, DataInitializer
├─ controller/      # @RestController classes (Auth, Painting, Order, Admin)
├─ dto/             # Request/Response records (auth, painting, order, etc.)
├─ exception/       # GlobalExceptionHandler, custom exceptions
├─ model/           # JPA @Entity classes (User, Painting, Order, Image, …)
├─ repository/      # JpaRepository interfaces
├─ security/        # JwtAuthenticationFilter, JwtProvider
└─ service/         # Business logic (OrderService, PaintingService, AuthService)

frontend/src/app/
├─ core/            # interceptors, singleton services, guards
├─ features/        # lazy‑loaded modules: admin, auth, cart, checkout, paintings
└─ shared/          # reusable UI components
```

---

## 4. DATA SCHEMA (compact signatures)
### Java DTOs (selected)
```java
public record UserDTO(Long id, String email, Set<String> roles) {}
public record PaintingDTO(Long id, String title, BigDecimal price, Integer stockQuantity,
                         Boolean isAvailable, List<PaintingImageDTO> images,
                         List<CategoryDTO> categories) {}
public record PaintingImageDTO(Long id, String url, Boolean isPrimary) {}
public record OrderDTO(Long id, String status, List<OrderItemDTO> items,
                       BigDecimal totalAmount, UserDTO user) {}
public record OrderItemDTO(Long id, Long paintingId, String title, BigDecimal price,
                          Integer quantity) {}
```
### TypeScript Interfaces (mirroring DTOs)
```typescript
interface User { id: number; email: string; roles: ('USER'|'ADMIN')[]; }
interface Painting { id: number; title: string; price: number; stockQuantity: number;
  isAvailable: boolean; images: PaintingImage[]; categories: Category[]; }
interface PaintingImage { id: number; url: string; isPrimary: boolean; }
interface Order { id: number; status: 'PENDING'|'CONFIRMED'|'SHIPPED'|'DELIVERED'|'CANCELLED';
  items: OrderItem[]; totalAmount: number; user: User; }
interface OrderItem { id: number; paintingId: number; title: string; price: number; quantity: number; }
```

---

## 5. API CONTRACT SUMMARY (endpoint groups)
| Group | Method | Path | Request DTO | Response DTO |
|------|--------|------|-------------|--------------|
| Auth | POST | /api/auth/login | LoginRequest | JwtResponse |
| Auth | POST | /api/auth/register | RegisterRequest | UserDTO |
| Paintings | GET | /api/paintings | – | PageResponse<PaintingDTO> |
| Paintings | POST | /api/admin/paintings | CreatePaintingRequest | PaintingDTO |
| Orders | POST | /api/orders | CreateOrderRequest | OrderDTO |
| Admin | GET | /api/admin/dashboard | – | DashboardStatsDTO |

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
```
### B. Painting Image Upload (`PaintingService.saveImages`)
```
receive MultipartFile[]
for each file:
  bytes = file.getBytes()
  create PaintingImage(entity, bytes, isPrimary flag)
associate images with Painting
save Painting (cascade images)
```

---

## 7. COMMON PITFALLS & GOTCHAS
- **Frontend:** forgetting `standalone: true` in component metadata.
- **Backend:** returning JPA entities directly → infinite recursion / lazy‑load errors.
- **Security:** new public endpoints must be added to `SecurityConfig` whitelist.
- **Transactions:** always annotate order creation with `@Transactional` to avoid partial stock updates.
- **File Paths:** certificate URLs must be URL‑encoded when stored in DB.

---
<<<===
