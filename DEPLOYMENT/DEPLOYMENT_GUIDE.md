# Complete Deployment Guide: TalkingCanvas to Railway & Vercel

## 📋 Overview

This guide will help you deploy:
- **Backend (Spring Boot)** + **PostgreSQL** → Railway.com
- **Frontend (Angular)** → Vercel.com

You'll learn how to configure everything step-by-step with no prior DevOps experience needed.

---

## 🎯 Prerequisites Checklist

- ✅ GitHub account connected to Railway
- ✅ GitHub account connected to Vercel
- ✅ Repository `TalkingCanvas-ws` visible on both platforms
- ✅ Local development environment working

---

## 📦 Part 1: Prepare Your Code for Deployment

### Step 1.1: Create Production Application Properties

You need a separate configuration file for production that Railway will use.

**Create file**: `src/main/resources/application-prod.properties`

```properties
# Application Name
spring.application.name=talkingCanvas

# Server Configuration - Railway will set PORT env variable
server.port=${PORT:8080}
server.error.include-message=always
server.error.include-binding-errors=always

# Database Configuration - Use Railway PostgreSQL environment variables
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

# JWT Configuration - Use environment variable
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
file.upload-dir=/app/uploads

# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Admin Configuration
admin.default.email=${ADMIN_EMAIL}
user.default.email=user@user.com
admin.default.password=${ADMIN_PASSWORD}
admin.default.name=${ADMIN_NAME:Admin}
admin.default.admin.name=${ADMIN_NAME:Admin}
admin.default.uncle.name=${ADMIN_NAME:Admin}

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api/docs
springdoc.swagger-ui.path=/api/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Logging Configuration - Reduce logging in production
logging.level.root=INFO
logging.level.com.example.talkingCanvas=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate.SQL=WARN

# CORS Configuration - Use environment variable for frontend URL
cors.allowed-origins=${FRONTEND_URL}

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoints.web.base-path=/api/actuator
management.endpoints.web.cors.allowed-origins=${FRONTEND_URL}
management.endpoints.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
management.endpoints.web.cors.allowed-headers=*
management.endpoint.metrics.enabled=true
```

### Step 1.2: Update Angular Environment for Production

**Edit file**: `client/src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiUrl: 'YOUR_RAILWAY_BACKEND_URL/api'  // We'll update this later
};
```

### Step 1.3: Create Railway-specific Files

**Create file in project root**: `Procfile` (no extension)

```
web: java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar
```

### Step 1.4: Update pom.xml for Railway Deployment

Make sure your `pom.xml` has the correct build configuration. Your current one looks good, but verify the build section includes the Spring Boot Maven plugin (which it does on line 134-145).

### Step 1.5: Create .gitignore Updates

Make sure these are in your `.gitignore`:
```
uploads/
target/
*.log
application-local.properties
```

### Step 1.6: Commit and Push Changes

```bash
git add .
git commit -m "Add production configuration for Railway and Vercel deployment"
git push origin main
```

---

## 🚂 Part 2: Deploy Backend + Database to Railway

### Step 2.1: Create New Project on Railway

1. Go to [railway.app](https://railway.app)
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your repository: **TalkingCanvas-ws**
5. Railway will detect it's a Java/Spring Boot project

### Step 2.2: Add PostgreSQL Database

1. In your Railway project dashboard, click **"+ New"**
2. Select **"Database"**
3. Choose **"PostgreSQL"**
4. Railway will create a PostgreSQL instance
5. Wait for it to provision (takes ~30 seconds)

### Step 2.3: Configure Environment Variables

Click on your **Spring Boot service** (not the database), then go to **"Variables"** tab.

Add these environment variables:

| Variable Name | Value | Notes |
|--------------|-------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Tells Spring to use application-prod.properties |
| `DATABASE_URL` | Click "Reference" → Select PostgreSQL → `DATABASE_URL` | This automatically links to your Railway PostgreSQL |
| `JWT_SECRET` | `TalkingCanvasProductionSecretKeyForJWT2024VerySecureAndLongMin64Chars` | Must be at least 64 characters |
| `MAIL_USERNAME` | `your-gmail@gmail.com` | Your Gmail address |
| `MAIL_PASSWORD` | `your-app-password` | Gmail app password (not regular password) |
| `ADMIN_EMAIL` | `pkumar.mail@gmail.com` | Admin email |
| `ADMIN_PASSWORD` | `Admin@1998` | Admin password (consider changing) |
| `ADMIN_NAME` | `Praveen Kumar` | Admin name |
| `FRONTEND_URL` | `https://your-app.vercel.app` | We'll update this after Vercel deployment |
| `MAVEN_OPTS` | `-Xmx512m` | Limit memory for build |

> **Important**: For `DATABASE_URL`, don't paste the value manually. Use Railway's "Reference" feature:
> 1. Click in the value field
> 2. Type `$`
> 3. Select your PostgreSQL service
> 4. Select `DATABASE_URL`

### Step 2.4: Configure Build Settings

1. In your service, go to **"Settings"** tab
2. **Root Directory**: Leave empty (it's the root)
3. **Build Command**: `mvn clean package -DskipTests`
4. **Start Command**: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar`
5. **Watch Paths**: Leave as default

### Step 2.5: Deploy

1. Railway should automatically trigger a deployment
2. Go to **"Deployments"** tab to watch the build logs
3. Wait for build to complete (5-10 minutes first time)
4. If successful, you'll see a green checkmark

### Step 2.6: Get Your Railway Backend URL

1. Go to **"Settings"** tab
2. Scroll to **"Networking"** section
3. Click **"Generate Domain"**
4. Railway will give you a URL like: `your-app-production.up.railway.app`
5. **Copy this URL** - you'll need it for Vercel!

### Step 2.7: Test Your Backend

Visit: `https://your-app-production.up.railway.app/api/actuator/health`

You should see:
```json
{
  "status": "UP"
}
```

---

## ☁️ Part 3: Deploy Frontend to Vercel

### Step 3.1: Update Production Environment with Railway URL

**Edit**: `client/src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-app-production.up.railway.app/api'  // Your Railway URL + /api
};
```

**Commit and push:**
```bash
git add client/src/environments/environment.prod.ts
git commit -m "Update production API URL to Railway backend"
git push origin main
```

### Step 3.2: Create Vercel Project

1. Go to [vercel.com](https://vercel.com)
2. Click **"Add New..."** → **"Project"**
3. Select your repository: **TalkingCanvas-ws**
4. Click **"Import"**

### Step 3.3: Configure Build Settings

In the configuration screen:

| Setting | Value |
|---------|-------|
| **Framework Preset** | Angular |
| **Root Directory** | `client` |
| **Build Command** | `npm run build` or `ng build --configuration production` |
| **Output Directory** | `dist/client/browser` (Angular 17+) or `dist/client` (older) |
| **Install Command** | `npm install` |
| **Node Version** | 18.x or 20.x (in Environment Variables) |

### Step 3.4: Add Environment Variables (Optional)

If you have any environment-specific settings for Angular, add them here. For now, you likely don't need any since you're using the `environment.prod.ts` file.

### Step 3.5: Deploy

1. Click **"Deploy"**
2. Vercel will build your Angular app (takes 3-5 minutes)
3. Watch the build logs
4. When it succeeds, Vercel gives you a URL like: `talkingcanvas-ws.vercel.app`

### Step 3.6: Update Railway CORS Settings

Now that you have your Vercel URL, go back to Railway:

1. Open your **Spring Boot service**
2. Go to **"Variables"**
3. Update **`FRONTEND_URL`** to: `https://talkingcanvas-ws.vercel.app`
4. Railway will automatically redeploy

---

## 🔗 Part 4: Final Configuration & Testing

### Step 4.1: Update CORS in Spring Boot (if needed)

Check your Spring Boot CORS configuration class. It should read from the `cors.allowed-origins` property, which we set via `FRONTEND_URL` environment variable.

If you have a `CorsConfig` or `WebConfig` class, make sure it looks like this:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### Step 4.2: Test Complete Flow

1. **Visit your Vercel frontend**: `https://talkingcanvas-ws.vercel.app`
2. **Test registration**: Try creating a new account
3. **Test login**: Login with your test account
4. **Test API calls**: Navigate through the app, check if paintings load
5. **Test admin**: Login as admin (email: `pkumar.mail@gmail.com`, password: `Admin@1998`)

### Step 4.3: Check Logs

**Railway Logs:**
1. Go to your Railway project
2. Click on your Spring Boot service
3. Go to **"Deployments"** → Click latest deployment → **"View Logs"**
4. Look for any errors

**Vercel Logs:**
1. Go to your Vercel project
2. Click on your deployment
3. Check **"Functions"** tab for any errors

---

## 📝 Part 5: Important Environment Variables Summary

### Railway Environment Variables (Spring Boot + PostgreSQL)

```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<Railway PostgreSQL reference>
JWT_SECRET=TalkingCanvasProductionSecretKeyForJWT2024VerySecureAndLongMin64Chars
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-app-password
ADMIN_EMAIL=pkumar.mail@gmail.com
ADMIN_PASSWORD=Admin@1998
ADMIN_NAME=Praveen Kumar
FRONTEND_URL=https://talkingcanvas-ws.vercel.app
MAVEN_OPTS=-Xmx512m
```

### Vercel Environment Variables (Angular)

Generally, none are needed if you're using `environment.prod.ts`. However, if you want to make the API URL configurable:

```bash
# Optional - if you modify your Angular app to use process.env
NG_APP_API_URL=https://your-app-production.up.railway.app/api
```

---

## 🚀 Part 6: Gmail App Password Setup (for emails)

Your app needs a Gmail App Password (not your regular Gmail password).

1. Go to [Google Account](https://myaccount.google.com/)
2. Navigate to **Security**
3. Enable **2-Step Verification** (if not already)
4. Scroll to **"2-Step Verification"** section
5. At the bottom, click **"App passwords"**
6. Select app: **"Mail"**
7. Select device: **"Other"** (name it "TalkingCanvas")
8. Click **"Generate"**
9. Copy the 16-character password
10. Use this as your `MAIL_PASSWORD` in Railway

---

## 🔧 Part 7: Troubleshooting Common Issues

### Issue 1: Railway Build Fails

**Symptom**: Build logs show "Out of memory" or build timeout

**Solution**:
- Add `MAVEN_OPTS=-Xmx512m` to environment variables
- Or update build command: `mvn clean package -DskipTests -Dmaven.test.skip=true`

### Issue 2: Database Connection Failed

**Symptom**: Logs show "Connection refused" or "Unknown database"

**Solution**:
- Verify `DATABASE_URL` is set correctly in Railway variables
- Make sure you used "Reference" to link to PostgreSQL, not manual paste
- Check PostgreSQL service is running (green status)

### Issue 3: CORS Errors on Frontend

**Symptom**: Browser console shows "CORS policy" errors

**Solution**:
- Verify `FRONTEND_URL` in Railway matches your Vercel URL exactly
- Check Spring Boot CORS configuration accepts your frontend URL
- Ensure no trailing slashes in URLs

### Issue 4: Vercel Build Fails

**Symptom**: Build logs show "Cannot find module" or Angular errors

**Solution**:
- Verify Root Directory is set to `client`
- Check Output Directory matches your Angular version
- Ensure `package.json` has all dependencies
- Try adding `NODE_VERSION=18.x` to Vercel environment variables

### Issue 5: 404 on Vercel Routes

**Symptom**: Refreshing Angular routes shows 404

**Solution**:
Create `vercel.json` in `client/` directory:

```json
{
  "routes": [
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ]
}
```

### Issue 6: File Uploads Not Working

**Symptom**: Image uploads fail on Railway

**Solution**:
- Railway uses ephemeral storage (resets on redeploy)
- For production, use cloud storage (AWS S3, Cloudinary, etc.)
- Temporary fix: Files work but disappear on redeploy

---

## 🎨 Part 8: Post-Deployment Checklist

- [ ] Backend health check works: `/api/actuator/health`
- [ ] Frontend loads successfully
- [ ] User registration works
- [ ] User login works
- [ ] Paintings list loads
- [ ] Admin login works
- [ ] Admin dashboard accessible
- [ ] Cart functionality works
- [ ] Checkout flow works
- [ ] Email notifications work (test forgot password)
- [ ] Theme toggle works (day/night mode)
- [ ] Mobile responsive design works

---

## 📊 Part 9: Monitoring Your Deployments

### Railway Monitoring

1. **Metrics**: Go to service → "Metrics" tab
   - View CPU, memory, network usage
   - Monitor request rates

2. **Logs**: Go to "Deployments" → Latest → "View Logs"
   - Check for errors
   - Monitor application startup

3. **Database**: Click PostgreSQL service
   - View connection count
   - Monitor storage usage

### Vercel Monitoring

1. **Analytics**: Enable Vercel Analytics
   - Click "Analytics" tab
   - See page views, visitors

2. **Speed Insights**: Track performance
   - Lighthouse scores
   - Core Web Vitals

---

## 💰 Part 10: Cost & Limits

### Railway Free Tier
- **Free credit**: $5/month
- **PostgreSQL**: Can use free credit
- **Spring Boot**: Uses ~0.5-1 GB RAM
- **Estimate**: ~4-5 hours uptime/day with free tier
- **Upgrade**: $5/month for more resources

### Vercel Free Tier
- **Bandwidth**: 100 GB/month
- **Build time**: 6000 minutes/month
- **Deployments**: Unlimited
- **Perfect for**: Personal projects & portfolios

---

## 🔐 Part 11: Security Best Practices (Post-Deployment)

1. **Change Default Passwords**
   ```bash
   # In Railway, update:
   ADMIN_PASSWORD=<strong-unique-password>
   ```

2. **Secure JWT Secret**
   ```bash
   # Generate a strong random secret
   JWT_SECRET=<use-random-64+-character-string>
   ```

3. **HTTPS Only**
   - Both Railway and Vercel provide HTTPS automatically
   - Ensure your CORS settings use `https://`

4. **Environment Variables**
   - Never commit sensitive data to git
   - Use Railway/Vercel environment variables
   - Keep `.env` files in `.gitignore`

---

## 🆘 Need Help?

### Railway Support
- [Railway Docs](https://docs.railway.app/)
- [Railway Discord](https://discord.gg/railway)

### Vercel Support
- [Vercel Docs](https://vercel.com/docs)
- [Vercel Community](https://github.com/vercel/vercel/discussions)

### Your App Logs
- Railway: Service → Deployments → View Logs
- Vercel: Project → Deployment → Functions

---

## ✅ Success Criteria

Your deployment is successful when:

1. ✅ Railway backend URL responds to `/api/actuator/health`
2. ✅ Vercel frontend loads without errors
3. ✅ You can register a new user
4. ✅ You can login and see paintings
5. ✅ Admin panel is accessible
6. ✅ No CORS errors in browser console
7. ✅ Database persists data between requests

---

## 🎉 You're Done!

Your TalkingCanvas application is now live on the internet!

- **Frontend**: `https://talkingcanvas-ws.vercel.app`
- **Backend**: `https://your-app-production.up.railway.app`
- **API Docs**: `https://your-app-production.up.railway.app/api/swagger-ui.html`

Share your application with the world! 🌎
