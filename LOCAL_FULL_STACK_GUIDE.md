# 🏡 Local Full Stack Deployment Guide

This guide explains how to run the **entire** TalkingCanvas application (Frontend + Backend + Database) on your local laptop.

> **Why do this?**
> *   🚀 **Fast**: No internet latency. Everything is on your machine.
> *   🔒 **Private**: Data stays on your laptop.
> *   💸 **Free**: No cloud costs or tunnel issues.

---

## ✅ Prerequisites

1.  **Docker & Docker Compose**: (You already have this installed)
2.  **Code**: You need the latest code on your laptop.

---

## 🚀 Step 1: Configuration

To make the frontend talk directly to your local backend (bypassing the internet), update one file:

**Edit:** `client/src/environments/environment.prod.ts`
```typescript
export const environment = {
  production: true,
  apiUrl: '/api' 
};
```
*Note: Setting `apiUrl` to `/api` tells the frontend to use the local Nginx proxy instead of checking Cloudflare.*

---

## 🏃 Step 2: Start Everything

Run this single command in your project folder (`~/Desktop/TalkingCanvas-ws`):

```bash
docker-compose up -d --build
```

**What happens?**
1.  **PostgreSQL** starts (Database).
2.  **Backend** compiles and starts (API).
3.  **Frontend** compiles (Angular) and starts (Web Server).

*The first time might take 5-10 minutes to build the frontend. Be patient!*

---

## 🌐 Step 3: Access the App

Open your browser and go to:
👉 **[http://localhost](http://localhost)**

*   **Frontend**: Loads from your local Docker container.
*   **Backend API**: Requests go automatically to your local backend.
*   **No Internet Required!** (Once built)

---

## 🛠️ Common Operations

**Stop everything:**
```bash
docker-compose down
```

**View Logs (Frontend):**
```bash
docker-compose logs -f frontend
```

**View Logs (Backend):**
```bash
docker-compose logs -f backend
```

**Update Code:**
1.  `git pull` (or copy new files)
2.  `docker-compose up -d --build` (Rebuilds everything)
