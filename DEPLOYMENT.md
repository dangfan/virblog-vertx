# Deployment Guide

This guide explains how to deploy the VirBlog application using Docker.

## Architecture

The deployment consists of a single containerized service:

- **app** - Kotlin/Vert.x backend application that handles all requests, serves static files, and renders blog pages

## Prerequisites

- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)
- At least 1GB of available RAM
- Port 8080 available

## Quick Start

### 1. Build and Start Services

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f app
```

### 2. Access the Application

- **Blog**: http://localhost:8080
- **Admin Panel**: http://localhost:8080/admin
- **API**: http://localhost:8080/api

## Management Commands

### Stop Services

```bash
docker-compose stop
```

### Restart Services

```bash
docker-compose restart
```

### Rebuild After Code Changes

```bash
# Rebuild and restart
docker-compose up -d --build

# Or rebuild specific service
docker-compose up -d --build app
```

### View Service Status

```bash
docker-compose ps
```

### Remove All Services

```bash
docker-compose down

# Remove services and volumes
docker-compose down -v
```

## Configuration

### Override Application Settings

To override application settings, create an `application.conf` file in the project root:

```bash
# Copy the example file
cp application.conf.example application.conf

# Edit with your settings
nano application.conf
```

Example `application.conf`:

```properties
# Server port
port=8080

# Database path
db.path=data/virblog.db

# JWT secret (CHANGE IN PRODUCTION!)
jwt.key=your-secure-secret-key

# Available locales
locales=zh-Hans,zh-Hant,en

# Font directories
font.input=fonts/input
font.output=fonts/output
```

The file is automatically mounted into the container and will override the default settings.

### Environment Variables

You can also customize Java options:

```yaml
# In docker-compose.yml
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m
```

### Database Persistence

The SQLite database is persisted in the `./data` directory, which is mounted as a volume. This ensures data survives container restarts.

### Static Files

Static files are served by the Vert.x application from:
- `/admin` → `src/main/resources/webroot/admin`
- `/assets` → `src/main/resources/webroot/assets`
- `/fonts` → `fonts/` directory

## Production Deployment

### 1. Use a Reverse Proxy for HTTPS

For production, it's recommended to use a reverse proxy (nginx, Caddy, or Traefik) in front of the application to handle HTTPS:

```yaml
# Example: Add nginx as reverse proxy
services:
  app:
    # ... existing config
    expose:
      - "8080"
  
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx-proxy.conf:/etc/nginx/nginx.conf:ro
      - /etc/letsencrypt:/etc/nginx/ssl:ro
```

Or use Caddy for automatic HTTPS:

```yaml
  caddy:
    image: caddy:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy_data:/data
```

### 2. Security Hardening

- Change default ports if needed
- Set up firewall rules
- Use strong JWT secrets (configure in application.conf)
- Enable rate limiting in nginx
- Regular security updates: `docker-compose pull && docker-compose up -d`

### 3. Monitoring

Check service health:

```bash
# Check app health
curl http://localhost:8080/api/health

# Check container status
docker-compose ps
```

### 4. Backup

Backup the database regularly:

```bash
# Create backup
cp data/virblog.db data/virblog.db.backup-$(date +%Y%m%d)

# Or use a cron job
0 2 * * * cp /path/to/data/virblog.db /path/to/backups/virblog.db.$(date +\%Y\%m\%d)
```

## Troubleshooting

### Application Won't Start

```bash
# Check logs
docker-compose logs app

# Check if port is already in use
lsof -i :8080
```

### Port Already in Use

```bash
# Check what's using port 8080
lsof -i :8080

# Or change the port in docker-compose.yml
ports:
  - "9090:8080"  # Use port 9090 instead
```

### Static Files Not Loading

```bash
# Verify file permissions
ls -la src/main/resources/webroot/

# Check if files are in the container
docker-compose exec app ls -la /app/
```

### Database Issues

```bash
# Check database file
ls -la data/

# Check database permissions
ls -la data/virblog.db
```

## Updating the Application

1. Pull latest code
2. Rebuild containers: `docker-compose up -d --build`
3. Check logs: `docker-compose logs -f`

## Performance Tuning

### Java Heap Size

Adjust in `docker-compose.yml`:

```yaml
environment:
  - JAVA_OPTS=-Xmx2g -Xms1g
```

### Vert.x Configuration

Adjust worker threads and other settings in `src/main/resources/application.conf`.

### Database Optimization

For better SQLite performance, ensure the database file is on fast storage (SSD).

## Support

For issues or questions, check the application logs first:

```bash
docker-compose logs -f
```
