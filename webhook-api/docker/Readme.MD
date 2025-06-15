# 🗄️ MongoDB Setup Guide

This guide covers the MongoDB setup and management for the Webhook project using Docker and 
convenient Make commands.

## 📋 Table of Contents

- [🛠️ Prerequisites](#️-prerequisites)
- [⚡ Quick Start](#-quick-start)
- [🐳 Docker Management](#-docker-management)
- [🔧 Configuration](#-configuration)
- [👥 Default Users](#-default-users)
- [🧪 Database Operations](#-database-operations)
- [🚨 Troubleshooting](#-troubleshooting)

## 🛠️ Prerequisites

- **Docker** and **Docker Compose** installed
- **Make** utility (usually pre-installed on Linux/macOS)
- Basic terminal/command line knowledge

## ⚡ Quick Start

### 1. Project Structure

```
webhook-api/
├── .env                     # Environment variables (root)
├── docker/                  # MongoDB Docker setup
│   ├── docker-compose.yml   # MongoDB & Mongo Express services
│   ├── init-mongo.js        # Database initialization script
│   ├── Makefile             # Management commands
│   └── logs/                # MongoDB logs directory
└── scripts/
    └── start-mongo.sh       # Quick startup script
```

### 2. Quick Setup

```bash
# Option 1: Use the automated script (recommended)
./scripts/start-mongo.sh

# Option 2: Manual setup
cd docker
make up
```

### 3. Access Points

- **Mongo Express UI**: http://localhost:8081
    - Username: `admin`
    - Password: `admin123`
- **MongoDB Direct**: `mongodb://noreyni:Password12345@localhost:27017/webhook-db`

## 🐳 Docker Management

All MongoDB management is done through convenient Make commands in the `docker/` directory.

### Basic Commands

```bash
cd docker

# Start MongoDB and Mongo Express
make up

# Stop all services
make down

# Restart services
make restart

# View service status
make status

# Clean everything (⚠️ removes all data)
make clean
```

### Monitoring Commands

```bash
# View all service logs
make logs

# View only MongoDB logs
make mongo-logs

# Open MongoDB shell
make mongo-shell

# Show help with all available commands
make help
```

### Advanced Operations

```bash
# Create database backup
make backup

# Restore from backup (specify backup directory)
make restore BACKUP_DIR=./backups/20240115_143000

# Create logs directory (if needed)
make create-logs-dir
```

## 🔧 Configuration

### Environment Variables

Configuration is managed through the `.env` file in the project root:

```bash
# MongoDB Configuration
MONGO_USER=noreyni                    # Database username
MONGO_PASSWORD=Password12345          # Database password  
MONGO_HOST=localhost                  # Database host
MONGO_PORT=27017                      # Database port
MONGO_DATABASE=webhook-db              # Database name
```

### Docker Compose Services

The setup includes two services:

#### 1. MongoDB Database
- **Image**: `mongo:7.0`
- **Port**: `27017`
- **Features**:
    - Data persistence with named volumes
    - Health checks for reliability
    - Automatic initialization with sample data
    - Log output to `docker/logs/mongodb/`

#### 2. Mongo Express (Web UI)
- **Image**: `mongo-express:1.0.2`
- **Port**: `8081`
- **Features**:
    - Web-based MongoDB administration
    - Authentication enabled
    - Depends on MongoDB health

### Database Initialization

The `init-mongo.js` script automatically:

✅ Creates the `webhook-db` database  
✅ Sets up performance indexes  
✅ Creates 5 sample users with different roles  
✅ Provides database statistics

**Indexes Created:**
- `idx_users_email_unique` - Unique email constraint
- `idx_users_role` - Role-based queries
- `idx_users_active` - Active/inactive filtering
- `idx_users_created_at` - Creation date sorting
- `idx_users_updated_at` - Update date sorting
- `idx_users_role_active` - Compound index for common queries
- `idx_users_name` - Name-based search

## 👥 Default Users

After initialization, you'll have these test users (all with password `admin123`):

| Email | Role | Status | Description |
|-------|------|--------|-------------|
| `admin@noreyni.sn` | ADMIN | ✅ Active | System administrator |
| `sophie.diallo@noreyni.sn` | ADMIN | ✅ Active | Admin user |
| `marie.dubois@noreyni.sn` | MEMBER | ✅ Active | Regular member |
| `jean.martin@noreyni.sn` | MEMBER | ✅ Active | Regular member |
| `amadou.ba@noreyni.sn` | MEMBER | ❌ Inactive | Inactive user (for testing) |

### Document Structure

MongoDB documents match the Java entity structure:

```json
{
  "_id": ObjectId("..."),
  "first_name": "Marie",              // Maps to firstName in Java
  "last_name": "Dubois",              // Maps to lastName in Java  
  "email": "marie.dubois@noreyni.sn",
  "password": "$2a$10$...",            // Bcrypt hashed
  "role": "MEMBER",                    // UserRole enum value
  "active": true,
  "createdBy": "system",               // Audit field
  "createdAt": ISODate("..."),         // Audit field
  "updatedBy": "system",               // Audit field
  "updatedAt": ISODate("...")          // Audit field
}
```

## 🧪 Database Operations

### Connecting to MongoDB

**MongoDB Shell:**
```bash
# Through Docker (recommended)
cd docker
make mongo-shell

# Direct connection
mongosh "mongodb://noreyni:Password12345@localhost:27017/webhook-db?authSource=admin"
```

**From Your Application:**
```yaml
# application.yml
quarkus:
  mongodb:
    connection-string: mongodb://noreyni:Password12345@localhost:27017/webhook-db?authSource=admin
    database: webhook-db
```

### Common MongoDB Operations

```javascript
// Connect to database
use webhook-db

// Find all users
db.users.find()

// Find admin users
db.users.find({role: "ADMIN"})

// Find active users
db.users.find({active: true})

// Count users by role
db.users.aggregate([
  {$group: {_id: "$role", count: {$sum: 1}}}
])

// Create index (if needed)
db.users.createIndex({email: 1}, {unique: true})

// Check indexes
db.users.getIndexes()
```

### Backup & Restore

**Create Backup:**
```bash
cd docker
make backup
# Creates backup in ./backups/YYYYMMDD_HHMMSS/
```

**Restore Backup:**
```bash
cd docker
make restore BACKUP_DIR=./backups/20240115_143000
```

**Manual Backup/Restore:**
```bash
# Manual backup
docker exec noreyni-mongodb mongodump \
  --username noreyni \
  --password Password12345 \
  --authenticationDatabase admin \
  --db webhook-db \
  --out /tmp/backup

# Manual restore  
docker exec noreyni-mongodb mongorestore \
  --username noreyni \
  --password Password12345 \
  --authenticationDatabase admin \
  --db webhook-db \
  --drop /tmp/restore/webhook-db
```

## 🚨 Troubleshooting

### Common Issues & Solutions

#### 🔴 MongoDB Won't Start

```bash
# Check if port 27017 is in use
lsof -i :27017

# View MongoDB logs
cd docker && make mongo-logs

# Clean and restart
cd docker && make clean && make up
```

#### 🔴 Cannot Connect to MongoDB

```bash
# Check service status
cd docker && make status

# Verify environment variables
cat ../.env

# Test connection
cd docker && make mongo-shell
```

#### 🔴 Mongo Express Not Accessible

```bash
# Check if port 8081 is available
lsof -i :8081

# View Mongo Express logs
cd docker && docker-compose logs mongo-express

# Restart services
cd docker && make restart
```

#### 🔴 Permission Denied Errors

```bash
# Make scripts executable
chmod +x scripts/*.sh

# Fix Docker permissions (Linux)
sudo usermod -aG docker $USER
# Then logout and login again
```

#### 🔴 Data Persistence Issues

```bash
# Check Docker volumes
docker volume ls | grep noreyni

# Remove volumes to start fresh (⚠️ DELETES ALL DATA)
cd docker && make clean
```

### Useful Debugging Commands

```bash
# Show Docker containers
docker ps -a

# Show Docker volumes  
docker volume ls

# Show Docker networks
docker network ls

# Inspect MongoDB container
docker inspect noreyni-mongodb

# Show environment variables
cd docker && docker-compose config

# Test MongoDB connectivity
docker exec noreyni-mongodb mongosh --eval "db.adminCommand('ping')"
```

### Log Locations

- **Docker Compose Logs**: `docker-compose logs`
- **MongoDB Logs**: `docker/logs/mongodb/mongod.log`
- **Application Logs**: Your Quarkus application console

### Reset Everything

If you need to start completely fresh:

```bash
cd docker

# Stop services and remove all data
make clean

# Remove Docker volumes
docker volume rm noreyni_mongodb_data noreyni_mongodb_config

# Start fresh
make up
```

## 🔧 Customization

### Changing Database Configuration

1. **Update `.env` file:**
   ```bash
   MONGO_DATABASE=your_database_name
   MONGO_PASSWORD=your_secure_password
   ```

2. **Restart services:**
   ```bash
   cd docker
   make down && make up
   ```

### Adding Custom Initialization

Edit `docker/init-mongo.js` to add:
- Additional sample data
- Custom indexes
- Database setup scripts
- User roles and permissions

### Production Considerations

For production use:

1. **Security**: Change default passwords
2. **Networking**: Use internal Docker networks
3. **Persistence**: Use external volume mounts
4. **Monitoring**: Add MongoDB monitoring tools
5. **Backup**: Set up automated backup schedules

---

**🎯 Quick Reference:**

```bash
# Start MongoDB
cd docker && make up

# Access UI  
open http://localhost:8081

# MongoDB Shell
cd docker && make mongo-shell

# View Logs
cd docker && make logs

# Stop Everything
cd docker && make down
```