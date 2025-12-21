# ðŸ ¡ Zero-Cost Local Deployment Guide (High Performance)

This guide explains how to host the **TalkingCanvas Backend** on your **Lenovo V15 G4** laptop (Linux) for **free**, while achieving excellent performance for users in India.

**Strategy:**
1.  **Backend & DB**: Run on your laptop using Docker (Native speed, no shared cloud limits).
2.  **Network**: Expose securely to the internet using **Cloudflare Tunnel** (No port forwarding needed, works with 5G).
3.  **Frontend**: Hosted on Vercel (as you currently have), pointing to your laptop.

---

## âœ… Prerequisites

Before starting, ensure your laptop is ready.

### 1. Install Docker & Docker Compose
If you haven't already:
```bash
# Update package index
sudo apt-get update

# Install Docker
sudo apt-get install -y docker.io docker-compose

# Start Docker and enable it to run on boot
sudo systemctl enable --now docker

# Add your user to the docker group (to run without sudo)
sudo usermod -aG docker $USER
newgrp docker
```

---

## ðŸš€ Step 1: Start the Backend

I have created a script `deploy.sh` in your project folder to make this one-click.

1.  Open a terminal in your project folder (`/home/sumit/Desktop/tc-git/TalkingCanvas-ws`).
2.  Run the deployment script:
    ```bash
    ./deploy.sh
    ```
    *This will pull the latest code, build the backend, and start the Database and API.*

3.  **Verify it's running**:
    ```bash
    docker-compose logs -f backend
    ```
    Wait until you see `Started TalkingCanvasApplication in ... seconds`.

---

## ðŸŒ  Step 2: Expose to the Internet (Cloudflare Tunnel)

We will use Cloudflare Tunnel to check `localhost:8088` and give you a public `https` URL.

### 1. Install `cloudflared`
```bash
# Download the latest release
wget -q https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb

# Install it
sudo dpkg -i cloudflared-linux-amd64.deb
```

### 2. Start the Tunnel
Run this command to start a temporary "Quick Tunnel":
```bash
cloudflared tunnel --url http://localhost:8088
```

**Output:**
Look for a line that looks like this:
`+--------------------------------------------------------------------------------------------+`
`|  Your quick tunnel has been created! Visit it at (below):                                  |`
`|  https://some-random-name.trycloudflare.com                                                |`
`+--------------------------------------------------------------------------------------------+`

**COPY THAT URL.** This is your Backend URL. (e.g., `https://cat-dance-piano.trycloudflare.com`)

> **Note:** This URL changes every time you restart the tunnel. For a permanent URL, you need a free Cloudflare account and a domain name (approx $10/year). Given your "Cheapest" requirement, we are using the free random one for now.

---

## ðŸ”— Step 3: Connect Frontend (Vercel)

Now tell your Vercel frontend where the backend is.

1.  Go to your **Vercel Dashboard** > Select Project > **Settings** > **Environment Variables**.
2.  Add/Edit the variable:
    *   **Key**: `API_URL` (or `apiUrl` depending on your build script, but usually you need to update the source file if you don't have build replacement set up).
    *   **Safest Way**: Modify `client/src/environments/environment.prod.ts`:
        *   Open `client/src/environments/environment.prod.ts`
        *   Change `apiUrl: '/api'` to `apiUrl: 'https://your-tunnel-url.trycloudflare.com'`
        *   Commit and Push:
            ```bash
            git add .
            git commit -m "chore: point production api to local tunnel"
            git push origin main
            ```
            *Vercel will automatically redeploy.*

---

## ðŸ¤– CI/CD (How to Update)

When you make changes to the code:

1.  **Push changes** to GitHub from your dev machine.
2.  **On your Laptop (Server):**
    ```bash
    cd ~/Desktop/tc-git/TalkingCanvas-ws
    ./deploy.sh
    ```
    *This updates the backend.*
3.  **Frontend**: Vercel updates automatically when you push.

---

## âš ï¸  Important Notes for "Smooth" Operation

1.  **Power**: Your laptop must be **ON** and **Connected to Internet** for the site to work. Configure power settings to "Do not sleep when lid is closed" if you want to close the lid.
2.  **Stability**: The 5G connection might fluctuate. If the tunnel disconnects, just re-run the `cloudflared` command.
3.  **Permanent URL**: If this becomes annoying, for ~â‚¹800/year you can buy a domain and have a permanent URL. But for free, the random URL works if you just update the frontend.
