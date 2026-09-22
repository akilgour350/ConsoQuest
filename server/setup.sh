#!/bin/bash

echo "================================"
echo "  ConsoQuest Server Setup"
echo "================================"
echo ""

# Function to check if a command exists
is_installed() {
    command -v "$1" >/dev/null 2>&1
}

# Install dependencies
echo "Checking system..."

if [ -f /etc/alpine-release ]; then
    echo "Alpine Linux detected."
    echo ""

    if ! is_installed docker; then
        echo "Installing Docker..."
        apk add --no-cache docker
        rc-update add docker boot
        rc-service docker start
    else
        echo "Docker already installed, skipping."
    fi

    if ! is_installed ansible; then
        echo "Installing Ansible..."
        apk add --no-cache ansible
    else
        echo "Ansible already installed, skipping."
    fi

    if ! is_installed java; then
        echo "Installing Java..."
        apk add --no-cache openjdk21-jre bash git
    else
        echo "Java already installed, skipping."
    fi

    if ! is_installed k3s; then
        echo "Installing k3s..."
        curl -sfL https://get.k3s.io | sh -
        rc-update add k3s boot

        echo "Disabling Traefik..."
        mkdir -p /etc/rancher/k3s
        cat > /etc/rancher/k3s/config.yaml << EOF
disable:
  - traefik
EOF

        echo "Installing Nginx ingress controller..."
        k3s kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.12.3/deploy/static/provider/cloud/deploy.yaml
        echo "Waiting for Nginx ingress to be ready..."
        k3s kubectl wait --namespace ingress-nginx \
          --for=condition=ready pod \
          --selector=app.kubernetes.io/component=controller \
          --timeout=120s
    else
        echo "k3s already installed, skipping."
    fi

    if ! is_installed liquibase; then
        echo "Installing Liquibase..."
        wget -q https://github.com/liquibase/liquibase/releases/download/v4.29.2/liquibase-4.29.2.tar.gz
        mkdir -p /opt/liquibase
        tar -xzf liquibase-4.29.2.tar.gz -C /opt/liquibase
        rm liquibase-4.29.2.tar.gz
        ln -sf /opt/liquibase/liquibase /usr/local/bin/liquibase
    else
        echo "Liquibase already installed, skipping."
    fi

else
    echo "WARNING: This script is designed for Alpine Linux."
    echo ""
    echo "If you already have the following installed, you can continue:"
    echo "  - Docker"
    echo "  - k3s"
    echo "  - Ansible"
    echo "  - Liquibase"
    echo "  - Java 21+"
    echo "  - Nginx ingress controller configured in k3s"
    echo ""
    read -p "Continue anyway? (y/n): " CONTINUE
    if [ "$CONTINUE" != "y" ]; then
        echo "Aborting. Please install the dependencies listed above and re-run."
        exit 1
    fi
fi

echo ""

# Verify dependencies
echo "Verifying dependencies..."
MISSING=""
is_installed docker || MISSING="$MISSING\n  - Docker"
is_installed k3s || MISSING="$MISSING\n  - k3s"
is_installed ansible || MISSING="$MISSING\n  - Ansible"
is_installed liquibase || MISSING="$MISSING\n  - Liquibase"
is_installed java || MISSING="$MISSING\n  - Java 21+"

if [ -n "$MISSING" ]; then
    echo "The following dependencies are missing:"
    echo -e "$MISSING"
    echo ""
    echo "Please install them and re-run this script."
    exit 1
fi

echo "All dependencies verified!"
echo ""

# Gather input
echo "Please provide the following configuration values:"
echo ""
read -p "Database password: " -s DB_PASSWORD
echo ""
read -p "JWT secret key: " -s JWT_SECRET
echo ""
read -p "VM internal IP (e.g. 10.10.10.4): " VM_IP
echo ""

# Create .env file
echo "Creating .env file..."
cat > docker/.env << EOF
POSTGRES_PASSWORD=$DB_PASSWORD
POSTGRES_USER=postgres
POSTGRES_DB=consodb
EOF
echo ".env created!"

# Start Docker services
echo "Starting Docker services..."
docker compose -p consoquest -f docker/docker-compose.yml up -d
echo "Docker services started!"

# Create Kubernetes secrets
echo "Creating Kubernetes secrets..."
k3s kubectl create secret generic db-credentials \
  --from-literal=password=$DB_PASSWORD \
  --dry-run=client -o yaml | k3s kubectl apply -f -
k3s kubectl create secret generic jwt-secret \
  --from-literal=secret=$JWT_SECRET \
  --dry-run=client -o yaml | k3s kubectl apply -f -
echo "Secrets created!"

# Apply k3s manifests
echo "Applying Kubernetes manifests..."
k3s kubectl apply -f k3s/
echo "Manifests applied!"

# Run Liquibase migrations
echo "Running database migrations..."
cd liquibase
liquibase \
  --url=jdbc:postgresql://localhost:3005/consodb \
  --username=postgres \
  --password=$DB_PASSWORD \
  --searchPath=$(pwd) \
  --changeLogFile=changelog.xml \
  update
cd ..
echo "Migrations complete!"

# Build and import API image
echo "Building API image..."
docker build -t consoquest-api:latest .
docker save consoquest-api:latest | k3s ctr images import -
k3s kubectl set image deployment/consoquest-api consoquest-api=consoquest-api:latest
echo "API deployed!"

echo ""
echo "================================"
echo "  Setup complete!"
echo ""
echo "  Don't forget to:"
echo "  - Configure your Cloudflare tunnel"
echo "    to point to http://$VM_IP:80"
echo "  - Set up Jenkins for CI/CD"
echo "    (see README for details)"
echo "================================"