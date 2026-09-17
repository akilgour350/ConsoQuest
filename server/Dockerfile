# Build stage
FROM alpine:latest AS builder

RUN apk add --no-cache \
    build-base \
    cmake \
    git \
    asio-dev \
    openssl-dev \
    linux-headers \
    postgresql-dev

# Clone Crow
RUN git clone https://github.com/CrowCpp/Crow.git /opt/crow

# Build libpqxx
RUN git clone https://github.com/jtv/libpqxx.git /opt/libpqxx && \
    cd /opt/libpqxx && \
    cmake -B build && \
    cmake --build build && \
    cmake --install build

# Copy and build the API
WORKDIR /app
COPY src/ .
RUN g++ -std=c++20 -O2 \
    -DASIO_STANDALONE \
    -I/opt/crow/include \
    -o consoquest-api main.cpp \
    -lpthread -lpqxx -lpq

# Run stage
FROM alpine:latest

RUN apk add --no-cache \
    libstdc++ \
    postgresql-libs

WORKDIR /app
COPY --from=builder /app/consoquest-api .
COPY --from=builder /usr/local/lib/libpqxx* /usr/local/lib/

EXPOSE 18080

CMD ["./consoquest-api"]
