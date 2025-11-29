# Complete Beginner's Guide: Deploy TalkingCanvas Backend (Spring Boot) for FREE

**For Non-DevOps Users** • **No Technical Knowledge Required** • **Step-by-Step with Pictures Descriptions**

---

## 📚 What This Guide Contains

This guide will help you deploy your TalkingCanvas Spring Boot backend application to free cloud platforms, even if you've never done deployment before. By the end, you'll have:

1. ✅ Your Spring Boot backend running on the internet (FREE)
2. ✅ PostgreSQL database connected (FREE)
3. ✅ Everything connected to your Angular frontend
4. ✅ A working application accessible worldwide

---

## 📖 Related Deployment Guides

This is **Part 2 of 3** in the TalkingCanvas deployment series:

1. **[POSTGRESQL_DEPLOYMENT_GUIDE.md](POSTGRESQL_DEPLOYMENT_GUIDE.md)** - Setup free PostgreSQL database (Supabase/Aiven)
2. **[BEGINNER_DEPLOYMENT_GUIDE.md](BEGINNER_DEPLOYMENT_GUIDE.md)** - Deploy Spring Boot backend (**You are here**)
3. **[ANGULAR_DEPLOYMENT_GUIDE.md](ANGULAR_DEPLOYMENT_GUIDE.md)** - Deploy Angular frontend (Vercel/Netlify/GitHub Pages)

> **💡 Recommended Order**: Database → Backend → Frontend

---

## 🎯 Before You Start - What You Need

### Must Have (Already Done):
- [x] Code pushed to GitHub repository
- [x] GitHub account

### We'll Set Up Together:
- [ ] PostgreSQL Database (FREE - Supabase recommended) - See [POSTGRESQL_DEPLOYMENT_GUIDE.md](POSTGRESQL_DEPLOYMENT_GUIDE.md)
- [ ] Backend Hosting (FREE - Railway or Render recommended)
- [ ] Frontend Hosting (FREE - Vercel) - See [ANGULAR_DEPLOYMENT_GUIDE.md](ANGULAR_DEPLOYMENT_GUIDE.md)

---

## 📋 Table of Contents

**PART 1: Setup PostgreSQL Database**
- [Option A: Supabase (Recommended - Easiest)](#part-1a-setup-postgresql-on-supabase)
- [Option B: Aiven (Alternative)](#part-1b-setup-postgresql-on-aiven)

**PART 2: Deploy Spring Boot Backend**
- [Option A: Railway (Recommended - Fastest)](#part-2a-deploy-to-railway)
- [Option B: Render (Best Free Tier)](#part-2b-deploy-to-render)
- [Option C: Koyeb](#part-2c-deploy-to-koyeb)
- [Option D: Google Cloud](#part-2d-deploy-to-google-cloud)
- [Option E: Oracle Cloud](#part-2e-deploy-to-oracle-cloud)

**PART 3: Connect Everything Together**
- [Step 1: Connect Backend to Database](#part-3-step-1-connect-backend-to-database)
- [Step 2: Deploy Angular Frontend](#part-3-step-2-deploy-angular-frontend)
- [Step 3: Connect Frontend to Backend](#part-3-step-3-connect-frontend-to-backend)
- [Step 4: Final Testing](#part-3-step-4-final-testing)

**PART 4: Important Information**
- [Values You Must Save](#part-4-values-to-save-dont-lose-these)
- [Environment Variables Explained](#part-4-environment-variables-explained)
- [Troubleshooting Common Problems](#part-4-troubleshooting)

---

# PART 1A: Setup PostgreSQL on Supabase

> **Why Supabase?** Free forever, easy to use, no credit card required, works perfectly with all platforms.

## Step 1.1: Create Supabase Account (3 minutes)

1. **Open your browser** and go to: `https://supabase.com`

2. **Look for the green button** that says **"Start your project"** (usually in the top-right corner)
   - Click it

3. **Sign up screen** will appear. You have 2 options:
   - **Option 1 (Recommended)**: Click the **"Continue with GitHub"** button
     - This will ask you to log in to GitHub
     - Click "Authorize Supabase" when asked
   - **Option 2**: Enter your email address and create a password
     - You'll receive a verification email
     - Click the link in the email to verify

4. **You're now logged in!** You'll see the Supabase dashboard (a page with your projects)

---

## Step 1.2: Create Database Project (5 minutes)

1. **On the Supabase dashboard**, look for a button that says **"New Project"** or **"+ New project"**
   - It's usually a green button
   - Click it

2. **Organization selection** (if asked):
   - Choose your **personal organization** (usually has your name)
   - If you only have one, it will auto-select

3. **Fill in the Project Details** form:

   **Field 1: Name**
   ```
   Type: talkingcanvas-db
   (You can use any name, but this is easy to remember)
   ```

   **Field 2: Database Password**
   - You'll see a text box with **"Generate a password"** next to it
   - **IMPORTANT**: Click **"Generate a password"** button
   - A random password will appear (looks like: `abc123XYZ!@#`)
   - **COPY THIS PASSWORD** immediately and save it somewhere safe (Notepad, sticky note, etc.)
   - ⚠️ **You CANNOT see this password again!**

   **Field 3: Region**
   - Click the dropdown
   - Choose the **region closest to where most users will be**:
     - `East US (North Virginia)` - For US East Coast
     - `West US (Oregon)` - For US West Coast
     - `Europe West` - For Europe
     - `Southeast Asia` - For Asia
   - Don't worry too much about this - any region works!

   **Field 4: Pricing Plan**
   - Make sure **"Free Plan"** is selected
   - It might say "Free - $0/month"
   - Should show: "500 MB Database, 2 GB File Storage"

4. **Click the green "Create new project" button** at the bottom

5. **Wait 2-3 minutes**
   - You'll see a loading screen that says "Setting up your project..."
   - **DO NOT CLOSE THIS PAGE**
   - When ready, you'll see the project dashboard

---

## Step 1.3: Get Your Database Connection Details (CRITICAL - Don't Skip!)

> **What we're doing**: Getting the "address" of your database so Spring Boot can connect to it.

1. **You should be on your project dashboard** (you just created the project)

2. **Find the sidebar** (left side of the screen) and look for a **gear/settings icon** ⚙️
   - It might be labeled **"Project Settings"** or just show a gear icon
   - **Click it**

3. **You'll see a new menu**. Look for **"Database"** in the left sidebar or tabs
   - **Click "Database"**

4. **Scroll down** on this page until you see a section called **"Connection string"** or **"Connection info"**

5. **You'll see a dropdown or toggle**. Look for options like:
   - URI
   - Connection pooling
   - Session mode
   - **SELECT "URI"** (this is what we need)

6. **You'll now see a long text string** that looks like:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.abc123xyz.supabase.co:5432/postgres
   ```

7. **COPY THE ENTIRE STRING** but pay attention to this:
   - Where it says `[YOUR-PASSWORD]`, it might show as `[YOUR-PASSWORD]` or dots `***`
   - **YOU NEED TO REPLACE THIS with the actual password you saved earlier!**

---

## Step 1.4: Save Your Database Information (Print or Write Down!)

**Open a text editor (Notepad, Word, etc.) and copy this template. Fill in YOUR values:**

```
=== SUPABASE DATABASE CREDENTIALS ===
Created on: [today's date]

Database Host: db.[something].supabase.co
Database Port: 5432
Database Name: postgres
Database Username: postgres
Database Password: [the password you saved in Step 1.2]

Full CONNECTION STRING for Spring Boot:
jdbc:postgresql://postgres:[YOUR-PASSWORD]@db.[something].supabase.co:5432/postgres?sslmode=require

EXAMPLE (yours will look similar):
jdbc:postgresql://postgres:SecurePass123!@db.abc123xyz.supabase.co:5432/postgres?sslmode=require
```

**Replace:**
- `[YOUR-PASSWORD]` with your actual database password
- `[something]` with your actual project identifier (you copied this in Step 1.3)

⚠️ **SAVE THIS FILE** - You'll need this in Part 2!

---

# PART 1B: Setup PostgreSQL on Aiven

> **Alternative to Supabase**. Use this if you prefer Aiven or if Supabase doesn't work for you.

## Step 1.1: Create Aiven Account

1. Go to `https://aiven.io`
2. Click **"Sign up for free"** (blue button, top-right)
3. Either:
   - Click **"Continue with GitHub"** (recommended)
   - OR Enter email and create password
4. Verify your email if needed

## Step 1.2: Create PostgreSQL Database

1. After logging in, click **"Create service"** (big blue button)

2. **Select Service Type**:
   - You'll see boxes for different databases
   - **Click "PostgreSQL"** (elephant icon)

3. **Select Cloud Provider and Region**:
   - Cloud: Choose **AWS**, **Google Cloud**, or **Azure** (doesn't matter much)
   - Region: Choose closest to you
   - Click your choice

4. **Select Plan**:
   - Look for **"Free plan"** or **"Hobbyist"**
   - Should say **$0/month**
   - Should show: 1 CPU, 1 GB RAM, 5 GB storage
   - **Click to select it**

5. **Service Name**:
   ```
   Type: talkingcanvas-postgres
   ```

6. **Click "Create service"** (button at bottom right)

7. **Wait 3-5 minutes**
   - Status will change from "REBUILDING" → "RUNNING"
   - **DO NOT CLOSE THE PAGE**

## Step 1.3: Get Connection Information

1. **Once status is "RUNNING"**, you'll see **"Service URI"** on the overview page

2. **The URI looks like**:
   ```
   postgres://avnadmin:abc123xyz@pg-something.aivencloud.com:12345/defaultdb?sslmode=require
   ```

3. **Copy this ENTIRE string**

4. **Convert it to Spring Boot format**:
   - Take your  copied string
   - Add `jdbc:` at the very beginning
   - Change `postgres://` to `postgresql://`

   **Original**:
   ```
   postgres://avnadmin:abc123xyz@pg-something.aivencloud.com:12345/defaultdb?sslmode=require
   ```

   **Converted for Spring Boot**:
   ```
   jdbc:postgresql://avnadmin:abc123xyz@pg-something.aivencloud.com:12345/defaultdb?sslmode=require
   ```

5. **Save this converted string** - you'll need it in Part 2!

---

# PART 2A: Deploy to Railway

> **Recommended for beginners** - Easiest platform, fastest deployment, clear interface.

## What You'll Get:
- ✅ Free $5 credit per month (~4-5 hours uptime/day)
- ✅ Automatic builds when you push to GitHub
- ✅ Built-in PostgreSQL (or use your Supabase)
- ✅ Easy to use interface

---

## Step 2A.1: Create Railway Account

1. **Go to**: `https://railway.app`

2. **Click** the big button that says **"Login"** or **"Start a New Project"**

3. **Sign in with GitHub**:
   - Click **"Login with GitHub"** (purple/black button)
   - Authorize Railway when GitHub asks
   - You'll be redirected back to Railway

4. **You're now on the Railway Dashboard!**
   - You'll see "Create a New Project" or a "+" button

---

## Step 2A.2: Create New Project and Deploy from GitHub

1. **Click "New Project"** (big purple button) or **"+ New Project"**

2. **You'll see options**. Click **"Deploy from GitHub repo"**

3. **Authorize GitHub** (if asked):
   - Click "configure GitHub App"
   - Select which repositories Railway can access:
     - **Recommended**: Select "Only select repositories"
     - Choose: `TalkingCanvas-ws`
   - Click "Install & Authorize"

4. **Select Repository**:
   - You'll see a list of your repositories
   - **Click on "TalkingCanvas-ws"**

5. **Railway starts deploying!**
   - You'll see a new project dashboard
   - There's a card/box that says "talkingCanvas" or the service name
   - It will start building automatically

---

## Step 2A.3: Add PostgreSQL Database

> **You have 2 options**:
> - **Option 1**: Use Railway's built-in PostgreSQL (easier)
> - **Option 2**: Use your Supabase database from Part 1 (what we'll do)

**We'll use Option 2 (Supabase) because it's free forever**

**For now, just remember your Supabase DATABASE_URL from Part 1. We'll add it in the next step.**

---

## Step 2A.4: Configure Environment Variables

> **What are Environment Variables?** Think of them as settings that tell your application important information (like passwords, URLs) without putting them directly in the code.

1. **On your Railway project dashboard**, click on the **service** box (it shows your app name/GitHub repo)

2. **Find and click the "Variables" tab**
   - It's usually at the top of the service details
   - Might be next to "Settings", "Deployments", "Logs"

3. **Click "+ New Variable"** button

4. **Add variables ONE BY ONE**. For each variable:
   - Click **"Variable Name"** field and type the name
   - Click **"Value"** field and type/paste the value
   - Click **"Add"** or just press Enter

**Here are ALL the variables you need to add:**

---

### Variable 1: SPRING_PROFILES_ACTIVE

```
Variable Name: SPRING_PROFILES_ACTIVE
Value: prod
```

**What it does**: Tells Spring Boot to use production settings

---

### Variable 2: DATABASE_URL

```
Variable Name: DATABASE_URL
Value: [PASTE YOUR SUPABASE CONNECTION STRING FROM PART 1]
```

**Example**:
```
jdbc:postgresql://postgres:YourPassword123@db.abc123xyz.supabase.co:5432/postgres?sslmode=require
```

**What it does**: Tells Spring Boot where your database is

⚠️ **MAKE SURE**:
- Password is replaced with YOUR actual password
- String starts with `jdbc:postgresql://`
- String ends with `?sslmode=require`

---

### Variable 3: JWT_SECRET

```
Variable Name: JWT_SECRET
Value: TalkingCanvas2024SecureRandomStringMinimum64CharactersLongForJWT
```

**What it does**: Secret key for user authentication tokens

⚠️ **For production, generate a random one**:
- Go to: https://www.grc.com/passwords.htm
- Copy the "63 random alpha-numeric characters" password
- Use that instead of the example above

---

### Variable 4: FRONTEND_URL

```
Variable Name: FRONTEND_URL
Value: http://localhost:4200
```

**What it does**: Tells the backend which websites can access it (CORS)

⚠️ **NOTE**: We'll change this later when we deploy the Angular frontend. For now, use `http://localhost:4200`

---

### Variable 5: ADMIN_EMAIL

```
Variable Name: ADMIN_EMAIL
Value: pkumar.mail@gmail.com
```

**What it does**: Email address for the admin account

**Change this** to your email if you want

---

### Variable 6: ADMIN_PASSWORD

```
Variable Name: ADMIN_PASSWORD
Value: Admin@1998
```

**What it does**: Password for the admin account

⚠️ **IMPORTANT**: Change this to a strong password!

---

### Variable 7: ADMIN_NAME

```
Variable Name: ADMIN_NAME
Value: Praveen Kumar
```

**What it does**: Display name for admin

**Change this** to your name

---

5. **After adding all 7 variables**, Railway will automatically **redeploy** your application

6. **Wait 5-10 minutes** for the build to complete
   - You can watch progress in the "Deployments" tab
   - Look for a green checkmark ✅ when done

---

## Step 2A.5: Get Your Backend URL

1. **Click on your service** (if not already viewing it)

2. **Click the "Settings" tab**

3. **Scroll down** to the **"Networking"** or **"Domains"** section

4. **Click "Generate Domain"** button

5. **Railway gives you a URL** like:
   ```
   https://talkingcanvas-production-abc123.up.railway.app
   ```

6. **COPY THIS URL** and save it:
   ```
   MY BACKEND URL: https://talkingcanvas-production-abc123.up.railway.app
   ```

---

## Step 2A.6: Test Your Deployment

1. **Open a new browser tab**

2. **Paste your backend URL** and add `/api/actuator/health` to the end:
   ```
   https://talkingcanvas-production-abc123.up.railway.app/api/actuator/health
   ```

3. **Press Enter**

4. **You should see**:
   ```json
   {"status":"UP"}
   ```

✅ **If you see this, your backend is working!**

❌ **If you see an error**:
- Check "Deployments" tab for build errors
- Check "Logs" tab for runtime errors
- Make sure all environment variables are correct
- See [Troubleshooting](#part-4-troubleshooting) section

---

## Step 2A.7: Values to Save for Later

**Open your notepad and add:**

```
=== RAILWAY DEPLOYMENT INFO ===
Backend URL: https://talkingcanvas-production-abc123.up.railway.app
Health Check: https://talkingcanvas-production-abc123.up.railway.app/api/actuator/health

Environment Variables Set:
✅ SPRING_PROFILES_ACTIVE=prod
✅ DATABASE_URL=(your Supabase connection)
✅ JWT_SECRET=(your secret key)
✅ FRONTEND_URL=http://localhost:4200 (will update later!)
✅ ADMIN_EMAIL=pkumar.mail@gmail.com
✅ ADMIN_PASSWORD=Admin@1998
✅ ADMIN_NAME=Praveen Kumar
```

**YOU'RE DONE WITH RAILWAY! 🎉**

Skip to [Part 3: Connect Everything Together](#part-3-step-1-connect-backend-to-database)

---

# PART 2B: Deploy to Render

> **Best free tier** - Always free, 24/7 uptime (with sleep), no credit card needed.

## What You'll Get:
- ✅ Always free (no time limits)
- ✅ Runs 24/7 (spins down after 15 min of inactivity)
- ✅ Auto-wakes on first request (30-60 seconds)
- ✅ Can use built-in PostgreSQL (90 days free) or Supabase

---

## Step 2B.1: Create Render Account

1. **Go to**: `https://render.com`

2. **Click "Get Started"** or **"Sign Up"**

3. **Sign in with GitHub**:
   - Click **"GitHub"** button
   - Authorize Render when asked
   - You'll be redirected to Render dashboard

---

## Step 2B.2: Connect GitHub Repository

1. **On Render Dashboard**, click **"New +"** button (top-right)

2. **Select "Web Service"**

3. **Connect Repository**:
   - You'll see "Connect a repository"
   - If first time, click **"Connect GitHub"**
   - **Grant access** to repositories:
     - Select "Only select repositories"
     - Choose `TalkingCanvas-ws`
     - Click "Install"

4. **Select Repository**:
   - You'll see your repositories listed
   - Find **"TalkingCanvas-ws"**
   - **Click "Connect"** next to it

---

## Step 2B.3: Configure Web Service

**You'll see a long configuration form. Fill it in exactly:**

---

### Field: Name
```
talkingcanvas-backend
```
(Can be anything, but keep it simple)

---

### Field: Region
```
Select: [Choose closest to your database]
```
- If using Supabase in US-East, choose "Oregon (US-West)" or "Ohio (US-East)"
- If using Supabase in Europe, choose "Frankfurt (EU-Central)"

---

### Field: Branch
```
main
```
(This is your GitHub branch)

---

### Field: Root Directory
```
[LEAVE THIS EMPTY]
```
(Just leave the field blank)

---

### Field: Runtime
```
Select: Java
```
Click the dropdown and select "Java"

---

### Field: Build Command
```
./mvnw clean package -DskipTests
```

**Copy this EXACTLY** - it tells Render how to build your app

---

### Field: Start Command
```
java -Xmx512m -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar
```

**Copy this EXACTLY** - it tells Render how to start your app

---

### Field: Instance Type
**Scroll down** and look for "Instance Type" or "Plan"

```
Select: Free
```

Should show **"Free - $0/month"**

---

## Step 2B.4: Add Environment Variables

**Before clicking "Create Web Service", scroll down to find "Environment Variables" section**

Click **"Add Environment Variable"** button and add these **ONE BY ONE**:

### Variable 1:
```
Key: SPRING_PROFILES_ACTIVE
Value: prod
```

### Variable 2:
```
Key: DATABASE_URL
Value: [YOUR SUPABASE CONNECTION STRING from Part 1]
```
Example: `jdbc:postgresql://postgres:YourPass@db.xyz.supabase.co:5432/postgres?sslmode=require`

### Variable 3:
```
Key: JWT_SECRET
Value: TalkingCanvas2024SecureRandomStringMinimum64CharactersLongForJWT
```
(Or generate a random one from https://www.grc.com/passwords.htm)

### Variable 4:
```
Key: FRONTEND_URL
Value: http://localhost:4200
```
(We'll update this later)

### Variable 5:
```
Key: ADMIN_EMAIL
Value: pkumar.mail@gmail.com
```

### Variable 6:
```
Key: ADMIN_PASSWORD
Value: Admin@1998
```
(Change this to a strong password!)

### Variable 7:
```
Key: ADMIN_NAME
Value: Praveen Kumar
```

---

## Step 2B.5: Deploy!

1. **Scroll to the bottom**

2. **Click the big blue "Create Web Service" button**

3. **Render starts building**:
   - You'll see a **"Deploying..."** message
   - Build logs will appear in real-time
   - **FIRST BUILD TAKES 10-15 MINUTES** - be patient!
   - ☕ Grab a coffee/tea

4. **Wait for** the message: **"Your service is live 🎉"**
   - Status will change to **"Live"** (green)

---

## Step 2B.6: Get Your Backend URL

1. **At the top of the page**, you'll see your service URL:
   ```
   https://talkingcanvas-backend.onrender.com
   ```

2. **COPY THIS URL** and save it

---

## Step 2B.7: Test Your Deployment

1. **In a new browser tab**, go to:
   ```
   https://talkingcanvas-backend.onrender.com/api/actuator/health
   ```

2. **You should see**:
   ```json
   {"status":"UP"}
   ```

✅ **Success! Your backend is live!**

---

## Step 2B.8: Important Render Behavior

⚠️ **Your free Render service will "sleep" after 15 minutes** of no requests

**What this means**:
- First request after sleep: **30-60 seconds to wake up**
- Subsequent requests: **normal speed**
- This is normal for Render's free tier

**For real users**: They might experience the first page load slowly, then it's fast

---

## Step 2B.9: Values to Save

```
=== RENDER DEPLOYMENT INFO ===
Backend URL: https://talkingcanvas-backend.onrender.com
Health Check: https://talkingcanvas-backend.onrender.com/api/actuator/health

All environment variables set ✅
```

**YOU'RE DONE WITH RENDER! 🎉**

Skip to [Part 3](#part-3-step-1-connect-backend-to-database)

---

# PART 2C: Deploy to Koyeb

> Fast builds, great performance, free tier available. **Note**: No free database, so you MUST use Supabase/Aiven.

## Step 2C.1: Create Account

1. Go to `https://app.koyeb.com`
2. Click **"Sign up"**
3. Use GitHub to sign up
4. Verify email if needed

## Step 2C.2: Create New App

1. Click **"Create App"** (blue button)
2. Select **"GitHub"** as source
3. **Connect GitHub**: Authorize Koyeb
4. **Select Repository**: Click "TalkingCanvas-ws"

## Step 2C.3: Configure Application

**Builder**: Select **"Buildpack"**

**App Configuration**:
```
App name: talkingcanvas
Branch: main

Build command:
./mvnw clean package -DskipTests

Run command:
java -Xmx512m -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/talkingCanvas-0.0.1-SNAPSHOT.jar
```

## Step 2C.4: Add Environment Variables

Click **"Advanced"** → **"Environment Variables"**

Add these:
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=[Your Supabase connection string]
JWT_SECRET=TalkingCanvas2024SecureRandomStringMinimum64CharactersLongForJWT
FRONTEND_URL=http://localhost:4200
ADMIN_EMAIL=pkumar.mail@gmail.com
ADMIN_PASSWORD=Admin@1998
ADMIN_NAME=Praveen Kumar
PORT=8080
```

## Step 2C.5: Deploy

1. Click **"Deploy"**
2. Wait 10-15 minutes
3. Your URL will be: `https://talkingcanvas-[random].koyeb.app`

## Step 2C.6: Test

Visit: `https://talkingcanvas-[random].koyeb.app/api/actuator/health`

Should see: `{"status":"UP"}`

---

# PART 2D: Deploy to Google Cloud

> **For advanced users** - Requires command line, Docker, and Google Cloud SDK. $300 free credit.

**Prerequisites**:
- ✅ Command line/terminal knowledge
- ✅ Docker installed
- ✅ Google Cloud SDK installed
- ✅ Credit card (for verification, won't be charged)

**If you're a beginner, use Railway or Render instead!**

[See POSTGRESQL_DEPLOYMENT_GUIDE.md Section 6 for detailed Google Cloud instructions]

---

# PART 2E: Deploy to Oracle Cloud

> **For advanced users** - Requires SSH, command line, server management skills. Always free tier.

**Prerequisites**:
- ✅ SSH key pair
- ✅ Command line knowledge
- ✅ Linux server management
- ✅ Comfortable with terminal

**If you're a beginner, use Railway or Render instead!**

[See POSTGRESQL_DEPLOYMENT_GUIDE.md Section 7 for detailed Oracle Cloud instructions]

---

# PART 3: Connect Everything Together

Now that your backend is deployed, we need to connect:
1. Backend ↔️ Database (already done with DATABASE_URL!)
2. Frontend ↔️ Backend (we'll do this)
3. Test everything works

---

## PART 3 Step 1: Connect Backend to Database

✅ **Already Done!** When you added the `DATABASE_URL` environment variable, you connected your backend to your database.

**Verify it works**:
1. Go to your backend health check URL:
   ```
   https://[your-backend-url]/api/actuator/health
   ```
2. If it says `{"status":"UP"}`, database connection is working!

---

## PART 3 Step 2: Deploy Angular Frontend to Vercel

> **Vercel** - Best free hosting for Angular. Automatic, fast, simple.

### Step 3.2.1: Create Vercel Account

1. Go to `https://vercel.com`
2. Click **"Sign Up"**
3. **Click "Continue with GitHub"**
4. Authorize Vercel

### Step 3.2.2: Import Project

1. On Vercel Dashboard, click **"Add New..."** → **"Project"**
2. Find your **"TalkingCanvas-ws"** repository
3. Click **"Import"**

### Step 3.2.3: Configure Build Settings

**Framework Preset**: Angular
```
Root Directory: client
```
Click the **"Edit"** button next to Root Directory and type `client`

**Build Settings**:
```
Build Command: npm run build
Output Directory: dist/client/browser
Install Command: npm install
```

(These should auto-fill, but verify them)

### Step 3.2.4: Deploy

1. Click **"Deploy"** (blue button)
2. Wait 3-5 minutes
3. You'll see **"Congratulations!"** when done

### Step 3.2.5: Get Your Frontend URL

Vercel shows your deployment URL:
```
https://talking-canvas-ws-[random].vercel.app
```

**Or set up custom domain** (optional):
- Click "View Domains"
- Add your own domain (e.g., `talkingcanvas.com`)

**COPY YOUR FRONTEND URL** - you need it for the next step!

---

## PART 3 Step 3: Connect Frontend to Backend

> **Two things to update**:
> 1. Tell frontend where backend is
> 2. Tell backend to allow frontend

### Step 3.3.1: Update Frontend API URL

**ON YOUR LOCAL COMPUTER:**

1. **Open file**: `client/src/environments/environment.prod.ts`

2. **Change the apiUrl** to your backend URL:

```typescript
export const environment = {
  production: true,
  // Update this with YOUR backend URL from Railway/Render
  apiUrl: 'https://your-backend-url.railway.app/api'
};
```

**Example**:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://talkingcanvas-production.up.railway.app/api'
};
```

3. **Save the file**

4. **Commit and push** to GitHub:
```bash
git add client/src/environments/environment.prod.ts
git commit -m "Update production API URL"
git push origin main
```

5. **Vercel will auto-deploy** the update (wait 2-3 minutes)

---

### Step 3.3.2: Update Backend CORS Settings

**ON YOUR DEPLOYMENT PLATFORM (Railway/Render/etc.):**

1. **Go to your backend service**
2. **Click "Variables" or "Environment Variables"**
3. **Find the variable**: `FRONTEND_URL`
4. **Change its value** to your Vercel URL:

```
OLD VALUE: http://localhost:4200
NEW VALUE: https://talking-canvas-ws-[random].vercel.app
```

⚠️ **IMPORTANT**:
- Use `https://` (not `http://`)
- NO trailing slash at the end
- Must be EXACT URL from Vercel

5. **Save/Update** the variable

6. **Your backend will auto-redeploy** (wait 2-3 minutes)

---

## PART 3 Step 4: Final Testing

### Test 1: Health Check

Visit: `https://your-backend-url/api/actuator/health`

✅ Should see: `{"status":"UP"}`

---

### Test 2: Frontend Loads

Visit: `https://your-frontend-url.vercel.app`

✅ Should see: Your TalkingCanvas home page

---

### Test 3: User Registration

1. Click **"Register"** or **"Sign Up"**
2. Fill in the form
3. Click **"Submit"**

✅ Should see: Success message or redirect to login

❌ If error: Check browser console (F12) for CORS errors

---

### Test 4: User Login

1. Login with the account you just created
2. Should get redirected after login

✅ Should see: Your paintings or user dashboard

---

### Test 5: Admin Login

1. Go to: `https://your-frontend-url.vercel.app/admin` (or wherever admin login is)
2. Use credentials:
   - Email: Your `ADMIN_EMAIL` value
   - Password: Your `ADMIN_PASSWORD` value
3. Click Login

✅ Should see: Admin dashboard

---

### Test 6: Paintings Display

1. Navigate to paintings page
2. Should load paintings from database

✅ Should see: List of paintings

---

## 🎉 CONGRATULATIONS! Everything is Connected!

If all 6 tests passed, you have:
- ✅ Backend running on Railway/Render
- ✅ Frontend running on Vercel
- ✅ Database running on Supabase
- ✅ All three connected and working
- ✅ Application accessible worldwide!

---

# PART 4: Values to Save (Don't Lose These!)

**Print this or save in a secure location:**

```
============================================
TALKINGCANVAS DEPLOYMENT INFORMATION
Created: [today's date]
============================================

DATABASE (Supabase):
--------------------------------------------
Host: db.[xxx].supabase.co
Port: 5432
Database: postgres
Username: postgres
Password: [your database password]
Full URL: jdbc:postgresql://postgres:[password]@db.[xxx].supabase.co:5432/postgres?sslmode=require


BACKEND (Railway/Render/Koyeb):
--------------------------------------------
Platform: [Railway/Render/Koyeb]
URL: https://[your-backend].railway.app (or .onrender.com or .koyeb.app)
Health Check: https://[your-backend].railway.app/api/actuator/health


FRONTEND (Vercel):
--------------------------------------------
URL: https://[your-frontend].vercel.app


ADMIN CREDENTIALS:
--------------------------------------------
Email: pkumar.mail@gmail.com (or whatever you set)
Password: Admin@1998 (or whatever you set)
Name: Praveen Kumar


ENVIRONMENT VARIABLES:
--------------------------------------------
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=[your full database connection string]
JWT_SECRET=[your secret key - 64+ characters]
FRONTEND_URL=https://[your-frontend].vercel.app
ADMIN_EMAIL=pkumar.mail@gmail.com
ADMIN_PASSWORD=Admin@1998
ADMIN_NAME=Praveen Kumar


IMPORTANT URLS TO BOOKMARK:
--------------------------------------------
Application: https://[your-frontend].vercel.app
Admin Panel: https://[your-frontend].vercel.app/admin
Backend Health: https://[your-backend].railway.app/api/actuator/health
Backend API Docs: https://[your-backend].railway.app/api/swagger-ui.html
```

---

# PART 4: Environment Variables Explained

> **What each variable does in simple terms**

| Variable | What It Does | Can I Change It? |
|----------|--------------|------------------|
| `SPRING_PROFILES_ACTIVE` | Tells Spring Boot to use production mode instead of development mode | ❌ No, keep it as `prod` |
| `DATABASE_URL` | The "address" of your database so Spring Boot knows where to store data | ⚠️ Only if you change databases |
| `JWT_SECRET` | Secret code used to create login tokens (like a master password) | ✅ Yes, should be random 64+ chars |
| `FRONTEND_URL` | Tells backend which website is allowed to access it (security feature) | ✅ Yes, must match your Vercel URL |
| `ADMIN_EMAIL` | Email address for the admin account that gets created automatically | ✅ Yes, use your email |
| `ADMIN_PASSWORD` | Password for the admin account | ✅ Yes, PLEASE change this! |
| `ADMIN_NAME` | Display name for admin | ✅ Yes, use your name |
| `PORT` | What port the server runs on (Koyeb only) | ❌ No, keep as 8080 |

---

# PART 4: Troubleshooting

## Problem: Can't See "Generate Domain" on Railway

**Solution**:
1. Make sure deployment succeeded (green checkmark)
2. Go to Settings tab
3. Scroll all the way down to "Networking"
4. Click "Generate Domain"

---

## Problem: Backend Health Check Shows Error

**Symptoms**: `/api/actuator/health` shows error or app crash

**Solutions**:
1. **Check Logs**:
   - Railway: Click "Deployments" → Latest → "View Logs"
   - Render: Click "Logs" tab
   - Look for red ERROR messages

2. **Common Issues**:
   - **Database connection failed**: Check `DATABASE_URL` is correct
   - **Java version mismatch**: Make sure `system.properties` file exists in your repo
   - **Out of memory**: Make sure start command includes `-Xmx512m`

3. **Verify Environment Variables**:
   - Go to Variables tab
   - Check all 7 variables are set
   - Check for typos in variable names

---

## Problem: CORS Error in Browser Console

**Symptoms**: 
```
Access to XMLHttpRequest at 'https://backend.com/api/...' from origin 'https://frontend.com' has been blocked by CORS policy
```

**Solutions**:
1. **Check FRONTEND_URL**:
   - Must exactly match your Vercel URL
   - Include `https://`
   - No trailing `/`

2. **Update Backend**:
   - Change `FRONTEND_URL` in environment variables
   - Save/redeploy
   - Wait 2-3 minutes

3. **Clear Browser Cache**:
   - Press Ctrl+Shift+Delete
   - Clear cached images and files
   - Refresh page

---

## Problem: Frontend Shows "Cannot GET /api/..."

**Symptoms**: Frontend loads but can't connect to backend

**Solutions**:
1. **Check environment.prod.ts**:
   - Open `client/src/environments/environment.prod.ts`
   - Verify `apiUrl` points to your backend
   - Must end with `/api` not just domain

2. **Example**:
   ```typescript
   // WRONG
   apiUrl: 'https://my-backend.railway.app'
   
   // CORRECT
   apiUrl: 'https://my-backend.railway.app/api'
   ```

3. **Push to GitHub**:
   - Commit changes
   - Push to GitHub
   - Vercel auto-deploys in 2-3 min

---

## Problem: Render Service Keeps Sleeping

**This is normal behavior!**

**What happens**:
- After 15 min of no requests, Render spins down your service (saves resources)
- First request after: 30-60 second delay
- After that: Normal speed

**Solutions**:
1. **Upgrade to paid plan** ($7/month for always-on)
2. **Use a uptime monitor** (free services that ping your app every 5 min):
   - https://uptimerobot.com (free, monitors every 5 min)
   - https://cron-job.org (free, can ping every minute)

**How to set up UptimeRobot**:
1. Sign up at uptimerobot.com
2. Add New Monitor
3. Monitor Type: HTTP(s)
4. Friendly Name: TalkingCanvas Backend
5. URL: Your health check URL (`/api/actuator/health`)
6. Monitoring Interval: 5 minutes
7. Save - it will ping your app every 5 min, keeping it awake!

---

## Problem: Build Failed on Railway/Render

**Symptoms**: Deployment shows "Build failed" or red X

**Solutions**:
1. **Check Build Logs**:
   - Look for the first ERROR message
   - Common errors:
     - "Cannot find mvnw" → Make sure `.mvn/wrapper` folder is in your repo
     - "Java version" → Make sure `system.properties` exists
     - "Out of memory" → Normal, retry deployment

2. **Push system.properties**:
   ```bash
   # Make sure this file exists in your repo root
   # File: system.properties
   java.runtime.version=21
   maven.version=3.9.9
   ```

3. **Retry Build**:
   - Railway: Click "Redeploy"
   - Render: Click "Manual Deploy" → "Deploy latest commit"

---

## Problem: Can't Login as Admin

**Symptoms**: Admin login fails, shows "Invalid credentials"

**Solutions**:
1. **Check Environment Variables**:
   - Verify `ADMIN_EMAIL` and `ADMIN_PASSWORD`
   - Use exact values you set

2. **Check Database**:
   - If you recently changed `DATABASE_URL`, admin account might not exist in new database
   - Solution: Register a new regular user, then manually promote to admin in database

3. **Reset Admin**:
   - Delete database tables
   - Redeploy backend (will recreate admin from env vars)

---

## Need More Help?

1. **Check Logs First**:
   - Railway: Deployments → Logs
   - Render: Logs tab
   - Look for ERROR or WARN messages

2. **Check Health Endpoint**:
   - If `/api/actuator/health` shows "UP", backend is fine
   - If it shows error or timeout, there's a backend problem

3. **Check Browser Console**:
   - Press F12 in browser
   - Click "Console" tab
   - Look for red errors
   - Common: CORS errors, 404 errors, Network errors

4. **Platform Status**:
   - Railway: https://railway.app/status
   - Render: https://status.render.com
   - Vercel: https://vercel-status.com
   - Sometimes platforms have outages!

---

## Quick Checklist If Nothing Works

```
[ ] Code is pushed to GitHub (latest version)
[ ] system.properties file exists in repo root
[ ] .mvn/wrapper/ folder exists in repo
[ ] All 7 environment variables are set correctly
[ ] DATABASE_URL starts with jdbc:postgresql://
[ ] DATABASE_URL ends with ?sslmode=require
[ ] DATABASE_URL has correct password (no [YOUR-PASSWORD] placeholder)
[ ] FRONTEND_URL matches Vercel URL exactly
[ ] environment.prod.ts has correct backend URL
[ ] Backend health check returns {"status":"UP"}
[ ] Build succeeded (green checkmark)
[ ] Service is running (not stopped)
```

If all checked and still not working:
1. Delete the service and redeploy from scratch
2. Try a different platform (Railway → Render or vice versa)
3. Double-check your database is still active (Supabase dashboard)

---

# 🎯 Final Notes

## Platform Comparison

| Platform | Best For | Free Tier | Difficulty |
|----------|----------|-----------|------------|
| **Railway** | Quick testing | $5/month credit | ⭐ Easiest |
| **Render** | Production | Always free (with sleep) | ⭐⭐ Easy |
| **Koyeb** | Fast performance | Limited free | ⭐⭐ Easy |
| **Google Cloud** | Enterprise/scale | $300 credit | ⭐⭐⭐⭐⭐ Advanced |
| **Oracle** | Always-free VM | Forever free | ⭐⭐⭐⭐⭐ Advanced |

## Recommendation

**For Beginners**:
1. Try Railway first (easiest, fastest)
2. If Railway credit runs out, switch to Render

**For Production** (real users):
- Use Render (always free, stable)
- Set up UptimeRobot to prevent sleep

**For Learning/Testing**:
- Use Railway (fastest feedback)

---

## What's Next?

Now that everything is deployed:

1. ✅ **Change admin password** on first login
2. ✅ **Add paintings** through admin panel
3. ✅ **Test all features**:
   - User registration
   - Painting browsing
   - Cart functions
   - Checkout
   - Admin features
4. ✅ **Set up custom domain** (optional):
   - Buy domain from Namecheap/Google Domains
   - Point to Vercel
   - Update FRONTEND_URL
5. ✅ **Set up email** (optional):
   - Get Gmail app password
   - Add MAIL_USERNAME and MAIL_PASSWORD env vars
   - Test password reset emails

---

**Congratulations! You've successfully deployed a full-stack application! 🎉**

**Questions?** Check the troubleshooting section or review the logs on your deployment platform.

---

**Last Updated**: 2025-11-29
**Guide Version**: 1.0 (Beginner-Friendly)
**Tested On**: Railway, Render, Vercel (November 2025)
