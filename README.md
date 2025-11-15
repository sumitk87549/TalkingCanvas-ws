# TALKING CANVAS - Oil Paintings E-commerce Platform

A complete, production-ready e-commerce platform for selling oil paintings online, built with Spring Boot and Angular.

## 🎨 Features

### User Features
- ✅ User registration and authentication (JWT-based)
- ✅ Browse paintings with advanced filtering and sorting
- ✅ Search functionality
- ✅ Detailed painting views with image galleries and certificates
- ✅ Shopping cart management
- ✅ Order placement with COD payment
- ✅ Order history and tracking
- ✅ User profile management
- ✅ Password change functionality

### Admin Features
- ✅ Comprehensive dashboard with analytics
- ✅ Painting management (CRUD operations)
- ✅ Image and certificate uploads
- ✅ Category management
- ✅ User management
- ✅ Order management and status updates
- ✅ Revenue statistics and reports

### Legal & Compliance
- ✅ GDPR, CCPA, PDPA compliant
- ✅ Privacy Policy
- ✅ Terms and Conditions
- ✅ Shipping and Delivery Policy
- ✅ Return and Refund Policy
- ✅ Cookie Policy
- ✅ Cookie consent mechanism

## 🏗️ Technology Stack

### Backend
- **Spring Boot 3.5.7** (Latest LTS)
- **Spring Security** with JWT authentication
- **PostgreSQL 17.x** database
- **Hibernate ORM**
- **BCrypt** password hashing
- **Spring Mail** for email notifications
- **Swagger/OpenAPI 3.0** for API documentation
- **Lombok** for reducing boilerplate

### Frontend
- **Angular 20.3** (Latest)
- **Angular Material 19.2** for UI components
- **RxJS** for reactive programming
- **TypeScript 5.9**
- Responsive design (mobile-first)

## 📋 Prerequisites

- **Java 21** or higher
- **Node.js 20** or higher
- **PostgreSQL 17** or higher
- **Maven 3.8** or higher
- **npm 10** or higher

## 🚀 Quick Start

### 1. Database Setup

Create PostgreSQL database:

```bash
# Login to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE talkingcanvas;

# Exit psql
\q
```

The application will automatically create tables on first run using Hibernate's `ddl-auto=update`.

### 2. Backend Setup

```bash
# Navigate to project root
cd talkingCanvas

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**Default Admin Credentials:**
- Email: `admin@talkingcanvas.com`
- Password: `Admin@123`

### 3. Frontend Setup

```bash
# Navigate to client directory
cd client

# Install dependencies
npm install

# Start development server
npm start
```

The frontend will start on `http://localhost:4200`

### 4. Access the Application

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/api/swagger-ui.html
- **API Docs:** http://localhost:8080/api/docs

## 📁 Project Structure

```
talkingCanvas/
├── src/                        # Spring Boot Backend
│   ├── main/
│   │   ├── java/com/example/talkingCanvas/
│   │   │   ├── config/        # Configuration classes
│   │   │   ├── controller/    # REST Controllers
│   │   │   ├── dto/           # Data Transfer Objects
│   │   │   ├── exception/     # Exception handling
│   │   │   ├── model/         # JPA Entities
│   │   │   ├── repository/    # JPA Repositories
│   │   │   ├── security/      # Security & JWT
│   │   │   ├── service/       # Business logic
│   │   │   └── util/          # Utility classes
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── client/                     # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/          # Core services, guards, interceptors
│   │   │   ├── features/      # Feature modules
│   │   │   ├── models/        # TypeScript interfaces
│   │   │   └── shared/        # Shared components
│   │   └── environments/
│   └── package.json
├── uploads/                    # File storage (created automatically)
├── docker-compose.yml
├── Dockerfile
└── README.md
```

## 🔧 Configuration

### Backend Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/talkingcanvas
spring.datasource.username=postgres
spring.datasource.password=0000

# JWT
jwt.secret=YourSecretKeyHere
jwt.expiration=86400000

# Mail (Configure for production)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Admin Default
admin.default.email=admin@talkingcanvas.com
admin.default.password=Admin@123
admin.default.uncle.name=Rajesh Kumar

# File Upload
file.upload-dir=uploads
```

### Frontend Configuration

Edit `client/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## 🐳 Docker Deployment

### Build and Run with Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode
docker-compose up -d

# Stop services
docker-compose down
```

### Manual Docker Build

```bash
# Build backend
docker build -t talkingcanvas-backend .

# Build frontend
cd client
docker build -t talkingcanvas-frontend .

# Run containers
docker run -p 8080:8080 talkingcanvas-backend
docker run -p 80:80 talkingcanvas-frontend
```

## 📝 API Documentation

### Authentication Endpoints

```
POST /api/auth/register  - Register new user
POST /api/auth/login     - User login
```

### Painting Endpoints (Public)

```
GET  /api/paintings                  - Get all paintings (paginated)
GET  /api/paintings/{id}             - Get painting by ID
GET  /api/paintings/featured         - Get featured paintings
GET  /api/paintings/search?query=    - Search paintings
GET  /api/paintings/filter/price     - Filter by price range
GET  /api/paintings/categories       - Get all categories
```

### Cart Endpoints (Authenticated)

```
GET    /api/cart              - Get user's cart
POST   /api/cart/add          - Add item to cart
PUT    /api/cart/items/{id}   - Update cart item quantity
DELETE /api/cart/items/{id}   - Remove cart item
DELETE /api/cart              - Clear cart
```

### Order Endpoints (Authenticated)

```
POST   /api/orders           - Create order
GET    /api/orders           - Get user's orders
GET    /api/orders/{id}      - Get order details
POST   /api/orders/{id}/cancel - Cancel order
```

### Admin Endpoints (Admin Only)

```
GET    /api/admin/dashboard/stats     - Dashboard statistics
GET    /api/admin/users               - List all users
GET    /api/admin/orders              - List all orders
POST   /api/admin/paintings           - Create painting
PUT    /api/admin/paintings/{id}      - Update painting
DELETE /api/admin/paintings/{id}      - Delete painting
POST   /api/admin/paintings/{id}/images - Upload images
POST   /api/admin/paintings/{id}/certificates - Upload certificates
PUT    /api/admin/orders/{id}/status  - Update order status
```

Complete API documentation available at: http://localhost:8080/api/swagger-ui.html

## 🧪 Testing

### Backend Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

### Frontend Testing

```bash
cd client

# Run unit tests
npm test

# Run e2e tests
npm run e2e
```

## 🌍 Environment Variables

### Production Environment Variables

```bash
# Database
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=talkingcanvas
DB_USER=postgres
DB_PASSWORD=your-password

# JWT
JWT_SECRET=your-very-long-and-secure-secret-key
JWT_EXPIRATION=86400000

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Admin
ADMIN_EMAIL=admin@talkingcanvas.com
ADMIN_PASSWORD=secure-admin-password

# File Storage
FILE_UPLOAD_DIR=/var/talkingcanvas/uploads

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

## 📊 Database Schema

The application automatically creates the following tables:

- `users` - User accounts
- `addresses` - User delivery addresses
- `paintings` - Painting listings
- `painting_images` - Painting images
- `painting_certificates` - Painting certificates
- `painting_categories` - Categories/styles
- `painting_category_mapping` - Many-to-many relationship
- `carts` - Shopping carts
- `cart_items` - Cart items
- `orders` - Customer orders
- `order_items` - Order line items
- `contact_messages` - Contact form submissions
- `site_config` - Site configuration

## 🔒 Security Features

- JWT-based authentication
- BCrypt password hashing (strength: 10)
- Role-based access control (USER, ADMIN)
- CORS configuration
- SQL injection prevention
- XSS protection
- CSRF protection
- Secure file upload validation
- Password strength validation
- Email validation

## 📧 Email Configuration

For Gmail, create an App Password:

1. Go to Google Account Settings
2. Security → 2-Step Verification
3. App passwords → Select app (Mail) and device
4. Copy the generated password
5. Update `application.properties`:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=generated-app-password
```

## 🎨 Sample Data

The application seeds the following data on first run:

- **Admin User:** admin@talkingcanvas.com
- **Categories:** Abstract, Landscape, Portrait, Modern, Contemporary, etc.
- **Sample Paintings:** 3 sample paintings with descriptions

## 🚀 Production Deployment

### Checklist

- [ ] Update JWT secret to a strong random key
- [ ] Configure production database
- [ ] Set up email service (SMTP)
- [ ] Configure file storage (AWS S3, Azure Blob, etc.)
- [ ] Update CORS allowed origins
- [ ] Set up SSL/TLS certificates
- [ ] Configure CDN for static assets
- [ ] Set up logging and monitoring
- [ ] Configure backup strategy
- [ ] Update admin credentials
- [ ] Review and update all environment variables
- [ ] Enable production mode in Angular
- [ ] Minify and optimize assets
- [ ] Set up CI/CD pipeline
- [ ] Configure rate limiting
- [ ] Set up error tracking (Sentry, etc.)

### Build for Production

```bash
# Backend
mvn clean package -DskipTests

# Frontend
cd client
npm run build --configuration=production
```

## 📱 Responsive Design

The application is fully responsive and tested on:

- Desktop (1920x1080, 1366x768)
- Tablet (iPad, Surface)
- Mobile (iPhone, Android)
- Various browsers (Chrome, Firefox, Safari, Edge)

## ♿ Accessibility

- WCAG 2.1 Level AA compliant
- Keyboard navigation support
- Screen reader friendly
- Sufficient color contrast
- Alt text for images
- Semantic HTML

## 🌐 SEO Features

- Server-side rendering ready
- Dynamic meta tags
- Open Graph tags
- Twitter Card tags
- Structured data (JSON-LD)
- XML sitemap (auto-generated)
- robots.txt
- Canonical URLs
- SEO-friendly URLs

## 🐛 Troubleshooting

### Common Issues

**1. Database Connection Error**
```
Solution: Ensure PostgreSQL is running and credentials are correct
Check: application.properties database configuration
```

**2. JWT Token Expired**
```
Solution: Login again to get a new token
The token expires after 24 hours by default
```

**3. File Upload Error**
```
Solution: Ensure 'uploads' directory exists and has write permissions
Check: file.upload-dir in application.properties
```

**4. CORS Error**
```
Solution: Update allowed origins in application.properties
Check: cors.allowed-origins configuration
```

**5. Angular Port Already in Use**
```
Solution: ng serve --port 4201
Or kill the process using port 4200
```

## 📞 Support

For issues, questions, or contributions:

- Email: admin@talkingcanvas.com
- GitHub Issues: [Create Issue]
- Documentation: [API Docs](http://localhost:8080/api/swagger-ui.html)

## 📄 License

This project is proprietary and confidential.

## 👥 Credits

- **Backend:** Spring Boot, Spring Security, PostgreSQL
- **Frontend:** Angular, Angular Material
- **Authentication:** JWT (JSON Web Tokens)
- **API Documentation:** Swagger/OpenAPI

---

## 🎯 Next Steps After Setup

1. **Login as Admin:**
   - Email: admin@talkingcanvas.com
   - Password: Admin@123

2. **Create Categories:**
   - Navigate to Admin Dashboard
   - Add painting categories

3. **Add Paintings:**
   - Create new painting listings
   - Upload images and certificates
   - Set prices and descriptions

4. **Test User Flow:**
   - Register as a new user
   - Browse paintings
   - Add to cart
   - Place order

5. **Customize Content:**
   - Update About Us page
   - Configure contact information
   - Update legal pages for your jurisdiction

---

**Built with ❤️ for Talking Canvas**

*Last Updated: 2025-11-10*
