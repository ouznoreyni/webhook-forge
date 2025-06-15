.PHONY: up down logs clean restart mongo-logs mongo-shell status backup restore help

# Default environment file
ENV_FILE := ../.env

# Load environment variables
ifneq (,$(wildcard $(ENV_FILE)))
    include $(ENV_FILE)
    export
endif

# Help command
help:
	@echo "🐳 Webhook MongoDB Docker Commands:"
	@echo ""
	@echo "  make up          - Start MongoDB and Mongo Express"
	@echo "  make down        - Stop all services"
	@echo "  make restart     - Restart all services"
	@echo "  make logs        - View all logs"
	@echo "  make mongo-logs  - View MongoDB logs only"
	@echo "  make mongo-shell - Open MongoDB shell"
	@echo "  make status      - Show service status"
	@echo "  make backup      - Create database backup"
	@echo "  make clean       - Remove containers and volumes"
	@echo ""

# Start all services
up:
	@echo "🚀 Starting MongoDB and Mongo Express..."
	@echo "📍 Using environment file: $(ENV_FILE)"
	docker-compose --env-file $(ENV_FILE) up -d
	@echo "✅ Services started!"
	@echo ""
	@echo "🌐 Mongo Express: http://localhost:8081 (admin/admin123)"
	@echo "🗄️  MongoDB: mongodb://$(MONGO_USER):$(MONGO_PASSWORD)@localhost:$(MONGO_PORT)/$(MONGO_DATABASE)"

# Stop all services
down:
	@echo "⏹️  Stopping services..."
	docker-compose down

# View logs
logs:
	docker-compose logs -f

# View MongoDB logs only
mongo-logs:
	docker-compose logs -f mongodb

# Clean up (remove containers and volumes)
clean:
	@echo "🧹 Cleaning up containers and volumes..."
	docker-compose down -v --remove-orphans
	docker volume prune -f

# Restart services
restart: down up

# Open MongoDB shell
mongo-shell:
	@echo "🐚 Opening MongoDB shell..."
	docker exec -it noreyni-mongodb mongosh -u $(MONGO_USER) -p $(MONGO_PASSWORD) --authenticationDatabase admin $(MONGO_DATABASE)

# Show service status
status:
	@echo "📊 Service Status:"
	docker-compose ps

# Create logs directory
create-logs-dir:
	@mkdir -p logs/mongodb

# Backup database
backup:
	@echo "💾 Creating database backup..."
	@mkdir -p ./backups
	docker exec noreyni-mongodb mongodump --username $(MONGO_USER) --password $(MONGO_PASSWORD) --authenticationDatabase admin --db $(MONGO_DATABASE) --out /tmp/backup
	docker cp noreyni-mongodb:/tmp/backup ./backups/$(shell date +%Y%m%d_%H%M%S)
	@echo "✅ Backup created in ./backups/"

# Restore database (usage: make restore BACKUP_DIR=./backups/20240115_143000)
restore:
	@if [ -z "$(BACKUP_DIR)" ]; then \
		echo "❌ Usage: make restore BACKUP_DIR=./backups/20240115_143000"; \
		exit 1; \
	fi
	@echo "🔄 Restoring database from $(BACKUP_DIR)..."
	docker cp $(BACKUP_DIR) noreyni-mongodb:/tmp/restore
	docker exec noreyni-mongodb mongorestore --username $(MONGO_USER) --password $(MONGO_PASSWORD) --authenticationDatabase admin --db $(MONGO_DATABASE) --drop /tmp/restore/$(MONGO_DATABASE)
	@echo "✅ Database restored successfully!"
