# TalkingCanvas — Interview Drill Guide (Engineer / Candidate POV)

**For:** Sumit Kumar (Java Full Stack + RAG/Agentic AI)
**How to use:** Keep this on one screen and VS Code on the other. Every section says **📂 Open:** with the file and method to look at. There are no code snippets on purpose. You read the real code, this guide tells you what it *means* and what to *say*.

---

## ⚠️ Read this first: scope note

The zip you uploaded (`TalkingCanvas-ws.zip`) is the **TalkingCanvas e-commerce painting store** (Spring Boot 3.5.7 + Angular 20 + PostgreSQL). It does **not** contain the **Words2Voice TTS SaaS** (FastAPI / Supertonic / semaphore / quota code). So:

- Parts 1–12 are grounded in the actual TalkingCanvas code. This is also on your resume (atomic `@Transactional` checkout, JWT, RBAC, RxJS state services), and your questions ("how is the transaction atomic", "idempotency", "scaling") map almost 1:1 onto its checkout flow.
- **Appendix A** covers Words2Voice at concept level, from your resume bullets only. I have not seen that code, so verify each point against your repo before saying it.
- If you upload the Words2Voice zip, I'll make the same file-pointing guide for it.

I read the code quickly, as you asked. I did not run it. Items I could not confirm by reading are listed in Part 13 ("Test these yourself"). Everything marked "gap" or "bug" is something I observed in the source. Confirm it in your editor before you present it as fact.

---

## Part 0. Mindset: how to answer design questions

Interviewers asking "how is it atomic / idempotent / scalable" are **not** testing whether you can recite annotations. They test whether you can:

1. **State the guarantee precisely.** "Atomic" means all-or-nothing. It does *not* mean safe under concurrency. Mixing these up is the #1 way candidates get caught.
2. **Name the failure mode.** "What breaks if two users do this at the same time?"
3. **Show trade-offs.** Every choice (JWT, in-memory cache, BLOB images) has a reason and a cost. Say both.
4. **Know your own gaps and have the fix ready.** Saying "this is a known gap; here's how I'd fix it, in this order" scores *higher* than pretending it's perfect.

### The universal answer template (use for any "how/why" question)

> **Goal → what I did → why → what it doesn't cover → how I'd improve it.**

### About "vibe-coded" (be honest, but frame it well)

If asked "did you write this yourself?", don't lie and don't apologise. Say something like:

> "I used AI tools to accelerate scaffolding. After that I went through every layer, traced the checkout flow end to end, and found its limits. For example, the checkout is atomic, but I know it isn't concurrency-safe yet, and here's how I'd fix it."

That second half is what makes you credible. The guide below gives you that depth.

---

## Part 1. Pitches (memorise these)

### 30-second pitch
> "TalkingCanvas is a full-stack e-commerce platform for selling original oil paintings. Spring Boot REST API with stateless JWT auth and role-based access, PostgreSQL for data, Angular SPA on the front. The core piece is checkout: it validates stock, creates the order with price snapshots, decrements inventory and clears the cart in a single database transaction. It's deployed with Docker, with the frontend on Vercel."

### 2-minute pitch (add these)
- **Users:** shoppers (browse, search, wishlist, cart, COD checkout, order history, cancel) and an admin (paintings CRUD with images and certificates, order status updates, dashboard stats, user management).
- **Backend layers:** Controller → Service → Repository → Entity, DTOs plus a `MapperUtil`, a global exception handler returning a uniform `ApiResponse`.
- **Performance:** Caffeine in-memory caching on the server (catalog, categories, dashboard), an HTTP GET cache interceptor in Angular, GZIP, HikariCP tuning for a free tier, 30-day cache headers on images.
- **Design decisions I can defend:** BigDecimal money, price snapshot on `OrderItem`, server-side price calculation (never trust the client), ownership checks on orders and cart items (IDOR protection), `open-in-view=false`.
- **Known gaps (say these proactively!):** no row locking or versioning on stock, no idempotency key, email sent inside the transaction, caches are per-instance, no tests, schema via `ddl-auto=update`.

### "What was the hardest part?" → see Part 11 for three candidate stories. Pick the one that is **true for you**.

---

## Part 2. Architecture & request lifecycle

```
Browser (Angular SPA, Vercel / nginx container)
   │  HTTPS, Authorization: Bearer <JWT>
   ▼
[ Spring Security filter chain ]
   JwtAuthenticationFilter → validate token → load user from DB → set SecurityContext
   authorizeHttpRequests (public / authenticated / ADMIN)
   ▼
Controller (@AuthenticationPrincipal → userId, @Valid DTO)
   ▼
Service (@Transactional, @Cacheable / @CacheEvict, business rules)
   ▼
Repository (Spring Data JPA → Hibernate → HikariCP → PostgreSQL)
   ▼
MapperUtil → DTO → ApiResponse<T> → JSON
```

**📂 Open (in this order, ~20 min):**
1. `config/SecurityConfig.java` → `securityFilterChain` (what is public vs protected)
2. `security/JwtAuthenticationFilter.java` → `doFilterInternal`
3. `controller/OrderController.java` → `createOrder`
4. `service/OrderService.java` → `createOrder` (the heart of the project)
5. `model/Order.java`, `OrderItem.java`, `Painting.java`, `Cart.java`
6. `config/CacheConfig.java`, `exception/GlobalExceptionHandler.java`
7. Angular: `app.config.ts`, `core/interceptors/*`, `core/services/auth.service.ts`, `cart.service.ts`, `features/checkout/checkout.component.ts`

### Quick facts to have in your head
| Item | Value | Where |
|---|---|---|
| Java / Spring Boot | 21 / 3.5.7 | `pom.xml` |
| Angular | 20.x, standalone components, **zoneless** change detection | `client/src/app/app.config.ts` |
| DB | PostgreSQL, `ddl-auto=update` (no Flyway/Liquibase) | `application.properties` |
| Auth | JWT (jjwt 0.12.5, HMAC), 24h expiry, subject = userId only | `JwtTokenProvider` |
| Password | BCrypt | `SecurityConfig.passwordEncoder` |
| Cache | Caffeine, per-JVM, TTLs 5–60 min | `CacheConfig` |
| Pool | HikariCP, 5 (dev props) / 10 (prod `DatabaseConfig`) | `application.properties`, `DatabaseConfig` |
| Payment | COD only (hard-coded) | `OrderService.createOrder` |
| Tests | One empty context-load test | `src/test/...` |

---

## Part 3. Code map: what each piece is for

| Area | File(s) | One-line purpose | Interview hook |
|---|---|---|---|
| Security | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider`, `CustomUserDetailsService`, `UserPrincipal`, `JwtAuthenticationEntryPoint` | Stateless JWT auth + RBAC | Why stateless? DB lookup per request? Revocation? |
| Orders | `OrderService`, `OrderController`, `Order`, `OrderItem`, `OrderRepository` | Checkout, cancel, history | Atomicity, concurrency, idempotency |
| Cart | `CartService`, `CartController`, `Cart`, `CartItem` | Per-user cart, cached | Stale cache, no stock reservation |
| Catalog | `PaintingService`, `PaintingRepository`, `Painting`, `PaintingImage`, `PaintingCategory`, `PaintingCertificate` | CRUD, search, filter, images | LIKE search, BLOB images, caching |
| Admin | `AdminService`, `AdminController`, `AdminPaintingController` | Dashboard, users, order status | No status state-machine |
| Infra | `CacheConfig`, `DatabaseConfig`, `CorsConfig`, `FileUploadConfig`, `DataInitializer` | Wiring, seeding admin/sample data | Default creds, env-based config |
| Utilities | `MapperUtil`, `EmailService`, `FileStorageService` | Entity→DTO, SMTP, disk uploads | Email in tx, ephemeral disk |
| Errors | `GlobalExceptionHandler`, `BadRequestException`, `ResourceNotFoundException` | Uniform error JSON | Missing 409 / DataIntegrity handling |
| Angular core | `core/interceptors/*`, `core/guards/*`, `core/services/*` | Auth header, caching, 401 handling, state | Interceptor order, cross-user cache |
| Angular features | `features/checkout`, `cart`, `paintings`, `admin` (lazy) | UI | Zoneless, signals vs RxJS |
| Deploy | root `Dockerfile`, `client/Dockerfile`, `client/nginx.conf`, `client/vercel.json`, `application-prod.properties` | Containers, reverse proxy, prod profile | `/api` relative URL → needs a proxy |

---

## Part 4. THE CORE: Checkout deep dive

📂 **Open:** `service/OrderService.java` → `createOrder` (starts ~line 68; ends ~line 190). Keep it open for this whole part.

### 4.1 What the method does, step by step
1. **Annotations (~68–69):** `@Transactional` (one DB transaction for the whole method) and `@CacheEvict` on `paintings` and `dashboard-stats` (all entries).
2. **Load user and cart (~73–80):** cart must exist and be non-empty, otherwise `BadRequestException("Cart is empty")` → HTTP 400.
3. **Validate stock (~83–93):** for each cart item, the painting must be available and `stockQuantity >= requested`. This is a *read*, then a *decision*. Remember that. It matters in 4.3.
4. **Save a delivery address (~96–107):** a **new** `Address` row per order.
5. **Compute total (~109–113):** `BigDecimal` sum of `price × quantity`, scale 2, `HALF_UP`. Prices come from the **database**, never from the client.
6. **Generate order number (~115, method at ~255):** `"TC"` + timestamp to milliseconds + a random 3-digit number.
7. **Build `Order` + `OrderItem`s (~118–150):** each item snapshots `priceAtPurchase`, `paintingTitle`, `artistName`.
8. **Decrement stock** in the same loop. If it hits 0 → `isAvailable=false`. Also bumps `purchaseCount`.
9. **Save + `flush()` (~152–154):** forces the SQL out *now* so constraint violations surface inside the method (e.g. the unique `order_number`).
10. **Clear cart (~157).**
11. **Send confirmation email (~160–170),** inside try/catch so an email failure doesn't roll back the order.
12. **Map to `OrderResponse`** and return.

### 4.2 "How is the transaction atomic?" (model answer)

> "`createOrder` is annotated `@Transactional`. Spring wraps the service in a proxy: on entry it opens a transaction and binds one JDBC connection to the thread; every repository call inside uses that same connection. On normal return it commits; if a `RuntimeException` escapes, it rolls back. So the address insert, order and order-item inserts, stock updates and cart clearing either all become visible together or none do. That's the 'A' in ACID — enforced by PostgreSQL's transaction, with Spring just managing begin/commit/rollback."

**Cross-questions and answers**

| Question | Answer |
|---|---|
| What exactly triggers a rollback? | By default, unchecked exceptions (`RuntimeException`, `Error`). **Checked exceptions do not roll back** unless `rollbackFor` is set. Here `BadRequestException` and `ResourceNotFoundException` extend `RuntimeException`, so they roll back. |
| What if the stock check fails on item 3 of 5? | Exception thrown before any writes happen (validation loop is first). Even if writes had happened, rollback would undo them. |
| Where does the proxy *not* work? | Self-invocation (calling a `@Transactional` method from another method in the same class), `private` methods, and non-Spring-managed objects. |
| What does `flush()` do and is it the commit? | No. Flush sends pending SQL to the DB *inside* the transaction. Commit happens when the method returns. Flush here makes constraint errors surface early and deterministically. |
| What isolation level is it? | PostgreSQL default: **READ COMMITTED**. Not configured otherwise in the code. |
| Is the email part of the atomic unit? | **No.** SMTP can't participate in a DB transaction. See 4.5. |
| What does `@CacheEvict` do relative to the commit? | It evicts after the method returns successfully, but the tx commit may come slightly after, so a concurrent reader could re-cache stale data in that gap. Cleaner: evict after commit (`@TransactionalEventListener(AFTER_COMMIT)`). *(Advanced point; mention only if asked about cache consistency.)* |
| What is the delivery-address design? | A fresh `Address` row per order, so later profile edits can't change a past order's delivery address (historical correctness). Trade-off: duplicate rows. |
| Why `OrderItem` stores price/title/artist? | **Snapshot.** If the painting's price or title changes or it's deleted, the order must still show what the customer actually bought and paid. |
| Why `BigDecimal`? | Floating point can't represent decimal money exactly. Scale 2 + `HALF_UP` makes rounding explicit. DB column is `numeric(10,2)`. |
| Why compute price on the server? | The client is untrusted. If total came from the request body, anyone could pay ₹1. |

### 4.3 ⚠️ The big one: "Atomic ≠ concurrency-safe" (the oversell race)

**What is true:** the checkout is atomic.
**What is NOT true as written:** two concurrent checkouts can both buy the last painting.

**Why (trace it):**
- `Painting` has **no `@Version`** field and the stock check uses a plain read (no `FOR UPDATE`).
- Under READ COMMITTED, two transactions A and B both read `stockQuantity = 1`, both pass the check, both write `stockQuantity = 0`. No error is thrown. Last write wins. **Two orders, one painting.** (Classic *check-then-act* / *lost update*.)
- The same pattern affects `viewCount` and `purchaseCount` increments (read-modify-write in Java).
- It's most dangerous precisely for this business: paintings are **one-of-a-kind (default stock = 1)**.

**📂 Open to see it:** `model/Painting.java` (look for the absence of a version column) and the validation loop + stock update in `OrderService.createOrder`.

**Three fixes (know all three and the trade-offs):**

| Option | How | Pros | Cons |
|---|---|---|---|
| **A. Atomic conditional UPDATE** (my recommendation) | One SQL statement per item: decrement stock only if `stock >= qty`; check rows-affected = 1, else throw "insufficient stock". Add a DB `CHECK (stock_quantity >= 0)`. | Simplest, fastest, no lock held across app logic. Postgres re-checks the WHERE clause after waiting on a row lock, so it's safe under READ COMMITTED. | Bypasses the entity (need a `@Modifying @Query` in `PaintingRepository`); must keep `isAvailable` in sync (or derive it from stock). |
| **B. Pessimistic lock** | Load paintings with `@Lock(PESSIMISTIC_WRITE)` (`SELECT … FOR UPDATE`) **ordered by id**. | Easy to reason about; serialises per painting. | Holds row locks for the whole transaction; **deadlock risk if different orders lock in different sequence** (hence order by id); lower throughput on hot rows. |
| **C. Optimistic lock** | Add `@Version` to `Painting`; on conflict Hibernate throws `OptimisticLockException`; catch and retry or return 409. | No DB locks; good when conflicts are rare. | Retry logic needed; for stock=1 items conflicts are *exactly* the interesting case; the loser gets an error. |

**Follow-ups interviewers love:**
- *"Won't pessimistic locking kill performance?"* Only on contended rows. A painting with stock 1 is only contended during a rush for that one item. Lock time is milliseconds if you move the email out of the transaction (4.5).
- *"What about deadlocks?"* Lock rows in a deterministic order (sort by painting id); Postgres detects deadlocks and aborts one transaction (SQLSTATE 40P01), so also add retry.
- *"What isolation level would fix it without code changes?"* REPEATABLE READ or SERIALIZABLE in Postgres: the loser gets a serialization failure (40001) and you must retry. Works, but you still need retry logic; the conditional UPDATE is simpler.
- *"How would you prove it's fixed?"* A concurrency test: N threads place an order for stock=1; assert exactly one success and stock = 0. Use Testcontainers + Postgres (H2 differs in locking semantics).

### 4.4 Idempotency: "How does it handle duplicate requests?"

**Honest status: there is no true idempotency.** Say it, then show you understand the pattern.

**What exists today (layers of accidental protection):**
1. **Client:** `isSubmitting` signal in `features/checkout/checkout.component.ts` (~line 43, set ~244, reset ~271/277) disables the submit while the request is in flight. Stops *double-clicks only*.
2. **Server (accidental):** after the first order commits, the cart is empty, so a *sequential* retry hits `"Cart is empty"` (400). That prevents a second order, but gives the user an error even though the first order succeeded.
3. **DB:** `order_number` is `UNIQUE` (a safety net against duplicate numbers, not duplicate orders).

**What breaks:**
- **Concurrent** duplicates (double-submit across tabs, network retry racing the original): both read the full cart before either commits → **two orders**, and stock is decremented twice (or oversold, see 4.3).
- **Lost response:** order commits, response is lost (timeout, mobile network). User retries, gets "Cart is empty", and doesn't know if the order exists.

**Definitions to say out loud:**
- *Idempotent* = repeating the same request has the same effect as doing it once. HTTP: GET, PUT, DELETE are idempotent by definition; **POST is not**. Creating an order is a POST, so you must *add* idempotency.

**How I'd implement it (concrete plan):**
1. Client generates a UUID **when the checkout page loads** (not per click) and sends it as an `Idempotency-Key` header.
2. Add a column (or small table) storing `(user_id, idempotency_key)` with a **UNIQUE constraint**, written in the **same transaction** as the order.
3. On request: look up `(user, key)`; if an order exists, **return the original response** (same status/body) instead of creating a new one.
4. Race: two identical requests insert at the same instant; the DB unique constraint makes one fail with a duplicate-key error; catch it (`DataIntegrityViolationException`), re-read, and return the existing order.
5. Extras: store a hash of the request body to reject the same key with a *different* payload (422); expire keys after ~24h.

**Cheaper complementary fix:** lock the **cart row** at the start (`SELECT … FOR UPDATE` on the user's cart). Concurrent checkouts for the same user serialise, and the second sees an empty cart. Combine with the idempotency key so the second call returns the first result instead of an error.

**Cross-questions:**
- *"Why not just disable the button?"* Client-side only; doesn't cover retries, multiple tabs, flaky networks, or API clients.
- *"Why not use the order number as the key?"* It's generated *server-side after* the request; the key must exist *before* the first attempt so retries can reuse it.
- *"Exactly-once?"* Not achievable end-to-end; you get *at-least-once delivery + idempotent processing = effectively once*.
- *"Is cancel idempotent?"* Sequentially, a second cancel returns 400 (status is already CANCELLED), so no double effect. **Concurrently**, two cancels can both see PENDING and both restore stock → stock inflated. Same fix family (lock/version on the order).

### 4.5 Email inside the transaction (a design smell you should spot first)

📂 **Open:** `OrderService.createOrder` email block (~160–170) and `util/EmailService.java` (note: each method already has its own try/catch, so the try/catch in `OrderService` is redundant).

**Problems:**
- **SMTP latency holds a DB connection.** The transaction (and its pooled connection) stays open while talking to Gmail. With a pool of 5–10, a slow SMTP can starve the pool. Back-of-envelope: if a checkout transaction takes ~50 ms with 10 connections → up to ~200 checkouts/s theoretical; if SMTP adds ~1 s inside the transaction → ~10/s. That's a 20× drop caused by one line.
- **Phantom emails:** email sent, then the commit fails → customer gets confirmation for an order that doesn't exist.
- **Lost emails:** SMTP fails → swallowed → nobody retries.

**Fix (in order of effort):**
1. Publish an `OrderPlacedEvent`; handle it with `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async` (a bounded executor). Email only after commit and off the request thread.
2. **Outbox pattern:** write an `outbox` row in the same transaction; a worker/poller sends and marks it sent (retries, survives crashes). Later, publish to Kafka (you already know Kafka).

### 4.6 Other checkout details interviewers may poke

| Topic | What the code does | What to say |
|---|---|---|
| Order number | `TC` + `yyyyMMddHHmmssSSS` + `Math.random()*1000` (3 digits), ~line 255 | Collision chance is tiny but non-zero; the unique constraint prevents duplicates but a collision would surface as a 500. Better: a DB sequence-based number or ULID. `Math.random` isn't secure, but ownership is checked on read, so guessing a number doesn't expose an order. |
| `idx_order_number` index + `unique=true` | Both exist | Redundant: the unique constraint already creates an index. (Small, but shows attention to detail.) |
| Ownership check (IDOR) | `getOrderById` and `cancelOrder` compare `order.getUser().getId()` to the caller; cart item ownership checked too | Good: prevents user A reading user B's order by changing the ID. The user ID comes from the JWT principal, never from the request. |
| Payment | Hard-coded `"COD"`; the Angular form sends `paymentMethod` but the DTO ignores it | Say "COD only today; online payments need the design in 4.8." |
| Cancel rule | Blocks DELIVERED/CANCELLED only; **SHIPPED can be cancelled**, while the controller's description says "if it hasn't been shipped" | A rule/doc mismatch: know it before they find it. |
| Cancel restores stock | Adds quantity and sets `isAvailable=true` | Fine for sequential cancel; racy concurrently (see 4.4). |
| Admin status update | `AdminService.updateOrderStatus` (~line 236): `valueOf(status)` and save | No state machine (DELIVERED→PENDING allowed), an invalid string gives a 500 via the generic handler, and admin-set CANCELLED **doesn't restore stock**. Fix: a transition table + one shared "cancel" routine. |
| Admin contact in response | `adminPhone` is wired from the property `admin.default.uncle.name` | A config naming bug (a phone field populated from a name property). Be ready to laugh and fix it. |
| Pagination | `getUserOrders` returns a `List` for a page | OK for now; return `PageResponse` with total count for the UI. |

### 4.7 Cart behaviour

📂 **Open:** `service/CartService.java` (`getCart` has `@Cacheable("user-cart")` keyed by userId; mutators have `@CacheEvict`).

- **No stock reservation.** Adding to cart only *checks* stock. Two users can hold the same one-off painting in their carts; the checkout decides who wins. That's a deliberate simplification. A reservation system (hold with TTL) is the upgrade.
- **Likely stale-cart bug:** `OrderService.createOrder` evicts `paintings` and `dashboard-stats`, but **not `user-cart`**. After checkout, `CartService.getCart` can return the cached pre-order cart for up to 5 minutes. The Angular checkout calls `cartService.getCart()` right after success (~line 265). **Test it:** place an order, then `GET /api/cart`. If stale, the fix is adding `user-cart` to the eviction list on `createOrder` (and `cancelOrder`). *(I inferred this from reading; confirm in your running app.)*
- **Add-to-cart race:** `findByCartIdAndPaintingId` then insert/update is check-then-act; two concurrent adds can create duplicates unless there's a unique `(cart_id, painting_id)` constraint. There is none in the model; add one.

### 4.8 "How would you add online payments?" (very common follow-up)

1. New order states: `PENDING_PAYMENT → PAID → CONFIRMED → SHIPPED → DELIVERED`, plus `PAYMENT_FAILED/EXPIRED` and `CANCELLED/REFUNDED`.
2. At checkout: **reserve stock** (atomic decrement or a reservation row with `expires_at`), create the order as `PENDING_PAYMENT`, call the gateway (e.g. Razorpay/Stripe) to create a payment intent/order, return its ID to the client.
3. Client completes payment with the gateway's SDK; **never trust the client's "success"**.
4. **Webhook** from the gateway → verify the **signature**, then update the order. Webhooks are delivered *at least once*, so processing must be **idempotent**: store the gateway `event_id` with a unique constraint; ignore repeats.
5. A **scheduled job** releases reservations for orders unpaid after N minutes (restores stock).
6. **Reconciliation** job compares gateway settlements to orders (handles missed webhooks).
7. Failure between "payment captured" and "order marked paid" is the classic **distributed transaction** problem; solved by idempotent webhooks + reconciliation (a saga/outbox style approach), not by a 2-phase commit.

---

## Part 5. Security: JWT, RBAC, and what an interviewer will probe

### 5.1 The flow (trace it once in the editor)
📂 **Open:** `service/AuthService.java` (`register`, `login`) → `security/JwtTokenProvider.java` → `security/JwtAuthenticationFilter.java` → `config/SecurityConfig.java` → Angular `core/services/auth.service.ts`, `core/interceptors/auth.interceptor.ts`, `error.interceptor.ts`

1. **Login:** `AuthService.login` calls Spring's `AuthenticationManager` → `DaoAuthenticationProvider` → `CustomUserDetailsService.loadUserByUsername(email)` → BCrypt compare. On success, `JwtTokenProvider.generateToken` signs a JWT (HMAC) whose **subject is only the user ID**, with a 24h expiry (`jwt.expiration=86400000`).
2. **Client:** `AuthService.setAuthData` stores the token and user object in `localStorage` (`auth_token`, `current_user`).
3. **Every request:** `authInterceptor` adds `Authorization: Bearer …`.
4. **Server:** `JwtAuthenticationFilter` validates signature and expiry, reads the user ID, **loads the user from the DB** (`loadUserById`), builds an `Authentication` with authorities and sets it in the `SecurityContext`.
5. **Authorization:** `SecurityConfig` rules: `/api/auth/**` and `GET /api/paintings/**` and images are public; `/api/admin/**` needs `ROLE_ADMIN`; cart, orders and users need authentication; everything else needs authentication.
6. **Controllers** get the caller via `@AuthenticationPrincipal UserPrincipal` and never accept a user ID from the client (prevents IDOR).

### 5.2 Q&A

| Question | Model answer |
|---|---|
| Why JWT instead of sessions? | Stateless: any instance can authenticate a request with just the secret, no sticky sessions or shared session store, so horizontal scaling is easier. Cost: hard to revoke, token carries data until expiry. |
| But you hit the DB on every request, so is it really stateless? | Fair point. The *server keeps no session state*, but my filter loads the user per request. Benefit: role changes and account deactivation (`promoteToAdmin`, `toggleUserStatus` in `AdminService`) take effect **immediately**, and no stale claims. Cost: one indexed PK lookup per authenticated request. Fix if it matters: cache the principal for ~30–60 s in Caffeine/Redis, or embed roles in the token with a short lifetime. |
| Why is CSRF disabled? | Auth is via an `Authorization` header that the browser doesn't attach automatically (unlike cookies), so classic CSRF doesn't apply. If I moved the token to a cookie I must re-enable CSRF protection / use SameSite. |
| Where is the token stored and is that safe? | `localStorage`: readable by any injected script (XSS). An HttpOnly + Secure + SameSite cookie is immune to JS theft but then needs CSRF defences. Angular's template escaping + a strict CSP reduce XSS risk. Honest answer: it's a known trade-off, and for higher-risk apps I'd use short-lived access tokens + HttpOnly refresh cookie. |
| How does logout work? | Client-only: removes the localStorage keys. The token remains valid until expiry. **No revocation.** |
| How would you add revocation? | (1) Short access token (10–15 min) + refresh token rotation stored server-side (hashed); (2) a `jti` denylist in Redis with TTL = remaining lifetime; (3) cheapest *here*: add a `tokenVersion` (or `passwordChangedAt`) to `User`, put it in the token, and compare in the filter. I'm already loading the user so it's free. |
| HS256 vs RS256? | HS256 = one shared secret signs and verifies (fine for one service). RS256/ES256 = private key signs, public key verifies, which is better with multiple services or third-party verifiers. |
| What if the token is expired? | `validateToken` returns false; the filter just continues unauthenticated; the entry point returns 401; Angular's `errorInterceptor` sees 401 → `authService.logout()` → redirect to login. |
| 401 vs 403? | 401 = not authenticated (no/invalid token). 403 = authenticated but not allowed (e.g. USER hitting `/api/admin/**`). |
| Are Angular guards security? | No. `authGuard`/`adminGuard` only check localStorage; they're UX. A user can edit localStorage to see admin screens but the API still returns 403. **Security is enforced on the server.** |
| How are passwords stored? | BCrypt (adaptive, salted, cost 10 default) via `BCryptPasswordEncoder`. Never reversible. |
| How do you prevent user enumeration? | Login returns a generic "Invalid email or password" (`BadCredentialsException` handler). But `register` says "Email already registered", which reveals existence; common trade-off for UX. |

### 5.3 Security gaps (own them)
- 📂 `application.properties`: **default admin password and default JWT secret are committed** as fallbacks. Fix: require env vars, fail fast at startup if missing, rotate any exposed secret.
- **No rate limiting / lockout on login** → brute force. Fix: Bucket4j/Redis token bucket per IP + per account, exponential delay, CAPTCHA after N failures. (You built per-IP rate limiting in Words2Voice; reuse the story!)
- **CORS** (`config/CorsConfig.java`) allows `https://*.vercel.app`: any app hosted on Vercel can call the API from a browser. Tighten to exact origins.
- **Swagger UI enabled in prod**, and prod actuator exposure is `*`. Restrict both.
- **File upload validation** (`util/FileStorageService.java`) trusts the client-supplied `Content-Type` and original extension. Fix: sniff magic bytes, whitelist extensions, re-encode images.
- **Generic exception handler** returns `ex.getMessage()` to clients (information leak) and there's no 409 handling for unique-constraint violations (e.g. two simultaneous registrations with the same email → DB unique violation → 500 instead of a clean 409).
- **Security headers** in `nginx.conf`: has X-Frame-Options etc., but no CSP or HSTS; `X-XSS-Protection` is obsolete.

---

## Part 6. Data model, JPA and PostgreSQL

📂 **Open:** `model/*.java`, `repository/PaintingRepository.java`, `repository/OrderRepository.java`, `util/MapperUtil.java`

### 6.1 Entity relationships (draw this on paper)
- `User` 1—1 `Cart`, 1—1 `Wishlist`, 1—N `Address`, 1—N `Order`
- `Order` 1—N `OrderItem` (cascade ALL + orphanRemoval), N—1 `Address` (EAGER)
- `Painting` N—M `PaintingCategory` (join table `painting_category_mapping`), 1—N `PaintingImage`, 1—N `PaintingCertificate`
- `OrderItem` and `CartItem` → N—1 `Painting` (EAGER)

### 6.2 Q&A

| Question | Answer |
|---|---|
| Why PostgreSQL (relational) for this? | Orders, inventory and payments need **ACID transactions, constraints and joins**. A document DB would make the checkout invariant (stock ≥ 0, order + items together) harder. |
| `ddl-auto=update`: is it OK? | OK for dev, **not for prod**: no history, can't roll back, never drops or renames safely, can drift between environments. Use Flyway/Liquibase. Evidence you need it: manual scripts `reset_paintings.sql` and `src/main/resources/db/fix-stock-quantity.sql`. |
| `open-in-view=false`: what and why? | Default (true) keeps the Hibernate session open through view rendering, holding a DB connection for the whole request. Setting false releases the connection when the service method ends, which is better for pool usage. Consequence: lazy associations must be loaded **inside** the transaction, or you get `LazyInitializationException`. |
| What's the N+1 problem and where could it hit here? | One query for a list plus one query per row for an association. Risk: order lists (each `OrderItem.painting` is EAGER, `Order.user` lazy but mapped), painting lists with images. Fixes: `JOIN FETCH` (see `findByIdWithImages`), `@EntityGraph`, `@BatchSize`, or DTO projections. Check it by watching the SQL log (`show-sql=true` in dev). |
| EAGER vs LAZY? | EAGER loads the association whenever the owner loads; easy but causes surprise queries. LAZY loads on first access; efficient but requires an open session. Rule: default to LAZY and fetch deliberately per use case. Here several `@ManyToOne` are EAGER, so mention it as a "would revisit". |
| Why `@Data` on entities is risky? | Lombok `@Data` generates `equals/hashCode/toString` over all fields. You excluded collections from equals/hashCode, but `toString` still walks relationships, so logging an `Order` could recurse (Order → items → order …) or trigger lazy loads. Prefer `@Getter/@Setter` + hand-written `equals/hashCode` on the ID/natural key. |
| Why DTOs and `MapperUtil`? | Don't expose entities (leaks fields like password hashes, couples API to schema, serialization loops). Manual mapper is simple but verbose; MapStruct generates it at compile time. |
| Money type? | `BigDecimal` ↔ `numeric(10,2)`; currency stored (INR). |
| Indexing: what exists, what's missing? | Present: orders by user/status/order_number, paintings by availability/price, users by email. Missing: Postgres doesn't auto-index **foreign keys**, so `order_items.order_id`, `cart_items.cart_id`, `cart_items.painting_id` etc. are unindexed. Also `idx_order_number` and `idx_user_email` duplicate their unique constraints. |
| Search is `LIKE '%q%'` with `LOWER()` on four columns. Problem? | A leading wildcard plus a function means no B-tree index → sequential scan; fine for 100s of rows, bad at 100k. Upgrade: `pg_trgm` GIN index, or Postgres full-text search (`tsvector`), or OpenSearch/Elasticsearch for typo tolerance and ranking. |
| Images in the DB? | `PaintingImage` stores bytes (`@Lob`) and `ImageController` serves them with a 30-day public cache header. **Why:** the PaaS filesystem is ephemeral (see the comment in `application-prod.properties`). **Cost:** bloats DB/backups, memory per request. **Better:** object storage (S3/Cloudinary) + CDN; DB keeps only the key/URL. Also, `@Basic(fetch=LAZY)` on a basic column is often ignored by Hibernate without bytecode enhancement. *(Verify by watching whether a painting query pulls bytes.)* |
| Certificates on disk? | `FileStorageService` writes to `file.upload-dir` (`/tmp/uploads` in prod) → lost on redeploy, and inconsistent with images being in the DB. Move to object storage. |
| `getPaintingById` writes on every GET? | Yes: it's a read-write `@Transactional` that increments `viewCount` and saves. Consequences: a write per page view, lost updates under concurrency (read-modify-write in Java), no caching of that endpoint. Fix: atomic `UPDATE … SET view_count = view_count + 1`, or count in Redis/async and flush periodically. |

---

## Part 7. Caching (two layers)

📂 **Open:** `config/CacheConfig.java`, `@Cacheable/@CacheEvict` usages in `PaintingService`, `CartService`, `AdminService.getDashboardStats`; Angular `core/interceptors/cache.interceptor.ts`, `app.config.ts`

### 7.1 Server cache (Caffeine, in-memory)
| Cache | TTL | Max | Used by |
|---|---|---|---|
| `paintings` | 15 min | 500 | list / featured / search / price filter (key includes page, size, sort, query) |
| `painting-details` | 10 min | 200 | evicted on update/delete |
| `categories` | 60 min | 50 | `getAllCategories` |
| `dashboard-stats` | 5 min | 10 | admin dashboard |
| `user-addresses` | 10 min | 200 | defined, check usage |
| `user-cart` | 5 min | 200 | `CartService.getCart` |

### 7.2 Q&A
| Question | Answer |
|---|---|
| Why cache? | Catalog browsing is read-heavy and changes rarely; caching removes repeat DB hits and cuts latency on a free-tier box. |
| Which pattern? | **Cache-aside** via Spring's abstraction: read from cache, on miss load from DB and populate; writes **evict**. |
| Why Caffeine, not Redis? | Zero infrastructure, in-process, microsecond reads. Fits a single instance on a free tier. |
| What breaks when you scale to 2+ instances? | Caches are **per JVM**. An admin edit evicts only the instance that handled it; others serve stale data until TTL (up to 15 min). Fix: Redis (`RedisCacheManager`), or short TTLs, or pub/sub invalidation. |
| Can stale cache cause overselling? | **No**, because `createOrder` reads stock from the database, not from the cache. Staleness only affects what the user *sees* (they may see "in stock" and get rejected at checkout). This is a good argument for why checkout must never read from a cache. |
| `allEntries = true`? | Blunt but safe: any painting change clears all list/search caches, because keys include query/page and we can't enumerate them. Fine at this size. |
| Cache stampede? | Many requests missing at once after expiry hit the DB together. Mitigations: Caffeine's per-key loading (`sync=true` on `@Cacheable`), TTL jitter, refresh-ahead. |
| Known bug? | `createOrder` doesn't evict `user-cart`, so the cart may look un-emptied for up to 5 min (see 4.7). |

### 7.3 Client cache interceptor (Angular)
- Module-level `Map` keyed by full URL, TTLs 5 min (paintings), 30 min (categories), 5 min (`/api/users/addresses`). Only GET + 200 + JSON.
- Cleared by `clearHttpCache()` only from painting create/update/delete calls in `core/services/painting.service.ts`.
- **Gaps:** (1) cache key doesn't include the user and **logout doesn't clear it**, so user B logging in on the same tab can be served user A's cached addresses for up to 5 minutes. (2) It's registered **first** in `withInterceptors([cache, auth, error])`, so cache hits bypass auth and error handling. (3) Layered staleness: client 5 min + server 15 min.
- **Better:** don't cache per-user endpoints client-side; use HTTP `Cache-Control` + `ETag` and let the browser/CDN do it; or clear on logout and include user ID in the key.

---

## Part 8. Angular (frontend) talking points

📂 **Open:** `app.config.ts`, `app.routes.ts`, `core/services/{auth,cart,order}.service.ts`, `features/checkout/checkout.component.ts`, `core/guards/*`

| Topic | Answer |
|---|---|
| Architecture | Angular 20 **standalone components** (no NgModules), **lazy-loaded routes** via `loadComponent`, admin area lazy via `loadChildren` (`features/admin/admin.routes.ts`). |
| Change detection | `provideZonelessChangeDetection()` in `app.config.ts`: **no Zone.js**. UI updates come from **signals**, the `async` pipe, or `markForCheck`. Note: the repo's `frontend_architecture.md` talks about `NgZone.run` as a deliberate pattern. In a zoneless app that's unnecessary. Don't repeat that claim; say "manual change-detection calls were needed where state was mutated outside signals." |
| State management | Small app: **services with `BehaviorSubject`** (`cart$`, `currentUser$`) as lightweight stores; local component state with **signals** (checkout's `isSubmitting`, addresses). NgRx would be overkill; as it grows, move to signal-based stores (NgRx Signals). |
| Signals vs RxJS | Signals: synchronous reactive *state*. RxJS: asynchronous *streams* (HTTP, events, debounce). Use both; convert with `toSignal`/`toObservable`. |
| Interceptors | Functional interceptors, order: cache → auth → error. `auth` clones the request with the Bearer token; `error` logs out on 401 (no refresh-token flow, no global 5xx/network UX). |
| Guards | `authGuard` = "has token", `adminGuard` = "has token + role from localStorage". UX only. `isLoggedIn` doesn't check expiry; the user stays "logged in" until the first 401. |
| Preventing duplicate submit | `isSubmitting` signal today. More robust RxJS: `exhaustMap` on the submit stream (ignores new clicks while one is in flight). Still needs the server idempotency key (4.4). |
| Order confirmation | `checkout.component.ts` (~266) navigates with the order in **router state**, so a browser refresh on the confirmation page likely loses it. Better: navigate to `/orders/:id` and refetch. |
| Environment/config | `environment.ts` → `http://localhost:8088/api`; `environment.prod.ts` → relative `/api`, which **requires a reverse proxy** (nginx in `client/nginx.conf`, or a rewrite in `client/vercel.json`). |
| SEO | SPA is client-rendered; there are `sitemap.xml`/`robots.txt`/meta tags and SEO fields on `Painting`. Upgrade: Angular SSR/prerender for painting pages. |
| Performance | Lazy routes, nginx gzip + 1-year immutable caching for hashed assets, `dominant-color.directive.ts` for image placeholders. |

---

## Part 9. Deployment, config and operations

📂 **Open:** root `Dockerfile`, `client/Dockerfile`, `client/nginx.conf`, `client/vercel.json`, `application.properties`, `application-prod.properties`, `config/DatabaseConfig.java`

- **Containers:** multi-stage builds (build with Node/Maven, run with slim nginx/JRE) → small images, no build tools in prod.
- **nginx:** SPA fallback (`try_files … /index.html`), gzip, immutable cache for static assets, reverse-proxy `/api/` and `/uploads/` to the backend service name.
- **Profiles:** `application.properties` (dev defaults, env overrides like `${PORT}`, `${SPRING_DATASOURCE_URL}`) vs `application-prod.properties` (+ `DatabaseConfig` active only under the `prod` profile).
- **`DatabaseConfig`:** PaaS providers hand you `postgresql://user:pass@host:port/db`, but JDBC needs `jdbc:postgresql://host:port/db`. This class parses the URL, forces `sslmode=require`, and sets Hikari to max 10. (**This is a real deploy problem you solved.** See Part 11.) Note it hard-codes pool sizes, overriding the properties file.
- **Twelve-factor config:** secrets and URLs from environment variables, not code. (Exception to fix: committed defaults for admin password and JWT secret.)
- **Health:** actuator health with probes enabled; custom `ActuatorController`. Prod exposure `*` should be narrowed.
- **Logging:** INFO in prod, SQL logging off. No metrics, tracing or correlation IDs yet.
- **No CI/CD pipeline** in the repo (only a `.github/modernize` hook folder).
- **Free-tier realities:** small Hikari pool, cold starts after idle, ephemeral disk, limited DB connections.

**Q: "How would you add observability?"** Micrometer → Prometheus → Grafana (RED metrics: rate, errors, duration; Hikari pool usage; cache hit ratio, since `recordStats()` is already on in `CacheConfig`); a servlet filter that sets a correlation ID in MDC and returns it in a response header; JSON logs; OpenTelemetry traces; alerts on p95 latency, 5xx rate, pool wait time.

---

## Part 10. Concurrency and scaling: "How does it handle multiple requests?"

### 10.1 What happens on one server today
- **Tomcat** handles each request on a worker thread (default max ~200 threads). Boot 3.5 on Java 21 can switch to **virtual threads** (`spring.threads.virtual.enabled=true`), which makes blocking I/O cheap, but...
- **HikariCP is the real limit.** Pool size 5–10 means at most 5–10 requests run DB work simultaneously; the rest wait up to `connection-timeout` (20–30 s) and then fail. With `open-in-view=false`, a connection is borrowed only inside a transaction. That's good.
- **Little's law (say this, it impresses):** concurrency ≈ throughput × time-in-system. 10 connections and a 50 ms transaction → ~200 tx/s. Make that transaction 1 s (email inside it) → ~10 tx/s. **Keep transactions short; don't do network I/O inside them.**
- **PostgreSQL** also caps total connections; many app instances × pool size can exceed it → PgBouncer.

### 10.2 Quick capacity estimate habit
1,000 concurrent users with ~10 s think time ≈ 100 requests/s. Most are catalog GETs (cache hits); maybe 1–2% reach checkout → 1–2 checkouts/s, trivial for Postgres. Bottlenecks arrive from: unindexed search, writes on GET (`viewCount`), SMTP inside the transaction, image bytes served by the app, and per-instance caches.

### 10.3 Scaling roadmap (answer in stages)
| Stage | Do | Why |
|---|---|---|
| 0. Correctness first | Stock locking/conditional update, idempotency key, cart-row lock, tests | Scaling a racy system multiplies the bugs. |
| 1. Cheap wins | Move email after commit/async; atomic view counters; FK indexes; pg_trgm search; paginate everything; virtual threads | Removes the top bottlenecks with little complexity. |
| 2. Horizontal scale | Load balancer + N stateless instances; **Redis** for caches (and rate limits); **object storage + CDN** for images/certificates; PgBouncer; cache the JWT principal | The app is already stateless (JWT), but caches, local disk and `DataInitializer` are not. |
| 3. Read scaling | CDN + `ETag`/`Cache-Control` for catalog, Postgres read replica for browse/search, dedicated search engine | Catalog is read-heavy. |
| 4. Write/hot-item scaling | Redis atomic stock counters or queue-based checkout for flash sales; **outbox + Kafka** for order events (email, analytics); separate notification worker | Only if single-row contention or throughput demands it. |
| Don't | Jump to microservices early | A well-structured **modular monolith** is easier to run; extract notifications/search first if needed. |

### 10.4 Failure questions
- *App crashes mid-checkout?* The transaction was never committed → Postgres rolls it back → **no partial order**. That's atomicity + durability (WAL) in action. The user retries (needs idempotency to be safe).
- *DB goes down?* Hikari times out → 500s (and the generic handler leaks messages). Add timeouts, a circuit breaker (Resilience4j) and friendly errors.
- *SMTP down?* Order succeeds, email is lost (no retry). Outbox fixes it.
- *Two instances start at once?* `DataInitializer`'s "exists then insert" for admin/categories can race; the unique constraint on email protects admin duplicates but one startup might fail. Fix with `ON CONFLICT DO NOTHING` or run seeding as a one-off job.

---

## Part 11. "Biggest problem and how you solved it": candidate stories

**Rule: pick the story that is true for *you*.** Interviewers cross-question; a made-up story collapses at the second "why". All of these are grounded in evidence in the repo, so you can *show* them in the code. Use the STAR shape: Situation → Task → Action → Result → What I'd do next.

### Story A: Images disappeared on deploy → moved to DB-backed storage
- **Evidence:** the comment about the ephemeral filesystem in `application-prod.properties`; `model/PaintingImage.java`; `controller/ImageController.java`.
- **Situation/Task:** On PaaS hosting the container disk is wiped on every restart/redeploy, so uploaded painting photos vanished.
- **Action:** Stored image bytes in PostgreSQL and served them via `/api/images/{id}` with a 30-day public cache header so repeat views don't hit the DB.
- **Result:** Images survive deploys without extra infrastructure.
- **Next:** Bloat/backup size and memory per request → move to S3/Cloudinary + CDN; the same issue still exists for certificates (still on disk).

### Story B: Deploying to a managed Postgres (connection URL mismatch)
- **Evidence:** `config/DatabaseConfig.java` (`@Profile("prod")`).
- **Problem:** The provider's `DATABASE_URL` isn't a valid JDBC URL; also SSL is required.
- **Action:** Parsed the URI, derived user/password/host/port/db, forced `sslmode=require`, and built a Hikari datasource sized for the free tier.
- **Next:** Spring can often do this via properties; drop the hard-coded pool sizes in favour of config.

### Story C: Keeping a cached catalog consistent
- **Evidence:** `CacheConfig`, `@CacheEvict` in `PaintingService`/`OrderService`, `clearHttpCache()` in the Angular painting service.
- **Problem:** Admin edits and orders must show up quickly, but the catalog is read-heavy.
- **Action:** Cache-aside with TTLs, evict-all on writes, and a client cache cleared on mutations. Kept **checkout reading from the DB** so stale cache can't oversell.
- **Next:** Redis for multi-instance; fix `user-cart` eviction after checkout; clear the client cache on logout.

### Story D: Designing the checkout transaction
- **Evidence:** `OrderService.createOrder`.
- **Problem:** Order, items, stock and cart must change together; an email failure must not undo a paid-for order.
- **Action:** One `@Transactional` method, early `flush()` to surface constraint errors, price snapshots on `OrderItem`, email wrapped in try/catch.
- **Next (shows maturity):** I later found it isn't concurrency-safe (no locking/version) and the email belongs after commit. Here's my fix plan (4.3, 4.5).

### Story E: Lazy-loading errors after turning off open-in-view
*(Only tell this if it actually happened to you.)* `spring.jpa.open-in-view=false` + lazy associations → `LazyInitializationException`; fixed by doing mapping inside transactional service methods and using `JOIN FETCH` queries (`findByIdWithImages`).

---

## Part 12. Gaps and upgrade roadmap (your "current problems / future upgrades" answer)

Sorted by what I'd do first. For each: **problem → fix → how I'd verify**.

| # | Sev | Problem | Where | Fix | Verify |
|---|---|---|---|---|---|
| 1 | 🔴 | Oversell race (no lock/version on stock) | `OrderService.createOrder`, `Painting` | Atomic conditional UPDATE (or `@Version` + retry) + DB `CHECK (stock >= 0)` | Concurrency test, N threads, stock=1 → exactly 1 success |
| 2 | 🔴 | No idempotency; duplicate orders on concurrent retries | `OrderController`/`OrderService` | `Idempotency-Key` header + unique `(user, key)` + cart-row lock | Fire the same request twice in parallel → one order, same response |
| 3 | 🔴 | Committed default admin password and JWT secret | `application*.properties`, `DataInitializer` | Env-only secrets, fail-fast, rotate | App fails to start without them |
| 4 | 🟠 | Email sent inside the transaction | `OrderService` | `AFTER_COMMIT` event + `@Async`; later outbox | Slow SMTP no longer slows checkout |
| 5 | 🟠 | Stale `user-cart` cache after checkout | `OrderService` eviction list | Evict `user-cart` (and on cancel) | Order → `GET /cart` is empty |
| 6 | 🟠 | No tests | `src/test` | Service tests + Testcontainers Postgres integration/concurrency tests + Angular interceptor/guard tests | CI green |
| 7 | 🟠 | `ddl-auto=update`, manual SQL scripts | `application*.properties`, `reset_paintings.sql` | Flyway migrations | Fresh DB builds from migrations only |
| 8 | 🟠 | No login rate limit / lockout | `AuthService`, filter chain | Bucket4j/Redis limiter per IP + account | 6th bad login in a minute → 429 |
| 9 | 🟠 | Client cache leaks across users; cache first in chain | `cache.interceptor.ts`, `app.config.ts` | Clear on logout, don't cache per-user URLs, use ETag | Login as A, logout, login as B → B sees own addresses |
| 10 | 🟡 | Write on every painting view | `PaintingService.getPaintingById` | Atomic increment or async counter | No UPDATE per GET in SQL log |
| 11 | 🟡 | Order status has no state machine; admin cancel doesn't restore stock; bad status → 500 | `AdminService.updateOrderStatus` | Transition table + shared cancel routine + 400 on invalid | Unit tests on transitions |
| 12 | 🟡 | Cancel allowed after SHIPPED, contradicting the API description | `OrderService.cancelOrder`, `OrderController` | Align rule and doc | Test |
| 13 | 🟡 | Images in DB; certificates on ephemeral disk | `PaintingImage`, `FileStorageService` | S3/Cloudinary + CDN, store keys | Redeploy keeps files |
| 14 | 🟡 | `LIKE '%q%'` search | `PaintingRepository.searchPaintings` | `pg_trgm` GIN or FTS; later OpenSearch | `EXPLAIN` shows index scan |
| 15 | 🟡 | Missing FK indexes; redundant indexes | models | Add indexes to FK columns; drop duplicates | `EXPLAIN` / pg_stat |
| 16 | 🟡 | Error handling: generic 500 leaks message; no 409 | `GlobalExceptionHandler` | Handle `DataIntegrityViolation`, `OptimisticLock`; hide internals | Duplicate register → 409 |
| 17 | 🟡 | Per-instance caches | `CacheConfig` | Redis | Edit on node A visible on node B |
| 18 | 🟡 | Tight CORS, Swagger/actuator exposure in prod | `CorsConfig`, prod props | Exact origins; restrict endpoints | Scan |
| 19 | 🟢 | Order number via timestamp + `Math.random` | `generateOrderNumber` | DB sequence/ULID | Unique under load |
| 20 | 🟢 | Order confirmation relies on router state | `checkout.component.ts` | Navigate by ID and refetch | Refresh page works |
| 21 | 🟢 | No observability / CI/CD | repo | Micrometer + Prometheus/Grafana, GitHub Actions | Dashboards/alerts |
| 22 | 🟢 | No online payments, refunds, returns | n/a | Design in 4.8 | n/a |

### "If I had two weeks"
Week 1: items 1, 2, 5, 3, 4 + a Testcontainers concurrency test (that test is your proof). Week 2: Flyway, rate limiting, client-cache fix, Redis cache, object storage, CI pipeline.

---

## Part 13. Test these yourself (60–90 min in VS Code)

Do these once. Seeing the behaviour is what turns knowledge into confidence. Use Swagger (`/api/swagger-ui.html`) or curl, plus the SQL log (`show-sql=true` in dev).

1. **Oversell:** make a painting with stock 1, put it in two different users' carts, fire both `POST /api/orders` at the same time (two terminals, or `&` in a shell). Does the second succeed? Check `stock_quantity` and the `orders` table. *(Expect it's possible; timing-dependent.)*
2. **Duplicate submit:** same user, same cart, two simultaneous `POST /api/orders`. One order or two?
3. **Sequential retry:** place an order, then repeat the same call. Expect "Cart is empty" (400).
4. **Stale cart:** after an order, call `GET /api/cart`. Is it still full? Wait 5 min or restart, and compare.
5. **Rollback proof:** temporarily throw an exception after the stock-decrement loop in a scratch run; confirm stock and orders are unchanged (atomicity demo). Then revert.
6. **Email failure:** use wrong SMTP credentials; order should still succeed. Time the request. How long does it block?
7. **SQL count:** watch the SQL log for one `createOrder` and for `GET /api/orders` (N+1?). Count statements.
8. **Lazy loading:** `OrderService.getOrderById/getUserOrders` aren't `@Transactional` while `open-in-view=false` and `MapperUtil.toOrderResponse` touches lazy collections. Open the Orders page. If it works, find out *why* (be able to explain); if it throws, you found a real bug.
9. **Token behaviour:** tamper with the JWT payload (→ 401); set `jwt.expiration` to ~60000 and watch `errorInterceptor` log you out; deactivate a user in admin and call the API with their still-valid token.
10. **Cross-user cache:** log in as A, open checkout (addresses load), log out, log in as B within 5 minutes without a hard refresh. Whose addresses show?
11. **Admin edge cases:** set an order status to a nonsense string (expect 500); set CANCELLED via admin and check whether stock was restored.

Write down what you observed. "I reproduced it and measured X" is the strongest sentence you can say in an interview.

---

## Part 14. Mock-interview drill ladders (practise aloud)

Each ladder = how a real interviewer keeps digging. Answer each rung in 2–3 sentences.

**Ladder 1: Atomicity → concurrency → fix**
1. Walk me through checkout. → 4.1
2. How is it atomic? → 4.2
3. What if two users buy the last painting? → 4.3 (oversell)
4. How would you fix it? → three options, pick conditional UPDATE
5. What about multi-item orders and deadlocks? → ordered locking / conditional updates are row-level and short; retry on 40P01
6. How do you test it? → Testcontainers + N threads
7. What isolation level is it running at? → READ COMMITTED

**Ladder 2: Idempotency**
1. Is checkout idempotent? → No, honest answer, explain what exists
2. What's the failure scenario? → lost response / concurrent retry
3. Design the fix. → key, unique constraint, same transaction, return original
4. What if the key is reused with a different body? → 422 + request hash
5. How long do you keep keys? → ~24 h, scheduled cleanup
6. What about payment webhooks? → event-id dedupe

**Ladder 3: Security**
1. How does auth work? → 5.1
2. Why do you query the DB per request? → immediate role/deactivation; trade-off; cache principal
3. How do you log out a stolen token? → short TTL + refresh rotation / `tokenVersion` / denylist
4. localStorage vs cookie? → XSS vs CSRF
5. Anything you'd change before production? → secrets, rate limit, CORS, actuator/Swagger

**Ladder 4: Scaling**
1. What happens at 10× traffic? → 10.1–10.3 (pool, per-instance cache, SMTP, search)
2. What breaks first? → DB pool/tx time, writes on GET, LIKE search
3. Is the app stateless? → Mostly: JWT yes; Caffeine, local disk no
4. How do you scale the database? → indexes → PgBouncer → replica → partition (later)
5. Hot product during a sale? → reservation counter in Redis / queue; DB remains source of truth

**Ladder 5: Caching**
1. What do you cache and why? → 7.1
2. Cache invalidation? → evict-on-write, TTL, `allEntries`
3. Multi-instance? → Redis
4. Can the cache cause wrong orders? → No: checkout reads the DB
5. What did you get wrong? → `user-cart` eviction; client cache across users

**Ladder 6: Design extension**
1. Add online payments. → 4.8
2. Add inventory reservation. → hold with TTL + scheduled release
3. Add search. → trigram/FTS → OpenSearch
4. Add notifications at scale. → outbox → Kafka → worker
5. Split into microservices? → not yet; modular monolith, extract notifications first

---

## Part 15. Rapid-fire (one- or two-line answers)

**Spring / JPA / Postgres**
- *`@Transactional` default propagation?* REQUIRED: join the existing transaction or start one.
- *Default rollback rule?* Unchecked exceptions and errors only.
- *`readOnly = true`?* A hint: skips dirty checking/flush in Hibernate, can route to replicas, and documents intent. It doesn't make writes impossible on its own.
- *Why does self-invocation skip the transaction?* The proxy is bypassed when `this.method()` is called.
- *Optimistic vs pessimistic?* Optimistic: detect conflicts at commit via a version column. Pessimistic: lock rows up front (`FOR UPDATE`).
- *Lost update?* Two transactions read the same value and both write back, with the last one silently overwriting.
- *Dirty read / non-repeatable / phantom?* Reading uncommitted data / same row returns different values in one tx / the same query returns new rows. Postgres never allows dirty reads.
- *Postgres default isolation?* READ COMMITTED (each statement sees data committed before it started).
- *Why a unique constraint matters?* The database is the only component that sees all concurrent writers, so it's the final arbiter of uniqueness.
- *`flush()` vs `commit()`?* Flush = send SQL; commit = make it durable/visible.
- *What's HikariCP?* A fast JDBC connection pool; reuses connections, bounds DB concurrency.
- *Why `Page` instead of `List`?* Includes total count and page metadata; limits payload size.

**REST / design**
- *Idempotent methods?* GET, PUT, DELETE, HEAD, OPTIONS. POST isn't.
- *Status codes you use?* 201 create, 400 validation/business rule, 401 unauth, 403 forbidden, 404 missing; I'd add 409 (conflict) and 429 (rate limit).
- *Why `ApiResponse<T>` wrapper?* Uniform envelope (`success`, `message`, `data`) for the frontend. Trade-off: duplicates HTTP status semantics.
- *API versioning?* Not present (`/api/...`); I'd add `/api/v1` before external consumers.
- *Validation?* Bean Validation (`@Valid`) on DTOs; business rules in services; errors mapped in `GlobalExceptionHandler`.

**Security**
- *What is IDOR and how do you prevent it?* Accessing another user's resource by changing an ID; prevented by deriving the user from the token and checking ownership (`getOrderById`, cart item checks).
- *Why BCrypt?* Slow, salted, adaptive hashing so stolen hashes are expensive to crack.
- *JWT parts?* Header.payload.signature; signature protects integrity, not confidentiality. Don't put secrets in the payload.

**Angular / RxJS**
- *Cold observable?* Executes per subscription (HTTP calls). `BehaviorSubject` is hot and replays the latest value.
- *`switchMap` vs `exhaustMap` vs `concatMap`?* Cancel previous / ignore new while busy / queue.
- *Why lazy-load routes?* Smaller initial bundle, faster first paint.
- *Interceptor order?* Request flows in the declared order, the response flows back reversed.

**General**
- *Why monolith?* One team, simple deploys, ACID transactions across order/stock/cart without distributed transaction complexity.
- *What would you not change?* Server-side pricing, price snapshots, ownership checks, DTO separation, stateless auth.

---

## Part 16. Final-day cheat sheet

**Say these sentences confidently:**
1. "Checkout is atomic via a single `@Transactional` unit; it's not yet concurrency-safe; I'd add an atomic conditional stock update plus an idempotency key."
2. "Prices are computed server-side, and each order item snapshots price and title."
3. "Auth is stateless JWT; I load the user per request so role and deactivation changes apply immediately; the trade-off is a DB hit, which I'd cache."
4. "Caches are per-instance Caffeine; checkout reads the DB so staleness can't oversell; for multiple instances I'd move to Redis."
5. "Email belongs after commit or in an outbox; right now it holds a DB connection while talking to SMTP."
6. "Atomicity is all-or-nothing; isolation and locking decide what happens when users collide."
7. "The Angular guards are UX; the server enforces security."
8. "I know where this system will break first and the order I'd fix it."

**Avoid saying:**
- ❌ "It's fully thread-safe / handles concurrency." (It doesn't yet.)
- ❌ "Stateless means no DB calls." (Stateless = no server-side session.)
- ❌ "Atomic means safe from race conditions."
- ❌ "I used NgZone deliberately." (The app is zoneless.)
- ❌ "Images in DB is the best practice." (It's a pragmatic choice; object storage is better.)
- ❌ Inventing a "hard bug" you didn't hit. Use Part 11 or Part 13 findings.

**Numbers to remember:** JWT 24 h; Hikari 5 (dev) / 10 (prod); caches 5–60 min; image cache 30 days; multipart 10 MB per file / 50 MB per request; Postgres default isolation READ COMMITTED; Tomcat ~200 threads.

---

## Appendix A: Words2Voice (TTS SaaS) at concept level

> ⚠️ **Written only from your resume. I have not seen this code.** Use it to prepare vocabulary and reasoning, then confirm every claim against your actual repo before saying it. Upload the Words2Voice zip and I'll produce a full file-pointing guide like the one above.

**Your resume claims:** Angular SPA (signals); stateless-JWT Spring Boot API; FastAPI microservice serving Supertonic-3; **semaphore-bounded concurrency**, **Spring Retry backoff**, **per-IP rate limiting**; **daily quotas via atomic PostgreSQL upserts**; deployed UI on Vercel (TTS service down).

**Questions to prepare, with the reasoning to look for in your code:**

| Topic | What to be able to explain |
|---|---|
| Why a separate FastAPI service? | Model inference is Python/ML-ecosystem work; Spring handles auth, quotas, business rules. Separate scaling and failure domains. |
| Why a semaphore? | The TTS engine is a single, compute-heavy worker. Unbounded parallel calls cause memory pressure and timeouts. A semaphore caps in-flight inference; extra requests either **wait with a timeout** or are **rejected fast** (429/503). Know which your code does and why. |
| Retry with backoff | Retries help with *transient* failures (timeouts, 503). Use **exponential backoff + jitter** to avoid synchronised retries; retry only safe/idempotent calls; cap attempts; consider a circuit breaker (Resilience4j) so a down TTS service fails fast. Danger: retries multiply load on an already-overloaded engine. |
| Per-IP rate limiting | Token bucket/fixed window per IP protects the expensive engine. Limits: in-memory limiters are per instance (use Redis for multi-instance); behind a proxy you must trust `X-Forwarded-For` carefully; shared NATs penalise innocent users; combine with per-user limits. |
| Atomic daily quota | The pitfall is read-modify-write (`SELECT count` then `UPDATE`) under concurrency. An **upsert** (`INSERT … ON CONFLICT (user, day) DO UPDATE …`) does the increment in **one statement**, so Postgres serialises concurrent increments on that row, and a conditional clause can refuse to exceed the limit. Be ready to explain *when* you consume quota (before synthesis vs after success) and how you handle a failed synthesis (refund/compensate). |
| Idempotency here | Is a repeated "generate this text" request safe? It costs compute, so consider a client-supplied request ID and caching by (text, voice) hash. |
| Latency and UX | Long text → long synthesis. Upgrade path: async job + queue, status polling/SSE/WebSocket, chunking, store audio in object storage with pre-signed URLs, stream chunks. |
| What breaks at scale | Single engine worker = hard throughput ceiling → scale out engine replicas behind a queue; autoscale on queue depth; GPU batching. |
| "TTS service down, only UI up" | Be honest: cost/hosting limits; say how you'd make it resilient (health checks, graceful degradation message, queue, alerting). |
| Signals in Angular | Same talking points as Part 8: signals for local state, RxJS for async streams. |

**Cross-project bridge:** TalkingCanvas shows *transactional correctness* (and its gaps); Words2Voice shows *protecting an expensive resource* (bounded concurrency, rate limits, atomic quotas). In an interview, connect them: "In Words2Voice I solved contention with an atomic upsert, which is the same technique I'd use for stock in TalkingCanvas."
