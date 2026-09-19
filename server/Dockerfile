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

# Build bcrypt
RUN git clone https://github.com/hilch/Bcrypt.cpp.git /opt/bcrypt && \
    cp /opt/bcrypt/include/bcrypt.h /usr/local/include/ && \
    cp /opt/bcrypt/src/bcrypt.cpp /opt/bcrypt/src/blowfish.cpp /usr/local/src/

# Copy and build the API
WORKDIR /app
COPY src/ .
RUN g++ -std=c++20 -O2 \
    -DASIO_STANDALONE \
    -I/opt/crow/include \
    -I/usr/local/include \
    -o consoquest-api main.cpp \
    /usr/local/src/bcrypt.cpp \
    /usr/local/src/blowfish.cpp \
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
