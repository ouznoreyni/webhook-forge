#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting webhook-db MongoDB Stack...${NC}"

# Check if running from project root
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ .env file not found. Please run this script from the project root.${NC}"
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check if docker folder exists
if [ ! -d "docker" ]; then
    echo -e "${RED}❌ Docker folder not found. Please ensure docker/docker-compose.yml exists.${NC}"
    exit 1
fi

# Load environment variables
set -a
source .env
set +a

echo -e "${YELLOW}📋 Configuration:${NC}"
echo -e "   Database: ${MONGO_DATABASE}"
echo -e "   User: ${MONGO_USER}"
echo -e "   Port: ${MONGO_PORT}"

# Create logs directory
mkdir -p docker/logs/mongodb

# Navigate to docker directory
cd docker

echo -e "${BLUE}📦 Pulling images...${NC}"
docker-compose pull

echo -e "${BLUE}🚀 Starting services...${NC}"
docker-compose --env-file ../.env up -d

echo -e "${YELLOW}⏳ Waiting for services to be healthy...${NC}"
sleep 15

# Check if services are running
if docker-compose ps | grep -q "Up"; then
    echo -e "${GREEN}✅ Services are running!${NC}"
    echo ""
    echo -e "${BLUE}🌐 Mongo Express UI: http://localhost:8081${NC}"
    echo -e "   👤 Username: admin"
    echo -e "   🔐 Password: admin123"
    echo ""
    echo -e "${BLUE}🗄️  MongoDB Connection:${NC}"
    echo -e "   📍 mongodb://${MONGO_USER}:${MONGO_PASSWORD}@localhost:${MONGO_PORT}/${MONGO_DATABASE}"
    echo ""
    echo -e "${YELLOW}📋 Useful commands:${NC}"
    echo -e "   View logs: cd docker && make logs"
    echo -e "   MongoDB shell: cd docker && make mongo-shell"
    echo -e "   Stop services: cd docker && make down"
else
    echo -e "${RED}❌ Services failed to start properly.${NC}"
    echo -e "${YELLOW}🔍 Check logs: cd docker && make logs${NC}"
    exit 1
fi