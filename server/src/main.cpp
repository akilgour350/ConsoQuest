#include <crow/http_response.h>
#include <string>
#include "crow.h"
#include <pqxx/pqxx>
#include "bcrypt.h"

using namespace std;
using namespace crow;
using namespace pqxx;


int main() {
    SimpleApp app;

    // tests to make sure we can actually retrieve the database password
    string dbPwd = string(getenv("DB_PASSWORD"));
    if (dbPwd.empty()) {
        cerr << "FATAL: Could not retrieve database password" << endl;
        return 1;
    }

    try {
        // DB connection
        // reference should be passed to all methods requiring DB access
        connection conn("host=10.10.10.4 port=3005 dbname=postgres user=postgres password=" + dbPwd));

        // tests the DB connection and closes program if not available
        if (!conn.is_open()) {
            cerr << "FATAL: Could not connect to database" << endl;
            return 1;
        }

        CROW_ROUTE(app, "/status")([]() {
            return response(status::OK);
        });

        CROW_ROUTE(app, "/register").methods(HTTPMethod::POST)([&conn](const request& req) {
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
                res["success"] = true;
                res["username"] = username;
                return response(201, res);

            } catch (const exception& e) {
                return response(500, e.what());
            }
        });

        CROW_ROUTE(app, "/login")([&conn](const request& req) {
            string auth = req.get_header_value("Authorization");

            // makes sure there is actually any auth credentials found, otherwise returns 401
            if (auth.empty()) {
                json::wvalue response;
                response["verified"] = false;
                response["returning"] = "";

                return crow::response(401, response);
            }

            // digs up the username and password from the base64 string
            string creds = auth.substr(6);
            string decoded = utility::base64decode(creds, creds.size());
            size_t dividerPos = decoded.find(':');
            string username = decoded.substr(0, dividerPos);
            string password = decoded.substr(dividerPos + 1);

            // checks if username and password are valid and returns that the user is authorised if so
            try {
                // creates and executes a database transaction
                work transaction(conn);
                auto result = transaction.exec("SELECT * FROM players WHERE username = $1", params(username));
                transaction.commit();

                // makes sure something was actually returned; if it was the username doesn't exist
                if (result.empty()) {
                    json::wvalue response;
                    response["verified"] = false;
                    response["message"] = "Invalid credentials";
                    return crow::response(401, response);
                }

                // extracts the stored hash from the results and checks if it's valid
                bool valid = bcrypt::validatePassword(password, result[0]["password_hash"].as<string>());

                if (valid) { // if the password is correct, return that the user has been verified and return a JWT access token
                    json::wvalue response;
                    response["verified"] = true;
                    response["message"] = "Success";
                    //response["xcoord"]   = result[0]["xcoord"].as<int>();
                    //response["ycoord"]   = result[0]["ycoord"].as<int>();

                    return crow::response(200, response);
                } else {
                    json::wvalue response;
                    response["verified"] = false;
                    response["message"] = "Invalid credentials";
                    return crow::response(500, response);
                }
            } catch (exception& e) {
                json::wvalue response;
                response["verified"] = false;
                response["message"] = e.what();
                return crow::response(500, response);
            }

            // if we got here, the user isn't authorised and 401 is returned
            return response(401, "Invalid credentials");
        });

        app.port(18080).run();



    } catch (const exception& e) {
        cerr << "FATAL: " << e.what() << endl;
        return 1;
    }
}
