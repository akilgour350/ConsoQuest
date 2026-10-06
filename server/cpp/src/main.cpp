#include <crow/common.h>
#include <crow/http_response.h>
#include <string>
#include "crow.h"
#include "crow/middlewares/cors.h"
#include <pqxx/pqxx>
#include "bcrypt.h"
#include "jwt-cpp/jwt.h"
#include "WorldGen.h"

using namespace std;
using namespace crow;
using namespace pqxx;


/// Generates and returns a JWT access token with the given secret and username
auto generateJwt(string username, string jwtSecret) {
    return jwt::create<jwt::traits::kazuho_picojson>()
        .set_issuer("consoquest")
        .set_subject(username)
        .set_issued_at(std::chrono::system_clock::now())
        .set_expires_at(std::chrono::system_clock::now() + std::chrono::minutes(10))
        .sign(jwt::algorithm::hs256{jwtSecret});
}

/// Checks if a given JWT token is valid 
bool verifyToken(const string& token, const string& secret) {
    try {
        auto decoded = jwt::decode<jwt::traits::kazuho_picojson>(token);

        auto verifier = jwt::verify<jwt::traits::kazuho_picojson>()
            .allow_algorithm(jwt::algorithm::hs256{secret})
            .with_issuer("consoquest");

        verifier.verify(decoded);
        return true;
    } catch (const exception& e) {
        return false;
    }
}

/// retrieves a user's username from the token for use in database
string getUsernameFromToken(const string& token) {
    auto decoded = jwt::decode<jwt::traits::kazuho_picojson>(token);
    return decoded.get_subject();
}

int main() {
    App<CORSHandler> app;

    auto& cors = app.get_middleware<crow::CORSHandler>();
    cors.global()
        .origin("*")
        .methods("GET"_method, "POST"_method)
        .headers("Content-Type", "Authorization");

    // tests to make sure we can actually retrieve the database password
    string dbPwd = string(getenv("DB_PASSWORD"));
    if (dbPwd.empty()) {
        cerr << "FATAL: Could not retrieve database password" << endl;
        return 1;
    }

    // tests to make sure we can actually retrieve the database host
    char* dbHost = getenv("DB_HOST");
    if (!dbHost) {
        cerr << "FATAL: Could not retrieve database host environment variable" << endl;
        return 1;
    }

    // tests to make sure we can actually retrieve the database port
    char* dbPort = getenv("DB_PORT");
    if (!dbPort) {
        cerr << "FATAL: Could not retrieve database port environment variable" << endl;
        return 1;
    }

    // tests to make sure we can actually retrieve the JWT secret
    string jwtSecret = string(getenv("JWT_SECRET"));
    if (dbPwd.empty()) {
        cerr << "FATAL: Could not retrieve JWT secret" << endl;
        return 1;
    }

    // retrieves the world seed
    char* seedEnv = getenv("WORLD_SEED");
    if (!seedEnv) {
        cerr << "FATAL: Could not retrieve seed environment variable" << endl;
        return 1;
    }
    auto worldGen = WorldGen(static_cast<int>(hash<string>{}(string(seedEnv))));

    try {
        // DB connection
        // reference should be passed to all methods requiring DB access
        connection conn("host=" + string(dbHost) +" port=" + string(dbPort) + " dbname=consodb user=postgres password=" + dbPwd);

        // tests the DB connection and closes program if not available
        if (!conn.is_open()) {
            cerr << "FATAL: Could not connect to database" << endl;
            return 1;
        }

        // returns if the server is up and running
        CROW_ROUTE(app, "/status")([]() {
            return response(status::OK);
        });


#pragma region USER MANAGEMENT
        // registers a new user with the given username and password
        CROW_ROUTE(app, "/register").methods(HTTPMethod::POST)([&conn, &jwtSecret](const request& req) {
            auto body = json::load(req.body);
            if (!body)
                return response(400, "Invalid JSON");

            string username = body["username"].s();
            string password = body["password"].s();

            if (username.empty() || password.empty())
                return response(400, "Username and password required");

            try {
                work transaction(conn);

                // Check if username already exists
                auto existing = transaction.exec(
                    "SELECT username FROM players WHERE username = $1",
                    params(username)
                );
                if (!existing.empty())
                    return response(409, "Username already taken");

                // Hash the password
                string hash = bcrypt::generateHash(password);

                // Insert new player
                transaction.exec(
                    "INSERT INTO players (username, password_hash) VALUES ($1, $2)",
                    params(username, hash)
                );
                transaction.commit();

                json::wvalue res;
                res["username"] = username;
                res["x"] = 0; //TODO: Make value randomised (probably weighted towards a village)
                res["y"] = 0; //TODO: Make value randomised (probably weighted towards a village)
                res["token"] = generateJwt(username, jwtSecret);
                return response(201, res);

            } catch (const exception& e) {
                return response(500, e.what());
            }
        });

        // logs the user in with the given username and password
        CROW_ROUTE(app, "/login").methods(HTTPMethod::POST)([&conn, &jwtSecret](const request& req) {
            auto body = json::load(req.body);

            string username = body["username"].s();
            string password = body["password"].s();

            // makes sure there is actually any auth credentials found, otherwise returns 401
            if (username.empty() || password.empty()) {
                return response(400, "Bad login request");
            }

            // checks if username and password are valid and returns that the user is authorised if so
            try {
                // creates and executes a database transaction
                work transaction(conn);
                auto result = transaction.exec("SELECT * FROM players WHERE username = $1", params(username));
                transaction.commit();

                // makes sure something was actually returned; if it was the username doesn't exist
                if (result.empty()) {
                    return response(401, "Invalid credentials");
                }

                // extracts the stored hash from the results and checks if it's valid
                bool valid = bcrypt::validatePassword(password, result[0]["password_hash"].as<string>());

                if (valid) { // if the password is correct, return that the user has been verified and return a JWT access token
                    json::wvalue res;
                    res["token"] = generateJwt(username, jwtSecret);
                    res["x"] = result[0]["x"].as<int>();
                    res["y"] = result[0]["y"].as<int>();

                    return response(200, res);
                } else {
                    return response(401, "Invalid credentials");
                }
            } catch (exception& e) {
                return response(500, "Internal server error: " + string(e.what()));
            }

            // if we got here, the user isn't authorised and 401 is returned
            return response(401, "Invalid credentials");
        });

        // deletes a user based on their username
        CROW_ROUTE(app, "/delete").methods(HTTPMethod::POST)([&conn, &jwtSecret](const request& req) {
            string token = req.get_header_value("Authorization").substr(7);

            if (verifyToken(token, jwtSecret)) { // only run if the provided token is valid
                string username = getUsernameFromToken(token);

                work transaction(conn);
                auto result = transaction.exec("DELETE FROM players WHERE username = $1", params(username)); // exterminate  ̵̄/͇̐|
                transaction.commit();

                if (result.affected_rows() == 1) {
                    return response(200, "Successfully deleted user");
                }

                return response(400, "No matching users deleted");
            }

            return response(403, "Invalid credentials");
        });

        // restores a user's session based on a given JWT token (KEEP THE TOKENS SAFE!!!!!)
        CROW_ROUTE(app, "/restore").methods(HTTPMethod::POST)([&conn, &jwtSecret](const request& req) {
            string token = req.get_header_value("Authorization").substr(7);

            if (verifyToken(token, jwtSecret)) {
                string username = getUsernameFromToken(token);
                work transaction(conn);
                auto result = transaction.exec("SELECT * FROM players WHERE username = $1", params(username));

                if (sizeof(result) > 0) { // check there was a user actually found with this username
                    json::wvalue res;
                    res["token"] = generateJwt(username, jwtSecret); // generates a new token
                    res["x"] = result[0]["x"].as<int>();
                    res["y"] = result[0]["y"].as<int>();
                    res["username"] = result[0]["username"].as<string>();

                    return response(200, res);
                } else {
                    return response(401, "Invalid username");
                }
            }

            return response(403, "Invalid credentials");
        });
#pragma endregion

#pragma region TILES
        // retrieves or generates a tile at the given X and Y coordinates
        CROW_ROUTE(app, "/tile").methods(HTTPMethod::POST)([&conn, &jwtSecret, &worldGen](const request& req) {
            string token = req.get_header_value("Authorization"); // checks the given JWT token is valid
            if (token.empty()) {
                return response(401, "No token provided");
            }

            token = token.substr(7);

            if (verifyToken(token, jwtSecret)) { // only executes if the given JWT is valid
                string username = getUsernameFromToken(token);

                auto body = json::load(req.body);
                int xcoord = static_cast<int>(body["x"].i());
                int ycoord = static_cast<int>(body["y"].i());

                work setPlayerCoordsTransaction(conn);
                setPlayerCoordsTransaction.exec(
                    "UPDATE players SET x = $1, y = $2 WHERE username = $3",
                    params(xcoord, ycoord, username)
                );
                setPlayerCoordsTransaction.commit();

                // looks to see if the tile exists in the database
                work getExistingTileTransaction(conn);
                auto dbTile = getExistingTileTransaction.exec(
                    "SELECT * FROM tiles WHERE x = $1 AND y = $2", params(xcoord, ycoord)
                );
                getExistingTileTransaction.commit();

                if (dbTile.empty()) { // checks if a matching tile was found
                    // if no tile was found, generate a new one, store it, and send it back
                    Tile tile = worldGen.generateTile(xcoord, ycoord);

                    work createNewTileTransaction(conn);
                    createNewTileTransaction.exec(
                        "INSERT INTO tiles (x, y, biome, structure) "
                        "VALUES ($1, $2, $3, $4)",
                        params(xcoord, ycoord, WorldGen::biomeToString(tile.biome), tile.structure)
                    );
                    createNewTileTransaction.commit();

                    // package up the tile into JSON and send it on its way
                    json::wvalue res;
                    res["x"] = xcoord;
                    res["y"] = ycoord;
                    res["biome"] = WorldGen::biomeToString(tile.biome);
                    res["structure"] = tile.structure;
                    res["structure_cleared"] = false;
                    res["token"] = generateJwt(username, jwtSecret); // we always generate a new token when a request is received; they have short lifespans
                    return response(200, res);
                }

                // structure completion check
                work checkClearedTransaction(conn);
                auto clearedResult = checkClearedTransaction.exec(
                    "SELECT * FROM player_structures WHERE x = $1 AND y = $2 AND username = $3",
                    params(xcoord, ycoord, username)
                );
                checkClearedTransaction.commit();

                // package everything up and send it home with a JWT bow on top
                json::wvalue res;
                res["x"] = xcoord;
                res["y"] = ycoord;
                res["biome"] = dbTile[0]["biome"].as<string>();
                res["structure"] = dbTile[0]["structure"].is_null() ? "NONE" : dbTile[0]["structure"].as<string>();
                res["structure_cleared"] = !clearedResult.empty();
                res["token"] = generateJwt(username, jwtSecret); // we always generate a new token when a request is received; they have short lifespans. like fruit flies.
                return response(200, res);

            }
            return response(403, "Invalid credentials");
        });
#pragma endregion

        app.port(18080).run();



    } catch (const exception& e) {
        cerr << "FATAL: " << e.what() << endl;
        return 1;
    }
}
