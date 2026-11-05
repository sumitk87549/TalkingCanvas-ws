# TalkingCanvas: Interview Mastery Guide

**For:** Sumit Kumar (Java Full Stack + RAG/Agentic AI candidate)
**Project:** TalkingCanvas, an e-commerce site for selling original oil paintings (Spring Boot 3.5 / Java 21, Angular 20, PostgreSQL, Docker; frontend on Vercel, backend on Render)
**Reading time:** 2-3 hours, with the repo open in VS Code beside this guide.

---

## 0. How to use this guide

**Honesty note.** I skimmed the code once and did not run anything. Every claim is tagged:

- ✅ = I read it directly in the code.
- 🔎 = I reasoned it out from the code but have not run it. Check it with the "2-minute experiments" in Appendix B before you state it as fact in an interview.

**Paths.** Backend paths are relative to `src/main/java/com/example/talkingCanvas/`, which I write as `BE/`. Frontend paths are relative to `client/src/app/`, written as `FE/`.

**Suggested schedule (about 2h45m):**

| Time | Part | What to do |
|---|---|---|
| 25 min | Sections 1-3 | Know your story, the architecture and the data model |
| 20 min | Section 4 | Trace the main flows in the code |
| 65 min | Sections 5-10 | The deep dives. Interviewers drill hardest here: transactions, concurrency, idempotency, security, caching, scaling |
| 20 min | Sections 11-13 | Frontend, deployment, and your own list of known issues |
| 15 min | Sections 14-15 | Your "biggest problem" stories and the upgrade roadmap |
| 30 min | Sections 16-17 | Read the Q&A bank out loud, then do the whiteboard drills |

**How interviewers will treat this project.** Your resume says "atomic @Transactional checkout" and "stateless JWT, RBAC". Those phrases are the hooks. A good interviewer will follow each one down to concurrency, idempotency and scale. The strongest position you can take is:

> "Here is what I built, here is exactly where it is solid, here is where it has known gaps, and here is how I would close them."

You can say that with confidence because section 13 gives you the gaps with the exact files.

---

## 1. Understanding you as the candidate (and how to frame this project)

### What your resume says about you
- You have 2 years of enterprise Java at a bank client (Morgan Stanley via Wissen): Spring Boot, Angular, Selenium/Cucumber, and routing across legacy and scaled database architectures.
- Since then you have self-taught the AI side: RAG, agentic AI, Transformers from scratch, and a TTS SaaS (Words2Voice) with FastAPI, semaphores, retry/backoff, rate limiting and atomic quota upserts.
- You want **Full Stack or AI Engineer** roles.

### Where TalkingCanvas fits
It is your **plain, classic full-stack proof**: auth, RBAC, transactional checkout, REST design, an Angular SPA, Dockerised, and deployed for a real person (your uncle). Words2Voice is your "I can handle concurrency and protection" project. READLYTE is your AI project. Interviewers will connect them. So expect:

> "You used atomic upserts for quotas in Words2Voice. Did you apply the same idea to stock in TalkingCanvas?"

The honest answer is "no, not yet", and section 6 shows exactly how you would do it. If you know this answer, the question helps you. If you don't, it hurts.

### How to talk about "vibe-coded"
You do not need to lead with that word. A credible framing:

> "I built it AI-assisted for speed, then I reviewed and audited it end to end. In the audit I found several real issues: stock race conditions, no idempotency, cache staleness, and some security gaps. I know the fixes and I'd implement them like this…"

The audit findings are your evidence that you own the system. Don't claim you hand-wrote things you can't explain. Everything in this guide is there so that you can explain it.

### 60-second pitch (memorise the shape, not the words)
> "TalkingCanvas is a painting e-commerce site I built for my uncle, who is an artist. Customers browse and search paintings, add them to a cart and place cash-on-delivery orders. An admin dashboard manages paintings, images, orders and shows revenue stats. The backend is Spring Boot with stateless JWT auth and role-based access, on PostgreSQL. The frontend is an Angular SPA with lazy-loaded routes and RxJS/signals state. Checkout is a single `@Transactional` operation: validate stock, snapshot prices into order items, decrement stock, create the order and clear the cart, all-or-nothing. It's deployed with the frontend on Vercel proxying `/api` to the backend on Render. Because the paintings are one-of-a-kind, stock correctness matters. I know the current read-check-write approach has a concurrency gap and I've designed the fix."

---

## 2. Architecture at a glance

```
Browser (Angular 20 SPA, served by Vercel)
   │   calls relative  /api/...
   ▼
Vercel rewrite  (client/vercel.json)  ──►  Spring Boot API on Render  (Dockerfile at repo root)
                                                │
                                    JwtAuthenticationFilter → controllers → services → repositories
                                                │
                                         PostgreSQL (DATABASE_URL, sslmode=require)
                                         ├─ paintings, categories, orders, carts, users…
                                         └─ painting images stored INSIDE the DB (bytes)
                                    + Caffeine in-memory cache (per JVM)
                                    + SMTP (Gmail) for order emails
                                    + local disk for "certificates" (ephemeral on Render)
```

✅ Why the Vercel rewrite matters: `FE/../environments/environment.prod.ts` uses a relative `apiUrl` of `/api`, and `client/vercel.json` rewrites `/api/*` to the Render URL. The browser therefore sees one origin, so there are no CORS problems in production and the frontend has no hard-coded backend URL.

✅ Layering (classic): `controller` → `service` → `repository` → `model`. Plus `dto` (request/response objects), `util/MapperUtil` (entity to DTO), `security`, `config`, and `exception/GlobalExceptionHandler`.

**Be ready to justify:**
- *Why a monolith?* One small team, one shop, low traffic. Microservices would add network, deployment and consistency problems with no benefit. (Compare: in Words2Voice the TTS model is a separate FastAPI service because it is a different runtime, GPU/CPU-heavy and single-worker. That separation has a reason.)
- *Why stateless JWT?* Horizontal scaling with no session store, and it suits an SPA and a separate frontend host.
- *Why Postgres?* Relational data (users, orders, line items), real transactions, money as `NUMERIC`.

### Repo map (open these first)
| Area | Where |
|---|---|
| Entry & config | `BE/TalkingCanvasApplication.java`, `src/main/resources/application.properties`, `application-prod.properties` |
| Security | `BE/config/SecurityConfig.java`, `BE/security/*` (JWT provider, filter, entry point, UserPrincipal) |
| Core business | `BE/service/OrderService.java`, `CartService.java`, `PaintingService.java`, `AuthService.java`, `AdminService.java` |
| Data | `BE/model/*`, `BE/repository/*` |
| Cache | `BE/config/CacheConfig.java` |
| Seeding | `BE/config/DataInitializer.java` |
| Frontend core | `FE/app.routes.ts`, `FE/app.config.ts`, `FE/core/interceptors/*`, `FE/core/guards/*`, `FE/core/services/*` |
| Checkout UI | `FE/features/checkout/checkout.component.ts` |
| Deploy | `Dockerfile`, `client/Dockerfile`, `client/nginx.conf`, `client/vercel.json` |
| Existing notes (useful but verify) | `backend_architecture.md`, `frontend_architecture.md`, `walkthrough.md` |

---

## 3. Data model: know it cold

✅ `BE/model/`. The relationships to be able to draw:

```
User 1──* Address
User 1──1 Cart 1──* CartItem *──1 Painting
User 1──* Order 1──* OrderItem *──1 Painting
Order *──1 Address (deliveryAddress)
Painting *──* PaintingCategory  (join table painting_category_mapping)
Painting 1──* PaintingImage      (bytes stored in DB)
Painting 1──* PaintingCertificate (file on disk)
User 1──1 Wishlist 1──* WishlistItem *──1 Painting
```

### Design decisions you can defend
1. **`OrderItem` snapshots `priceAtPurchase`, `paintingTitle` and `artistName`.** A later price change or edit must never alter historical orders. This is standard e-commerce practice. Say it proudly.
2. **Money is `BigDecimal` with precision 10 and scale 2 (`NUMERIC(10,2)`)**, never `double`. The total is rounded `HALF_UP` to scale 2. (Height/width/depth are `Double`, which is fine for measurements.)
3. **Cart is persisted per user** (`Cart.user` is `OneToOne`, unique), not kept in the browser. It survives devices and sessions, at the cost of a DB write on every cart change.
4. **`stockQuantity` plus `isAvailable`.** Paintings are mostly one-of-a-kind (default stock = 1). When stock hits 0 the painting is marked unavailable.
5. **Soft delete.** `PaintingService.deletePainting` just sets `isAvailable=false`. Old orders keep referencing a valid row (no FK break). But it conflates "sold out" with "deleted/hidden" (see issues, section 13).
6. **Order status enum:** `PENDING → CONFIRMED → SHIPPED → DELIVERED`, or `CANCELLED`. Stored as a string (`EnumType.STRING`), which is safer than ordinals.
7. **Indexes** (✅): on `paintings.is_available` and `price`; on `orders.user_id`, `order_status` and `order_number`; on `users.email`. Note `order_number` is already `unique`, so its extra index is redundant.
8. **Entity/DTO split.** Entities never leave the service layer; controllers return `ApiResponse<T>` wrapping DTOs. This avoids lazy-loading errors, over-exposure (e.g. the password hash) and tight coupling between API and schema.
9. **Images in the database (`PaintingImage.data`).** Very likely driven by Render's ephemeral filesystem. (✅ the comment in `application-prod.properties` says uploads "won't persist across restarts", and `PaintingImage.imageUrl` is a "legacy column retained for backward compatibility", which tells me images used to be files.) Trade-offs are in section 10.

---

## 4. Trace the main flows (do this in VS Code, ~20 min)

### 4.1 Register / Login
1. `BE/controller/AuthController.java` calls `AuthService.register` / `login`.
2. Register (✅): checks `existsByEmail`, BCrypt-encodes the password, creates User with role USER, a default Address and an empty Cart in one transaction (cascade from `User`), then issues a JWT.
3. Login (✅): `AuthenticationManager.authenticate` (DaoAuthenticationProvider + BCrypt) then `JwtTokenProvider.generateToken`. The response includes token, role, name and `expiresIn`.
4. Frontend: `FE/core/services/auth.service.ts` stores token and user in `localStorage` and pushes the user into a `BehaviorSubject`.

### 4.2 An authenticated request
`authInterceptor` adds `Authorization: Bearer …` → `JwtAuthenticationFilter.doFilterInternal` extracts the token → `JwtTokenProvider.validateToken` → `getUserIdFromJWT` → `CustomUserDetailsService.loadUserById` (**a DB query on every authenticated request**) → builds `UsernamePasswordAuthenticationToken` and sets it in `SecurityContextHolder` → `SecurityConfig` rules decide → controller receives `@AuthenticationPrincipal UserPrincipal`.

### 4.3 Browse paintings
`PaintingController` → `PaintingService.getAllPaintings` (✅ `@Cacheable`, key includes page/size/sort) → `PaintingRepository.findByIsAvailable` → `MapperUtil.toPaintingResponse` → `PageResponse<PaintingResponse>`.
Search uses a JPQL `LIKE '%q%'` on title, description, artist and medium (`PaintingRepository.searchPaintings`).
Detail: `getPaintingById` also increments `viewCount` (a write on a GET).

### 4.4 Cart
`CartService.addToCart` (✅): get or create cart, load painting, check `isAvailable` and stock, then either increase an existing `CartItem` (re-checking stock against the new total) or add a new one. `@CacheEvict("user-cart")`. `getCart` is `@Cacheable("user-cart")`.

### 4.5 Checkout (the star), `BE/service/OrderService.createOrder`
Read this method twice. In order (✅):
1. Load the user and the cart. If there is no cart or it is empty, throw `BadRequestException("Cart is empty")` (400).
2. **Validate loop:** for each item, check `isAvailable` and `stockQuantity >= quantity`. (This is a **read**.)
3. Save a **new** `Address` row from the request DTO (always a new row, even when the user picked a saved address).
4. Compute the total from current painting prices, scale 2.
5. Generate `orderNumber` = "TC" + timestamp in ms + 3 random digits (unique constraint on the column).
6. Build the `Order` (`PENDING`, currency `INR`, payment `COD` set by the server, ignoring what the client sent).
7. For each cart item: create an `OrderItem` snapshot, **decrement stock** (a read-modify-write on the entity), set unavailable at 0, increment `purchaseCount`, save the painting.
8. `orderRepository.save` then `flush()` so constraint violations surface inside the transaction.
9. `cart.clear()` (orphanRemoval deletes the cart items) and save the cart.
10. Send the confirmation email (inside the transaction; the failure is swallowed).
11. Map to `OrderResponse` including the admin contact block.
12. `@CacheEvict` on `paintings` and `dashboard-stats` (all entries).

Frontend side: `FE/features/checkout/checkout.component.ts` (multi-step UI; `forkJoin` loads cart and addresses in parallel; `isSubmitting` signal; on success navigates to `order-confirmation` passing the order in router state).

### 4.6 Admin
`/api/admin/**` requires ADMIN (`SecurityConfig`), and `AdminPaintingController` also has `@PreAuthorize("hasRole('ADMIN')")` (defence in depth, enabled by `@EnableMethodSecurity`). Admin features: painting CRUD, image and certificate upload, category creation, order list/filter, status updates with tracking info, dashboard stats (revenue, status distribution, monthly trend, popular paintings), user role promotion.

---

## 5. Deep dive: Transactions and atomicity

### What to be able to explain
**ACID in one breath:** Atomicity (all-or-nothing), Consistency (constraints always hold), Isolation (concurrent transactions don't see each other's half-done work), Durability (committed means persisted).

**What `@Transactional` does in Spring:** Spring wraps the bean in a proxy (AOP). On entry it opens a transaction (propagation `REQUIRED` by default: join the current one or start a new one), binds a JDBC connection to the thread, and on method return it commits. On a **RuntimeException or Error** it rolls back; checked exceptions do **not** roll back unless you say `rollbackFor`. `BadRequestException` and `ResourceNotFoundException` here are unchecked, so validation failures roll back cleanly.

**Pitfalls interviewers like:**
- *Self-invocation:* calling a `@Transactional` method from another method of the same class bypasses the proxy, so there is no transaction. (`PaintingService.createPaintingsWithImages` calls `this::createPainting`, but the outer method is already `@Transactional`, so it's fine.)
- *Only public methods through the proxy count.*
- *Lazy loading:* `spring.jpa.open-in-view=false` (✅ in `application.properties`), so lazy associations can only be read inside the transaction. That is why mapping to DTOs happens inside the service method.

### Why checkout is atomic (✅ → say this)
Everything in `createOrder` runs on **one transaction / one DB connection**: the new address, the order and its items, every stock update, and the cart clear. If any step throws a runtime exception (say the `order_number` unique constraint, or a DB error on `flush`), the whole thing rolls back: no half-created order, no stock leaked, cart untouched.

The `flush()` before clearing the cart matters: JPA otherwise delays SQL until commit, so a constraint violation would only appear at commit time. Flushing forces them to happen while the method is still running.

### What is *not* inside the atomic boundary
- **Email** is sent *inside* the transaction, but the SMTP call is not transactional. If the transaction later failed at commit, the customer would still have received a confirmation (a phantom order). And the email call holds a DB connection while it talks to Gmail (slow), which hurts under load. Fix in section 15: publish an event and send after commit (`@TransactionalEventListener(AFTER_COMMIT)` plus `@Async`), or use an outbox table.
- **Caches** are separate from the DB transaction. See section 9 for the stale-cart bug.

### The honest nuance to volunteer
**Atomicity ≠ isolation.** Your checkout is atomic (all-or-nothing). Whether two *concurrent* checkouts can both succeed for the last painting is an isolation question, and the answer today is "yes, possibly". That is section 6.

**Model answer to "how is your transaction atomic?":**
> "Order creation, line items, stock decrement and cart clear happen in a single `@Transactional` service method, so they commit or roll back together. Spring rolls back on unchecked exceptions, and I flush before clearing the cart so DB constraint failures happen inside the transaction. Atomicity is covered; for concurrent buyers I'd add an atomic conditional stock update because PostgreSQL's default READ COMMITTED isn't enough for read-check-write on its own."

---

## 6. Deep dive: Concurrency, overselling and isolation

### The current behaviour (✅ code, 🔎 outcome)
`createOrder` does: **read** stock → check `stock >= qty` in Java → **write** `stock - qty` as an absolute value. There is no `@Version`, no `@Lock`, and no conditional SQL (✅ I grepped; none exist anywhere in the project).

PostgreSQL's default isolation is READ COMMITTED. Scenario for the last painting (stock = 1):

| Time | Buyer A (cart X) | Buyer B (cart Y) |
|---|---|---|
| t1 | reads stock = 1, passes check | reads stock = 1, passes check |
| t2 | writes stock = 0 | writes stock = 0 (absolute value, after A commits) |
| t3 | commits: order A created | commits: order B created |

Result: **two orders for one unique painting (oversold).** This is the classic **lost update / check-then-act race**.

🔎 *Same user double-submitting* is different. Both requests share one cart; both try to delete the same cart-item rows. The second delete would likely hit a "0 rows affected" row-count mismatch in Hibernate and roll back, so that case is *accidentally* protected. But that is luck, not design, and it does nothing for two *different* buyers. Be careful how you describe this: "likely" and "by accident".

### Fix options (know all four, pick one and justify)

| Option | How | Pros | Cons |
|---|---|---|---|
| **A. Atomic conditional UPDATE** (my pick) | One SQL statement: decrement stock only where `stock >= qty`, then check rows-affected (0 = insufficient). Implemented as a `@Modifying @Query` on `PaintingRepository`. | Race-free by construction, no extra locks held in app code, simplest mental model, same technique as your Words2Voice quota upserts | Bypasses the entity cache, so refresh or clear the persistence context afterwards; availability flag must be set in the same statement |
| **B. Pessimistic lock** | `@Lock(PESSIMISTIC_WRITE)` on a `findById` variant (= `SELECT … FOR UPDATE`). Lock multiple paintings in a **consistent order** (by id) to avoid deadlocks | Straightforward; serialises buyers of the same painting | Holds row locks for the whole transaction (which includes email today); deadlock risk; reduced throughput on hot rows |
| **C. Optimistic locking** | Add `@Version` to `Painting`; a conflicting commit fails with `OptimisticLockException`; catch it and tell the loser "just sold" (or retry) | No locks; good when conflicts are rare, and for unique artworks a conflict means "two people want the same piece", so failing fast is the correct UX | Needs a retry/error strategy; wasted work on conflict |
| **D. Higher isolation** | `REPEATABLE READ`/`SERIALIZABLE` on this transaction | Postgres aborts the loser with a serialization failure | Must catch and retry; broad effect; heavier |

**Also add a DB `CHECK (stock_quantity >= 0)`** as the last line of defence. Today only the Bean Validation `@Min(0)` exists, which is an app-level check.

**My answer:** "A, atomic conditional update, plus the CHECK constraint, and a friendly 409 if rows-affected is 0. For a one-of-a-kind painting, losing the race is a normal business outcome, not an error."

### Related races in the same code (🔎 call these out; they show depth)
- **Cart add:** `CartItem` has **no unique constraint on (cart_id, painting_id)**. Two concurrent adds could insert duplicate rows, and then `findByCartIdAndPaintingId` (returns `Optional`) would throw on the next call. Fix: unique constraint plus upsert.
- **Cancel order:** `cancelOrder` restores stock with read-modify-write, and checks status in Java first. Two concurrent cancels could restore stock twice. Same fix family (guarded status transition via conditional UPDATE).
- **`incrementViewCount` / `incrementPurchaseCount`:** read-modify-write counters. Lost updates under concurrency. Use `SET view_count = view_count + 1`, or better, buffer view counts (see section 10).
- **Reservation:** the cart does *not* reserve stock. For unique art that is a deliberate simplicity choice: "in cart" does not mean "yours", and the checkout validation is the real gate. A future feature could reserve with a TTL.

### Drill questions
- *"Two people buy the last painting at the same time. What happens?"* → Describe the table above, say it can oversell today, and give option A.
- *"Why not just synchronise the method?"* → `synchronized` only works inside one JVM; with two instances, or a second Render instance during a deploy, it fails. The database is the only shared arbiter, so the fix must live in the DB.
- *"Pessimistic vs optimistic?"* → Pessimistic = lock first, wait. Optimistic = proceed, detect conflict at commit via version. Use optimistic when conflicts are rare or cheap to fail; pessimistic when conflicts are common and retries are costly.
- *"How would you test it?"* → An integration test with Testcontainers Postgres: stock 1, fire N checkouts with an `ExecutorService` and a `CountDownLatch`, assert exactly one success and final stock 0.
- *"What isolation level do you use?"* → The PostgreSQL default, READ COMMITTED. Each statement sees data committed before it started; it prevents dirty reads but not lost updates.

---

## 7. Deep dive: Idempotency

### Definitions
An operation is **idempotent** if doing it N times has the same effect as doing it once. HTTP: GET, PUT and DELETE are idempotent by definition; **POST is not**. Idempotency matters because of retries: double-clicks, a mobile network drop where the response is lost and the client retries, load-balancer or client retry policies.

### Current state in TalkingCanvas (✅ code, 🔎 outcomes)
**There is no idempotency key.** What exists is **incidental protection**:
- `isSubmitting` signal in `FE/features/checkout/checkout.component.ts` guards against double-clicks in the UI (confirm in the template that the button is `[disabled]` by it).
- After a successful order the cart is empty, so a *sequential* retry gets "Cart is empty" (400) and does not create a second order.
- `cancelOrder` is effectively idempotent: a second cancel is rejected because the status is already `CANCELLED`, so stock is not restored twice (sequentially).
- `DataInitializer` seeding is idempotent (`existsByEmail` / `existsByName` checks before inserting).

**The weakness:** the "protection" turns a successful order into a confusing error on retry. If the first response was lost (timeout), the user retries, sees "Cart is empty", and thinks the order failed. The order exists, but the client can't tell. A real idempotent API would return the **same order** again.

### How to implement it properly (design to describe)
1. The client generates a **UUID per checkout attempt** and sends it as an `Idempotency-Key` header (generate it when the checkout page loads or when the user clicks Place Order; reuse it on retry).
2. The server stores the key in a table (or a unique column on `orders`): columns like `key`, `user_id`, `request_hash`, `order_id`/response, `created_at`, with a **unique constraint on (user_id, key)**.
3. Inside the **same transaction** that creates the order, insert the key. Same transaction means "order exists" and "key exists" are atomic together.
4. If the insert hits the unique constraint, the request is a replay: return the stored order (200/201 with the same body), not a new one.
5. **Concurrent duplicates:** a second concurrent insert of the same key blocks on the unique index until the first transaction commits or rolls back, then fails and takes the "replay" path. The database's unique constraint is doing the concurrency control.
6. Same key with a **different payload** → reject (422/409). Expire keys after ~24h with a cleanup job.

This is the Stripe-style approach. Pair it with the stock fix from section 6 and checkout becomes both safe and retry-friendly.

### Drill questions
- *"How do you handle idempotency?"* → "Honestly, today by incidental guards: UI disable, the empty cart after success, and a status check on cancel. The right design is an idempotency key with a unique constraint written in the same transaction as the order; here's how it works…"
- *"Is your payment/order API safe to retry?"* → Say what happens for sequential retries (400 with 'Cart is empty', no duplicate) and concurrent ones (see section 6), then give the key-based fix.
- *"Idempotent vs safe methods?"* → Safe = no state change (GET). Idempotent = repeated calls leave the same final state (PUT, DELETE). POST is neither by default.
- *"How would payment webhooks (Razorpay) be idempotent?"* → Store processed event/payment IDs with a unique constraint; ignore duplicates; verify the HMAC signature first.

---

## 8. Deep dive: Security

### Flow (✅)
- **Passwords:** `BCryptPasswordEncoder` (adaptive, salted hash; default strength 10).
- **JWT:** HS256 signed with a secret (jjwt library). Claims: `sub` = user id, `iat`, `exp` (24h = 86,400,000 ms). **No role claim.**
- **Per request:** the filter validates the signature and expiry, loads the user **from the DB** by id, and uses their *current* role. Benefit: role changes and deactivation take effect immediately and you can't forge a role in the token. Cost: one DB query per request (mitigate with a short-lived cache keyed by user id).
- **Stateless:** `SessionCreationPolicy.STATELESS`, so there's no server session and any instance can serve any request.
- **CSRF disabled:** acceptable because auth is in a header the browser doesn't attach automatically (not a cookie). If you moved the token to a cookie you would have to re-enable CSRF protection.
- **Authorization rules:** public: `/api/auth/**`, `GET /api/paintings/**`, images, contact submit, Swagger, actuator. `/api/admin/**` needs ADMIN. Everything else needs authentication.
- **Ownership checks (IDOR defence):** `OrderService.getOrderById`/`cancelOrder` check the order belongs to the caller; `CartService` checks a cart item belongs to the caller's cart. Always derived from the token principal, never from a user id in the request.
- **Client guards** (`FE/core/guards`) are UX only; the server is the real gate.
- **Errors:** `JwtAuthenticationEntryPoint` returns 401; `GlobalExceptionHandler` maps `AccessDeniedException` to 403 and `BadCredentialsException` to a generic 401 message (avoids revealing whether the email exists).

### Weaknesses you should know (so you aren't surprised)
| # | Issue | Why it matters | Fix |
|---|---|---|---|
| 1 | **Token in `localStorage`** (`FE/core/services/auth.service.ts`) | Any XSS can steal it | HttpOnly+Secure+SameSite cookie (then re-enable CSRF), plus a strict CSP |
| 2 | **No refresh token, 24h life, logout is client-only** | A stolen token stays valid until expiry; can't revoke | Short access token (15 min) + rotating refresh token; or a denylist keyed by `jti` |
| 3 | **Default JWT secret in properties** (`jwt.secret=${JWT_SECRET:<default>}`) ✅ | If the env var is missing in prod, tokens are signed with a publicly known key, so anyone can forge an admin token | Fail startup if the secret is not provided; rotate |
| 4 | **Default admin credentials** via `DataInitializer` with a fallback password ✅ | Predictable admin account | Required env vars; force change on first login |
| 5 | **CORS allows any origin pattern with credentials** (`BE/config/CorsConfig.java`; the `cors.allowed-origins` property is read but effectively unused) ✅ | Sloppy; low risk only because auth isn't cookie-based | Restrict to the real Vercel domain(s) |
| 6 | **Actuator `exposure.include=*` in prod** (`application-prod.properties`) ✅ + non-public actuator paths only need "authenticated" 🔎 | Any logged-in customer might reach sensitive endpoints (e.g. heapdump) | Expose only `health,info`; require ADMIN |
| 7 | **No rate limiting / lockout on login** | Brute force and credential stuffing | Bucket4j/Resilience4j or edge rate limiting; (you built per-IP rate limiting in Words2Voice, so say so) |
| 8 | **Swagger public in prod** ✅ | Free reconnaissance | Disable or protect in prod |
| 9 | **`GlobalExceptionHandler` returns `ex.getMessage()` for 500s** ✅ and `server.error.include-message=always` | Leaks internals | Generic message plus a correlation id |
| 10 | **Register reveals "Email already registered"** | User enumeration (minor, common tradeoff) | Accept, or throttle |
| 11 | **File upload validation trusts `Content-Type`** (`util/FileStorageService.java`) ✅ | Spoofable | Sniff magic bytes, limit size (limit exists: 10MB), re-encode images |

### Drill questions
- *"Walk me through authentication end to end."* → Section 4.1 and 4.2 verbatim.
- *"Why isn't the role in the JWT?"* → Authoritative role comes from the DB each request, so changes and deactivation apply at once. Trade-off: one query per request, cacheable.
- *"How do you log a user out / revoke a token?"* → Today it's client-side only (a known gap). Options: short expiry plus refresh tokens, or a `jti` denylist in Redis.
- *"JWT vs sessions?"* → JWT: stateless, scales horizontally, but hard to revoke. Sessions: easy revoke, but need shared storage (Redis) when scaled. For this store either works; JWT was chosen for the decoupled SPA.
- *"Where do you store the token and what are the risks?"* → localStorage: XSS risk. Cookie: CSRF risk. State the trade-off and your upgrade path.
- *"`@PreAuthorize` vs `SecurityConfig` rules?"* → URL rules are coarse and central; method security is fine-grained and sits next to the code. Using both is defence in depth.

---

## 9. Deep dive: Caching (three layers, and its bugs)

### The three layers (✅)
1. **Browser memory cache**: `FE/core/interceptors/cache.interceptor.ts`. A module-level `Map` caches GETs for `/api/paintings` (5 min), `/api/categories` (30 min), `/api/users/addresses` (5 min). Cleared via `clearHttpCache()` after admin painting mutations (`painting.service.ts`).
2. **Server in-memory cache**: `BE/config/CacheConfig.java` using Caffeine through Spring's `SimpleCacheManager`. Named caches with sizes and TTLs: `paintings` (500 / 15 min), `painting-details` (200 / 10 min), `categories` (50 / 60 min), `dashboard-stats` (10 / 5 min), `user-addresses` (200 / 10 min), `user-cart` (200 / 5 min). Used via `@Cacheable` / `@CacheEvict` on the services.
3. **HTTP caching of images**: `BE/controller/ImageController.java` sends `Cache-Control: public, max-age=30 days`. This is safe because a re-upload creates a **new image id/URL**, so each URL is effectively immutable. (Say that: it shows you understand cache-busting.)

### Why it is there
Render's free tier and a small DB: the catalogue is read-heavy and rarely changes, so caching saves queries and DB load.

### Bugs and risks (🔎 verify with Appendix B)
1. **Stale cart after checkout.** `createOrder` evicts `paintings` and `dashboard-stats`, but **not `user-cart`**. After the order, the frontend calls `getCart()` "to refresh" (`checkout.component.ts`), which is served from the 5-minute `user-cart` cache, so the user may still see the old items in the cart or badge. Fix: also evict `user-cart` (key = userId) in `createOrder`.
2. **Browser cache survives logout/login.** `cacheInterceptor` runs *before* the auth interceptor, keys only on URL (not user), and nothing clears it on logout. In the same tab, user B can briefly get user A's cached `/api/users/addresses` response. Privacy bug. Fix: key by user or call `clearHttpCache()` in `logout()` and `login()`.
3. **Stale stock in the browser.** The browser cache isn't cleared when an order is placed, so painting lists can show an item as available for up to 5 minutes. Not a correctness problem (the server revalidates at checkout) but a UX one.
4. **Per-JVM cache.** Caffeine lives inside one instance. With 2+ instances, each has its own copy and eviction on instance A doesn't evict B. Use Redis (or accept short TTL staleness).
5. **`painting-details` cache is declared and evicted but never used** (no `@Cacheable` for it) ✅. Dead config.
6. **`@CacheEvict(allEntries=true)` is blunt.** Any order wipes the whole painting list cache. Fine at this size.
7. **Ordering nuance (advanced):** eviction might fire before the transaction commits, so a concurrent request could re-cache pre-commit data. Mitigations: `@TransactionalEventListener(AFTER_COMMIT)` for eviction, or short TTLs.
8. **Cache key built from raw user input** (search query, sortBy). Memory is bounded by `maximumSize`, but an arbitrary `sortBy` value reaches `Sort.by(...)` and can cause a 500; whitelist sort fields.

### Drill questions
- *"What do you cache, and how do you keep it consistent?"* → Layers above; TTL plus eviction on writes; known gaps (cart, multi-instance).
- *"What happens when you scale to two instances?"* → Each has its own Caffeine, so stale reads differ per instance; move to Redis or rely on short TTLs.
- *"Cache invalidation strategy?"* → Write-through eviction (`@CacheEvict`) plus TTL as a safety net. "There are two hard problems…" is a fun opener.
- *"What shouldn't be cached?"* → Per-user mutable data without a user-specific key; anything where stale = wrong money/stock (that's why checkout always reads the DB).

---

## 10. Deep dive: Performance and scaling

### Where the current design is slow (ranked by likely impact)
1. **Images stored as DB bytes.** (✅ model, 🔎 impact.) `PaintingImage.data` is a `@Lob @Basic(fetch = LAZY)` `byte[]`. Hibernate only honours lazy *basic* fields with bytecode enhancement; I didn't see it configured, so loading a painting's `images` collection likely **pulls the image bytes from the DB just to produce a URL**. A list page can then drag megabytes through the app and cache them. Also, each `/api/images/{id}` request hits the DB. Check `@Lob` mapping on Postgres (it may map to `oid`, a large object, rather than `bytea`). Fix: object storage (S3/Cloudflare R2/Cloudinary) with a CDN, storing only the URL; or at least a separate `image_data` table or projection queries that exclude bytes.
2. **N+1 queries.** `MapperUtil.toPaintingResponse` touches `images`, `certificates`, and `categories` (all lazy) for every painting, so a page of N paintings costs about 3N+1 queries. Fix: a lighter list DTO (primary image only), `hibernate.default_batch_fetch_size`, or `@EntityGraph`/two-step fetch (join-fetching collections with pagination triggers in-memory paging, a known Hibernate trap).
3. **JWT filter hits the DB on every authenticated request.** Cache the principal for ~1 min or put the essentials in the token.
4. **Email inside the transaction** holds a connection during SMTP. With a pool of 5-10 connections, a few slow emails can exhaust it. Fix: after-commit async.
5. **`getPaintingById` is `@Transactional` and writes** (`viewCount++`) on every view, including bots. It also invalidates nothing but creates row contention. Fix: increment atomically in SQL, or buffer counts in memory/Redis and flush periodically.
6. **Search** uses `LOWER(col) LIKE '%q%'` across four columns including `TEXT`; a B-tree index can't help, so it is a full scan. Fine for hundreds of rows. Next step: Postgres `pg_trgm` GIN index or full-text search (`tsvector`), then Elasticsearch/OpenSearch only if the catalogue becomes large.
7. **Connection pool:** `spring.datasource.hikari.maximum-pool-size` defaults to 5 in `application.properties`, but `DatabaseConfig` (prod profile) builds its own `HikariDataSource` with max 10 (🔎 likely overrides the properties). Know the formula: **instances × pool size ≤ DB max connections**, then consider PgBouncer.
8. **Render free-tier cold starts.** The first request after idle is slow. Mitigate with a keep-alive ping to `/api/actuator/health` or a paid plan.

### Scaling plan (say it in stages, and say "I'd measure first")
- **Stage 0, today:** one instance, one DB, tiny traffic.
- **Stage 1, correctness and cheap wins:** fix the stock race and idempotency, fix N+1 and image loading, async email, indexes, Flyway.
- **Stage 2, horizontal scale:** the app is mostly stateless (JWT, no sessions). Things that break statelessness: Caffeine (→ Redis), certificates on local disk (→ object storage), and in-memory rate limits. Then run N instances behind a load balancer.
- **Stage 3, read scale:** CDN for images and the catalogue API, DB read replicas for browsing, a cache for hot reads.
- **Stage 4, decouple:** a queue for emails/notifications/WhatsApp; payments with webhooks; a search engine if needed.

### Maturity point to say out loud
> "For a single-artist store the real risk isn't scale; it's correctness (overselling, duplicate orders) and operations (backups, migrations, secrets). A single Postgres plus a CDN would carry 10-100× today's traffic. I'd fix correctness first and scale only when metrics justify it."

### Drill questions
- *"How would you handle 1,000 concurrent users?"* → Stages above; identify bottlenecks first (images, N+1, pool), measure with a load test (k6/JMeter), then scale horizontally.
- *"What breaks first?"* → Image bytes through the DB/app, then the connection pool; not CPU.
- *"Multiple requests hitting the same painting?"* → Section 6.
- *"What does 'stateless' buy you and where are you not stateless?"* → Caches, local-disk uploads.

---

## 11. Frontend deep dive (Angular 20)

### Facts (✅)
- **Standalone components, no NgModules.** Every route uses `loadComponent` (lazy loading) and admin uses `loadChildren` (`FE/app.routes.ts`). Smaller initial bundle.
- **Zoneless change detection** (`provideZonelessChangeDetection()` in `FE/app.config.ts`) and `ChangeDetectionStrategy.OnPush`, with **signals** (`signal`, `computed`) in newer components such as checkout.
- **State management is hybrid:** services with `BehaviorSubject` (`AuthService.currentUser$`, `CartService.cart$`) for shared state, and signals for local component state. That matches your resume: "RxJS state services".
- **Functional interceptors, registered in this order:** `cacheInterceptor` → `authInterceptor` → `errorInterceptor` (`provideHttpClient(withInterceptors([...]))`). The error interceptor logs the user out on any 401.
- **Functional guards:** `authGuard` (token present) and `adminGuard` (logged in and role ADMIN). They check token *presence*, not expiry; an expired token passes the guard and is rejected by the server with a 401, which triggers logout.
- **Checkout UX:** step indicator, `forkJoin` to load cart and addresses in parallel, reactive form validation (phone pattern, required address fields), error signal, `isSubmitting`.
- **SEO hints:** `sitemap.xml`, `robots.txt`, SEO fields on the Painting entity. But it is a client-rendered SPA with no SSR, so crawlers see little. An upgrade path is Angular SSR/prerendering for painting pages.

### Frontend weak spots (🔎)
- After placing the order the confirmation page gets the order via **router navigation state**, which is lost on refresh. Better: navigate to `/orders/:id` and fetch.
- `notes` carries the phone number as the text "Contact: …" (`checkout.component.ts`). It should be a structured field.
- The `paymentMethod` the form sends is ignored by the server, which hard-codes `COD` (fine, but misleading).
- Lots of `console.log` in services (including logging the whole register request, which includes the password!, in `AuthService.register`). Remove before production. A good "I'd catch this in code review" item.

### Drill questions
- *"Signals vs RxJS?"* → Signals: synchronous local/derived state, simple, great with zoneless and OnPush. RxJS: async streams, HTTP, cancellation, debounce, combination. Use both where each fits.
- *"What is zoneless?"* → Angular doesn't patch async APIs via zone.js; change detection is triggered by signals and events, so less overhead.
- *"How do route guards protect admin?"* → They don't, really; they improve UX. The server's `hasRole('ADMIN')` is the real protection.
- *"Interceptor order matters. Why?"* → Request flows in listed order, response in reverse. Cache first means a hit short-circuits everything else (including auth), which is why per-user data must not be cached there without a user-specific key.

---

## 12. Deployment, config and operations

- ✅ **Backend `Dockerfile`:** multi-stage (Maven + JDK 21 build, JRE 21 Alpine runtime), so a small image with no build tools.
- ✅ **`client/Dockerfile`:** Node build, then Nginx serves static files (`client/nginx.conf` for SPA fallback). Used for local full-stack Docker; production uses Vercel.
- ✅ **Config via environment variables** with defaults: `SPRING_DATASOURCE_*`, `DATABASE_URL` (parsed by `BE/config/DatabaseConfig.java` for the `prod` profile: converts `postgresql://` to JDBC, adds `sslmode=require`), `JWT_SECRET`, `MAIL_*`, `ADMIN_*`, `CORS_ALLOWED_ORIGINS`. (12-factor style.)
- ⚠ **`spring.jpa.hibernate.ddl-auto=update` in prod.** Convenient, but unsafe: no versioned migrations, no rollback, can't reliably alter existing columns. The repo contains manual fix scripts (`src/main/resources/db/fix-stock-quantity.sql`, `reset_paintings.sql`), which is evidence of exactly this pain. Upgrade: **Flyway** (versioned SQL migrations) and `ddl-auto=validate`.
- ✅ **Prod logging** is moderate; `show-sql` is on in dev and off in prod.
- ✅ **GZIP compression** enabled for JSON and text.
- ⚠ **Health endpoint:** a custom `ActuatorController` at `/api/actuator/health` (plus Boot's own actuator). Good for a keep-alive and a platform health check. See the actuator exposure issue above.
- ⚠ **Timestamps** are `LocalDateTime` (no zone), so values depend on the server's timezone (UTC on Render) while the customer is in India. Better: `Instant`/`OffsetDateTime` stored as UTC, formatted in the UI.
- ⚠ **No CI/CD pipeline, near-zero tests** (only the default context-load test and Angular boilerplate specs). Say what you would test: the concurrent checkout (Testcontainers), security rules with MockMvc, mapper/total calculation, and the cancel flow.
- ⚠ **Observability:** only logs. Add request/correlation ids in MDC, Micrometer plus Prometheus/Grafana (Caffeine already `recordStats()`), and error tracking (Sentry).
- ⚠ **Backups:** Render free Postgres has limited retention. Since images live in the DB, backup/restore matters even more.

---

## 13. Known-issues register (your honest audit; ranked)

Use this as your "current problems and how I'd fix them" answer. Pick the top 3-4 depending on the question.

| Sev | Issue | Where | Fix |
|---|---|---|---|
| **High** | Overselling: read-check-write on stock, no lock/version/conditional update | `OrderService.createOrder` | Atomic conditional UPDATE plus CHECK constraint (section 6) |
| **High** | No idempotency key on order creation | `OrderController` / `OrderService` | Idempotency-Key plus unique constraint (section 7) |
| **High** | Default JWT secret / default admin password fallbacks | `application*.properties`, `DataInitializer` | Required env vars, fail fast |
| **High** | Actuator exposure `*` in prod; any authenticated user may reach non-public actuator paths 🔎 | `application-prod.properties`, `SecurityConfig` | Restrict to health/info; ADMIN only |
| **Med** | Browser cache not user-scoped or cleared on logout (address leakage across users in one tab) 🔎 | `FE/core/interceptors/cache.interceptor.ts` | Per-user key / clear on auth change |
| **Med** | `user-cart` cache not evicted on order → stale cart 🔎 | `OrderService.createOrder` | Add `user-cart` eviction |
| **Med** | Image bytes loaded via lazy collections, N+1 on lists 🔎 | `PaintingImage`, `MapperUtil`, `PaintingService` | Object storage + CDN, list DTO, batch fetch |
| **Med** | Email sent inside the transaction | `OrderService.createOrder` | After-commit async listener / outbox |
| **Med** | `ddl-auto=update` in prod, no migrations | `application-prod.properties` | Flyway |
| **Med** | Admin can set any status (no state machine); **admin cancel doesn't restore stock** | `AdminService.updateOrderStatus` | Enforce allowed transitions; share the cancel logic |
| **Med** | `cancelOrder` re-enables availability even for a painting the admin "deleted" (soft delete = same flag as sold-out) | `OrderService.cancelOrder`, `PaintingService.deletePainting` | Separate `status`/`deleted` flag from stock availability |
| **Med** | Possible `LazyInitializationException`: `getOrderById` and `getUserOrders` have no `@Transactional` while `open-in-view=false` and `Order.items`/`User.addresses` are lazy 🔎 | `OrderService` | Add `@Transactional(readOnly=true)`; first check whether it already works (Appendix B, E1) |
| **Med** | No unique constraint on (cart_id, painting_id) | `CartItem` | Unique constraint plus upsert |
| **Low** | `adminPhone` is injected from `admin.default.uncle.name` (a name, not a phone) ✅ | `OrderService` (also AdminService) | Proper `admin.default.phone` property |
| **Low** | A new Address row is created on every order, even for a saved address | `OrderService.createOrder` | Reuse the address by id; snapshot only if edited |
| **Low** | `orderNumber` uses `Math.random()` | `OrderService.generateOrderNumber` | DB sequence or ULID; the unique constraint prevents duplicates but a collision would be a 500 |
| **Low** | `getPaintingById` writes a view count on each GET | `PaintingService` | Atomic increment or buffered counter |
| **Low** | LIKE search without index support | `PaintingRepository.searchPaintings` | `pg_trgm` / FTS |
| **Low** | `console.log` of sensitive payloads | `auth.service.ts` | Remove |
| **Low** | Order confirmation state lost on refresh | `order-confirmation` | Fetch by id |
| **Low** | 500 handler leaks `ex.getMessage()` | `GlobalExceptionHandler` | Generic message plus correlation id; add a 409 handler for `DataIntegrityViolation` |

**How to use this in an interview:** never recite the whole table. Pick the one relevant to the question, state the impact in one sentence, then the fix in two.

---

## 14. "Biggest problem and how you solved it" (stories grounded in your repo)

I can't know your real history, so pick the ones that are **true for you** (check `git log`, ask your uncle or recall) and tell them as problem → diagnosis → decision → result → what I'd do differently.

**Story A: "Images disappeared after every deploy."**
Evidence: the ephemeral-filesystem comment in `application-prod.properties`, the legacy `image_url = 'INLINE'` column in `PaintingImage`, and `ImageController` serving bytes from the DB. Problem: uploads to local disk vanish on every Render/Railway restart. Decision: store image bytes in Postgres and serve through `/api/images/{id}` with a 30-day immutable cache header. Trade-off you accepted: simplicity and persistence versus DB size and performance, with object storage plus CDN as the planned next step. (This story shows you understand platform constraints and trade-offs.)

**Story B: "Schema drift and the stock column."**
Evidence: `src/main/resources/db/fix-stock-quantity.sql`, `reset_paintings.sql`, and `ddl-auto=update`. If true, tell it as: adding a NOT NULL column to a table that already had rows broke startup, so you wrote a SQL fix; the lasting lesson is Flyway migrations.

**Story C: "CORS and the API URL between Vercel and Render."**
Evidence: `client/vercel.json` rewrite with a relative `/api`, plus the CORS config with Vercel preview-URL patterns. Problem: cross-origin calls and credentials. Decision: proxy via rewrite so the browser sees one origin. Lesson: you also left CORS too open, which you'd tighten.

**Story D: "Free-tier performance."**
Evidence: the "PERFORMANCE OPTIMIZATIONS FOR FREE TIER" block in `application.properties` (gzip, Hikari limits, leak detection), Caffeine caches, lazy-loaded Angular routes, the browser cache interceptor, parallel `forkJoin` in checkout. Problem: slow pages and a tiny DB/CPU budget. Decision: layered caching and fewer round-trips. Lesson: caching introduced staleness bugs (cart, logout), so you'd now add proper invalidation and measure.

**Story E (the strongest "senior" story): "I audited it and found the stock race."**
Tell it as a review: you walked the checkout, noticed it's atomic but not isolated, reasoned about READ COMMITTED, and designed the conditional-update fix and a Testcontainers concurrency test. This shows judgment, which interviewers value more than "I never had a bug".

---

## 15. Future upgrades roadmap (with how to implement)

1. **Concurrency + idempotency** (sections 6 and 7): conditional stock update, CHECK constraint, idempotency key table, 409s, and a concurrency integration test.
2. **Payments (Razorpay/UPI):**
   - Server creates a payment order, the client pays, and the **webhook** (HMAC-verified, idempotent by payment/event id) confirms it.
   - Order states become `PENDING_PAYMENT → PAID → …`.
   - **Stock reservation with TTL:** reserve on checkout start, release via a scheduled job if unpaid (Spring `@Scheduled`, or a delayed message).
   - Order state machine enforced in one place.
3. **Async notifications:** `OrderPlaced` domain event with an `AFTER_COMMIT` listener, then email plus WhatsApp/SMS to the **admin (your uncle)** (he currently isn't notified of new orders, only the customer is). Upgrade to an outbox table plus a worker for guaranteed delivery and retries.
4. **Media:** move images to S3/R2/Cloudinary and serve via CDN; generate thumbnails/WebP; store only keys/URLs; presigned uploads so large files bypass the app. Certificates move off local disk too.
5. **Schema management:** Flyway migrations, `ddl-auto=validate`, backups and restore drills.
6. **Auth hardening:** refresh tokens, HttpOnly cookies (or short access tokens), login rate limiting, admin MFA, secrets via platform secret store.
7. **Performance:** list DTO with primary image only, batch fetching, Redis shared cache, `pg_trgm` search, pagination everywhere (admin lists, orders list returns a plain `List` today).
8. **Product features:** inventory for editions/prints, discount codes, order tracking updates by email, reviews, a wishlist share link, multi-currency (currency is hard-coded INR).
9. **SEO and growth:** Angular SSR/prerender for painting pages, structured data (schema.org `Product`), image `alt` text, sitemap generation from the DB.
10. **Quality:** unit tests for pricing/stock logic, MockMvc security tests, Testcontainers integration tests, GitHub Actions CI (build, test, Docker image), Sentry and metrics.
11. **AI angle (ties to your profile; keep it small and real):** painting description/SEO generation from the image, semantic search with embeddings (pgvector in the same Postgres), a RAG chat "ask about this painting" over the artist's notes. Say it as a roadmap, not as something already built.

---

## 16. Q&A drill bank (read each question aloud, answer, then compare)

Format: **Q → short model answer** (*where to look*). Answers are intentionally compact; expand with the sections above.

### A. Project and design
1. **Why this stack?** → Java/Spring Boot is what I use professionally; Angular for a structured SPA; Postgres for relational data and transactions; Docker for reproducible builds; Vercel and Render for cheap hosting. (*pom.xml, Dockerfile*)
2. **Walk me through the architecture.** → Use the diagram in section 2: SPA → Vercel rewrite → Spring Boot → Postgres, with the cache and email on the side.
3. **Why DTOs?** → Decouple API from schema, avoid exposing sensitive fields, avoid lazy-loading surprises, and control payload shape. (*BE/dto, MapperUtil*)
4. **Why `ApiResponse<T>` wrapper?** → Uniform `{success, message, data}` envelope for the frontend and error handling. (*dto/common/ApiResponse.java*)
5. **How do you handle validation?** → Bean Validation annotations on request DTOs with `@Valid`, mapped to 400 with field errors by `GlobalExceptionHandler`; business rules (stock, ownership) in services. DB constraints are the last line.
6. **How do you do pagination?** → Spring Data `Pageable`; the response wraps `PageResponse` (content, page, size, totals, first/last). The orders list currently returns a plain list (still paged by the query).
7. **Why store the price on the order item?** → Historical accuracy; prices change and orders must not.
8. **Why soft delete?** → Keeps referential integrity with historical orders.
9. **How are categories modelled?** → Many-to-many join table `painting_category_mapping`; `categories` list cached 1 hour.
10. **What does `DataInitializer` do and is it safe?** → Seeds admin, categories and sample paintings at startup, with `existsBy` checks so it's idempotent. Risk: default admin password fallback; multi-instance start-up races (unique constraints would catch).
11. **How is the admin dashboard computed?** → JPQL aggregates in `OrderRepository` (revenue excluding cancelled, status distribution, monthly trend); cached 5 minutes. (*AdminService.getDashboardStats*)
12. **Why `BigDecimal`?** → Binary floating point can't represent decimal money exactly; `BigDecimal` plus an explicit rounding mode avoids drift.

### B. Transactions, concurrency, idempotency
13. **How is checkout atomic?** → Section 5 model answer.
14. **What happens if the DB fails halfway through checkout?** → The exception propagates, Spring rolls back the transaction, and nothing persists; the user gets an error and the cart is intact.
15. **Does `@Transactional` roll back on checked exceptions?** → No, only unchecked unless `rollbackFor` is set.
16. **What's the transaction isolation level?** → Postgres default READ COMMITTED; Spring doesn't override it.
17. **Two users buy the last painting at once?** → Section 6; can oversell today; fix with atomic conditional update.
18. **Optimistic vs pessimistic locking?** → Section 6 table.
19. **What's a deadlock and how could you create one here?** → Two transactions locking the same rows in opposite order (A locks painting 1 then 2; B locks 2 then 1). Avoid by ordering locks by id and keeping transactions short.
20. **How do you handle idempotency?** → Section 7: incidental today, idempotency key design for the fix.
21. **User double-clicks Place Order?** → UI `isSubmitting` guard; server: the second request finds the cart cleared; the proper fix is an idempotency key.
22. **Why flush before clearing the cart?** → Forces constraint violations inside the transaction so they roll back everything, instead of surfacing at commit.
23. **What if the email fails?** → It's caught and logged; the order stays. Weakness: email is sent before commit and holds the transaction open; fix with after-commit async.
24. **What is the N+1 problem here and how would you fix it?** → Section 10 item 2.
25. **What would you do if the cancel and ship actions race?** → Guard transitions with a conditional UPDATE on the expected current status (or `@Version`), so only one wins.

### C. Security
26. **How does JWT auth work here?** → Sections 4.2 and 8.
27. **How do you secure admin APIs?** → URL rule (`hasRole('ADMIN')`) plus `@PreAuthorize`; the role comes from the DB per request.
28. **How do you prevent users from reading others' orders?** → The order's owner id is compared with the principal's id, which comes from the token, never from the request. (*OrderService.getOrderById*)
29. **Why disable CSRF?** → Stateless header-based auth isn't vulnerable to classic CSRF; if cookies were used I'd re-enable it.
30. **Weaknesses in your auth?** → localStorage token, no refresh/revocation, default-secret fallback, no login throttling. Fixes in section 8.
31. **How are passwords stored?** → BCrypt with a per-password salt; never reversible.
32. **What's in your JWT?** → Subject = user id, issued-at, expiry; signed HS256. No sensitive data, and the token is signed, not encrypted, so anyone can read the payload.
33. **HS256 vs RS256?** → HS256 = shared secret, fine for one service; RS256 = private key signs, public key verifies, better with multiple verifiers/services.
34. **XSS vs CSRF trade-off for token storage?** → localStorage: XSS steals it, no CSRF. HttpOnly cookie: JS can't read it, but needs CSRF protection and SameSite.
35. **How would you add refresh tokens?** → Short access token plus a long-lived rotating refresh token in an HttpOnly cookie, stored/hashed server-side so it can be revoked; rotate on use and detect reuse.

### D. Caching, performance, scaling
36. **What do you cache and why?** → Section 9.
37. **How do you invalidate?** → `@CacheEvict` on writes plus TTL.
38. **What happens with multiple instances?** → Per-JVM caches diverge; use Redis.
39. **Why Caffeine?** → Fast, in-process, with size and TTL eviction, good for a single small instance.
40. **How do you make the site handle more traffic?** → Section 10 staged plan.
41. **What's the first bottleneck?** → Image bytes and N+1 on the list endpoint, then the DB pool.
42. **Why serve images through the API instead of static files?** → Ephemeral FS on the host; DB persistence made deployment simple. Next step: object storage plus CDN.
43. **How do you size the connection pool?** → Total connections = instances × pool; must stay under the DB limit; small pools are usually enough because the DB, not the app, is the bottleneck. Use PgBouncer if needed.
44. **What would you monitor?** → Latency p95/p99, error rate, DB pool usage, cache hit ratio, slow queries, order success rate and stock anomalies.
45. **How would you load-test?** → k6/JMeter against browse and checkout flows, with a dataset of realistic size; watch the DB and the pool.

### E. Frontend
46. **Why standalone components and lazy loading?** → Less boilerplate, smaller initial bundle, route-level code splitting. (*app.routes.ts*)
47. **How do you share state?** → `BehaviorSubject` services (auth, cart) and signals for local/derived state. (*core/services*)
48. **How do interceptors work in your app?** → Cache, then attach token, then handle 401. Order matters (section 11).
49. **How do guards work and are they enough?** → UX only; the server enforces.
50. **How do you handle errors in the UI?** → The error interceptor plus per-component error signals, with server messages from `ApiResponse`.
51. **What is OnPush and why zoneless?** → Components re-render only on input/signal changes; zoneless removes zone.js overhead.
52. **How would you improve SEO?** → SSR/prerender, structured data, meta per painting, a generated sitemap.

### F. Deployment and engineering practice
53. **How do you deploy?** → Frontend on Vercel (build from `client/`), backend Docker on Render with env vars, DB on managed Postgres. (*vercel.json, Dockerfile*)
54. **How do you manage config and secrets?** → Env vars with local defaults. Gap: unsafe defaults for secrets; fix with required properties.
55. **How do you do DB migrations?** → Currently `ddl-auto=update`, which isn't production-safe; moving to Flyway.
56. **How do you test?** → Honestly minimal today; plan: Testcontainers for checkout concurrency, MockMvc for security, unit tests for totals. Don't oversell the existing tests.
57. **How would you do CI/CD?** → GitHub Actions: build, test, build the Docker image, deploy on merge.
58. **How would you roll back a bad deploy?** → Redeploy the previous image; migrations must be backward compatible (expand/contract).

### G. Behavioural and "your" angle
59. **What would you do differently?** → Start with Flyway, put images on object storage, add the stock/idempotency tests first, and design the order state machine up front.
60. **What was the hardest part?** → Pick a story from section 14.
61. **How is this different from Words2Voice?** → TalkingCanvas is a monolithic transactional CRUD/e-commerce system; Words2Voice protects a scarce single-worker model (semaphores, retries, rate limits, atomic quotas). The same concurrency thinking applies to TalkingCanvas stock.
62. **Did you write all of this yourself?** → "I built it AI-assisted, then reviewed it thoroughly. I can walk through any flow and I found and documented these specific issues." Then immediately show one.
63. **Why should we believe you understand it?** → Trace any flow in the code from memory (sections 4 and 5) and name its trade-offs.
64. **How would AI/RAG fit here?** → Section 15 item 11: semantic search with pgvector, generated descriptions, an assistant grounded in the artist's notes. Keep it to a roadmap statement.

### H. Cross-question chains (practise these)
- **"Atomic?" → "Concurrent?" → "Fix?" → "Test?"** Atomic checkout → two buyers can race → conditional UPDATE plus CHECK → Testcontainers with N threads.
- **"JWT?" → "Revoke?" → "Where stored?" → "Refresh?"** Stateless → no revocation today → localStorage vs cookie trade-off → rotating refresh tokens.
- **"Cache?" → "Stale?" → "Multi-instance?" → "Redis?"** TTL plus evict → cart staleness example → per-JVM problem → Redis shared cache with `AFTER_COMMIT` invalidation.
- **"Scale?" → "Bottleneck?" → "Evidence?" → "Next?"** Stages → images/N+1/pool → load test and metrics → object storage, CDN, replicas.
- **"Retry?" → "Idempotent?" → "Payments?" → "Webhooks?"** Idempotency-Key design → unique constraint in the same transaction → Razorpay webhook with signature and event-id dedupe.

---

## 17. Whiteboard drills (be able to draw these in 2 minutes each)

1. **System diagram** (section 2) with the cache, DB, email, and image path.
2. **ER diagram** (section 3) with the snapshot in `OrderItem`.
3. **Checkout sequence** (section 4.5): client → filter → controller → service → DB, with the transaction boundary drawn around steps 3-9 and the email outside it (in the target design).
4. **Race timeline** (section 6 table) and then the fixed version using the conditional UPDATE.
5. **Idempotency flow:** client key → unique insert → replay returns the stored order.

---

## Appendix A: One-page cheat sheet (re-read before interviews)

**Atomicity:** `createOrder` is one `@Transactional`: order, items, stock, cart clear all-or-nothing; rollback on runtime exceptions; flush forces constraint errors early.
**Isolation gap:** READ COMMITTED plus read-check-write on stock can oversell the last painting. Fix: atomic `UPDATE … WHERE stock >= qty` plus a CHECK constraint (also `@Version` or `FOR UPDATE` as alternatives).
**Idempotency:** not implemented; incidental guards only (UI disable, cart emptied, cancel status check). Design: Idempotency-Key, unique (user, key), inserted in the same transaction, replay returns the stored order.
**Security:** stateless JWT (sub = user id, HS256, 24h), BCrypt, the role comes from the DB per request, `hasRole` plus `@PreAuthorize`, CSRF off (header auth). Gaps: localStorage token, no refresh/revocation, default secrets, open CORS, actuator exposure, no login throttling.
**Caching:** browser Map interceptor, Caffeine on the server, 30-day image Cache-Control. Bugs: `user-cart` not evicted after an order, browser cache not user-scoped, per-JVM cache, dead `painting-details` cache.
**Performance:** images in DB bytes, N+1 on lists, per-request user lookup, email in transaction, view-count writes on GET, LIKE search.
**Scale story:** correctness first, then quick wins, then stateless horizontal scale (Redis, object storage), then CDN/replicas/queues. "Measure before scaling."
**Deploy:** Vercel rewrite `/api` → Render; Docker multi-stage; env-var config; `ddl-auto=update` → Flyway.
**Numbers:** JWT 24h; Hikari 5 (properties) / 10 (prod `DatabaseConfig`); caches 5 / 10 / 15 / 60 min; images cached 30 days; upload limit 10MB (50MB per request); Spring Boot 3.5.7, Java 21, Angular 20.3.

**Say / don't say**
- ✅ "Atomic, and I know what isn't isolated yet." ❌ "It's fully thread-safe."
- ✅ "Idempotency is the next piece; here's the design." ❌ "The cart clearing makes it idempotent."
- ✅ "I'd measure before scaling." ❌ "I'd move to microservices and Kubernetes."
- ✅ "AI-assisted, then audited." ❌ "I wrote every line" (if untrue).

---

## Appendix B: 2-minute experiments (optional; they turn 🔎 into facts)

Do these in your local setup if you have time. Each is a few clicks, no code changes.

- **E1: Lazy loading.** Log in, place an order, open order history and order detail (`GET /api/orders` and `/api/orders/{id}` via the UI or Swagger). If you see a `LazyInitializationException`, it's the missing `@Transactional(readOnly = true)` on `OrderService.getOrderById`/`getUserOrders`. If it works, find out why (check fetch types on `Order.items` and `User.addresses` and your logs).
- **E2: Image bytes in list queries.** With `spring.jpa.show-sql=true` (default in dev), load `/paintings` and look for a SELECT on `painting_images` that includes `image_data`. If present, the "lazy" byte field isn't lazy.
- **E3: Column type.** In `psql`, describe `painting_images` and note whether `image_data` is `bytea` or `oid`. Be able to explain the difference.
- **E4: Stale cart.** Add an item, place an order, then open the cart or check the badge. If the old items still show for a few minutes, that's the `user-cart` cache bug.
- **E5: Cross-user browser cache.** In one tab: log in as user A, open checkout (loads addresses), log out, log in as user B, open checkout. If A's addresses show, that's the cache leak.
- **E6: Oversell race (optional).** Make a painting with stock 1; use two browsers (two users) with the item in each cart, and click Place Order at nearly the same moment. Don't be disappointed if timing doesn't reproduce it; the reasoning is what counts.
- **E7: Actuator as a normal user.** Log in as a non-admin and request `/actuator/health` and `/actuator/env` with your token. Note what's reachable.
- **E8: Read `git log`.** Look for commits about images, CORS, Railway/Render, and the stock column. They're your real stories for section 14.

---

*End of guide. If you tell me which sections the interviewers hit hardest in your next mock, I can write a tighter drill set for just those.*
