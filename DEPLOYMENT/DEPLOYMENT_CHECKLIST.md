# TalkingCanvas Production Deployment Checklist

## Pre-Deployment Configuration

### Backend Files Created/Updated
- [x] `system.properties` - Java 21 runtime declaration
- [x] `application-prod.properties` - Production database and CORS config
- [x] `Procfile` - Memory-limited startup command
- [x] `railway.json` - Railway platform configuration
- [x] `render.yaml` - Render blueprint for one-click deployment
- [x] `pom.xml` - Build properties and executable JAR config

### Frontend Files Updated
- [x] `environment.prod.ts` - Production API URL with comments
- [x] `package.json` - Production build scripts
- [x] `vercel.json` - Already configured for SPA routing

---

## Environment Variables Required

### Railway / Render (Backend)
Set these in your platform's environment variables:

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | ✅ Yes | Activates production profile | `prod` |
| `DATABASE_URL` | ✅ Yes | PostgreSQL connection string | Auto-provided by platform |
| `JWT_SECRET` | ✅ Yes | JWT signing key (64+ chars) | `YourSecureRandomString64+Characters` |
| `FRONTEND_URL` | ✅ Yes | Vercel frontend URL for CORS | `https://your-app.vercel.app` |
| `MAIL_USERNAME` | ⚠️ Optional | Gmail for notifications | `your-email@gmail.com` |
| `MAIL_PASSWORD` | ⚠️ Optional | Gmail app password | `your-app-password` |
| `ADMIN_EMAIL` | ✅ Yes | Admin user email | `pkumar.mail@gmail.com` |
| `ADMIN_PASSWORD` | ✅ Yes | Admin user password | `Admin@1998` |
| `ADMIN_NAME` | ⚠️ Optional | Admin display name | `Praveen Kumar` |

### Vercel (Frontend)
Usually not required - uses `environment.prod.ts`

---

## Deployment Steps

### Option 1: Railway

1. **Create Project**
   - Go to [railway.app](https://railway.app)
   - Click "New Project" → "Deploy from GitHub repo"
   - Select `TalkingCanvas-ws` repository

2. **Add PostgreSQL**
   - Click "+ New" → "Database" → "PostgreSQL"
   - Wait for provisioning (~30 seconds)

3. **Configure Service**
   - Click on your Spring Boot service
   - Go to "Variables" tab
   - Add all required environment variables (see table above)
   - For `DATABASE_URL`: Click value field → Type `$` → Select PostgreSQL → Select `DATABASE_URL`

4. **Deploy**
   - Railway auto-deploys on push
   - Monitor logs: "Deployments" tab
   - Generate domain: "Settings" → "Networking" → "Generate Domain"

5. **Test**
   - Visit: `https://your-app.railway.app/api/actuator/health`
   - Should return: `{"status":"UP"}`

---

### Option 2: Render

1. **Create Web Service**
   - Go to [render.com](https://render.com)
   - Click "New +" → "Web Service"
   - Connect GitHub repository: `TalkingCanvas-ws`

2. **Configure Build**
   - **Name**: `talkingcanvas-backend`
   - **Runtime**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -Xmx512m -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar`

3. **Add PostgreSQL**
   - Go to Dashboard → "New +" → "PostgreSQL"
   - Name: `talkingcanvas-db`
   - Plan: Free
   - After creation, copy "Internal Database URL"

4. **Set Environment Variables**
   - Go to your web service → "Environment"
   - Add all required variables (see table above)
   - For `DATABASE_URL`: Paste the PostgreSQL Internal Database URL

5. **Deploy**
   - Click "Manual Deploy" or wait for auto-deploy
   - Monitor logs in real-time
   - Get URL from service dashboard

6. **Test**
   - Visit: `https://your-app.onrender.com/api/actuator/health`
   - Should return: `{"status":"UP"}`

---

### Option 3: Vercel (Frontend)

1. **Import Project**
   - Go to [vercel.com](https://vercel.com)
   - Click "Add New..." → "Project"
   - Import `TalkingCanvas-ws` repository

2. **Configure Settings**
   - **Framework Preset**: Angular
   - **Root Directory**: `client`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist/client/browser`
   - **Install Command**: `npm install`

3. **Deploy**
   - Click "Deploy"
   - Wait 3-5 minutes for build
   - Get deployment URL

4. **Update Backend CORS**
   - Go to Railway/Render
   - Update `FRONTEND_URL` environment variable to your Vercel URL
   - Backend will auto-redeploy

5. **Test**
   - Visit your Vercel URL
   - Try user registration
   - Try login
   - Check browser console for CORS errors (should be none)

---

## Troubleshooting

### Build Fails on Railway/Render

**Symptom**: "Out of memory" or build timeout

**Solution**:
```bash
# Already configured in Procfile with -Xmx512m
# If still failing, check logs for specific errors
```

### Database Connection Fails

**Symptom**: "Connection refused" in logs

**Solution**:
1. Verify `DATABASE_URL` is correctly set
2. For Railway: Use "Reference" feature, not manual paste
3. For Render: Use "Internal Database URL" not External
4. Check PostgreSQL service is running (green status)

### CORS Errors in Browser

**Symptom**: "CORS policy" errors in browser console

**Solution**:
1. Verify `FRONTEND_URL` matches your Vercel URL exactly
2. Check no trailing slashes
3. For preview deployments, CORS config supports `*.vercel.app` wildcard

### Port Issues

**Symptom**: "Port already in use" or service won't start

**Solution**:
```properties
# Already configured in application-prod.properties
server.port=${PORT:8080}
```
Platform sets `$PORT` automatically

### Angular Build Fails on Vercel

**Symptom**: "Cannot find module" errors

**Solution**:
1. Ensure Root Directory = `client`
2. Check `package.json` has all dependencies
3. Try adding environment variable: `NODE_VERSION=20.x`

---

## Post-Deployment Verification

- [ ] Backend health endpoint responds with `{"status":"UP"}`
- [ ] Frontend loads without errors
- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Paintings list displays
- [ ] Admin login works
- [ ] Admin dashboard accessible
- [ ] No CORS errors in browser console
- [ ] Cart functionality works
- [ ] Mobile responsive design works

---

## Free Tier Limitations

### Railway
- **Free credit**: $5/month
- **Uptime**: ~4-5 hours/day with free tier
- **Upgrade**: $5/month for 24/7 uptime

### Render
- **Free tier**: Always free
- **Limitation**: Spins down after 15 min inactivity
- **Cold start**: 30-60 seconds on first request
- **Upgrade**: $7/month for always-on

### Vercel
- **Free tier**: 100 GB bandwidth/month
- **Build time**: 6000 minutes/month
- **Perfect for**: Personal projects

---

## Security Recommendations

1. **Change default passwords** after first deployment
2. **Use strong JWT_SECRET** (generate random 64+ character string)
3. **Enable 2FA** on admin accounts
4. **Keep dependencies updated**: Run `mvn versions:display-dependency-updates`
5. **Monitor logs** regularly for suspicious activity

---

## Quick Commands Reference

```bash
# Test local build
./mvnw clean package -DskipTests

# Test with production profile
java -Dspring.profiles.active=prod -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar

# Build frontend
cd client && npm run build

# Check file permissions
chmod +x mvnw

# Git commit all changes
git add .
git commit -m "Production deployment configuration"
git push origin main
```

---

## Support Resources

- **Railway**: [docs.railway.app](https://docs.railway.app)
- **Render**: [render.com/docs](https://render.com/docs)
- **Vercel**: [vercel.com/docs](https://vercel.com/docs)
- **Spring Boot**: [spring.io/guides](https://spring.io/guides)
- **Angular**: [angular.io/docs](https://angular.io/docs)

---

**Last Updated**: 2025-11-29
