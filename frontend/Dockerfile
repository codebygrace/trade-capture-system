# Multi-stage build
# Stage 1 - Base stage with Node.js and pnpm setup
FROM node:20-slim AS base

# Install pnpm via corepack
# Set environment variable inside the container
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"

# Enables management of pnpm package manager
RUN corepack enable

# Set the working directory
WORKDIR /app


# Stage 2 - builder stage to install everything
FROM base AS builder

# Copy dependencies (for better catching)
COPY package.json pnpm-lock.yaml ./

# Cache pnpm store across builds
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile

# Copy build content from the directory where docker build is run to this container
COPY . .

# Run build
RUN pnpm run build

# For production, nginx is used to serve static files
FROM nginx:alpine AS final

# Copy files from base to the directory where nginx stores static files
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx from build content into the container
COPY nginx.conf /etc/nginx/nginx.conf

# Port the container is listening on at runtime - default port for nginx
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]