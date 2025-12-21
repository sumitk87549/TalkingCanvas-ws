#!/bin/bash

# TalkingCanvas Local Deployment Update Script

echo "ðŸš€ Starting Deployment Update..."

# 1. Pull the latest code from Git
echo "ðŸ“¥ Pulling latest changes from Git..."
git pull origin main

# 2. Rebuild and restart the backend container
echo "ðŸ”„ Rebuilding and restarting Backend..."
docker-compose up -d --build backend

# 3. Clean up unused images to save disk space
echo "ðŸ§¹ Cleaning up old Docker images..."
docker image prune -f

echo "âœ… Deployment Update Complete! Backend is running."
echo "ðŸ›¡ï¸  Check status with: docker-compose logs -f backend"
