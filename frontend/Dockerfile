# --- Build Stage ---
FROM node:20-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
# npm ci skips lightningcss's platform binary on musl (alpine); install it
# explicitly so vite's CSS minifier can load lightningcss.linux-x64-musl.node.
RUN --mount=type=cache,target=/root/.npm \
    npm ci && npm install --no-save lightningcss-linux-x64-musl@1.33.0

COPY . .
RUN npm run build

# --- Run Stage ---
FROM nginx:1.27-alpine AS run

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
