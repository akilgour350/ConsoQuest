#include <crow/http_response.h>
#include <string>
#include "crow.h"

using namespace std;

int main() {
    crow::SimpleApp app;

    CROW_ROUTE(app, "/status")([]() {
        return crow::response(crow::status::OK);
    });

    CROW_ROUTE(app, "/verify")([](const crow::request& req) {
        string auth = req.get_header_value("Authorization");

        // makes sure there is actually any auth credentials found, otherwise returns 401
        if (auth.empty()) {
            crow::json::wvalue response;
            response["verified"] = false;
            response["username"] = "";

            return crow::response(401, response);
        }

        // digs up the username and password from the base64 string
        string creds = auth.substr(6);
        string decoded = crow::utility::base64decode(creds, creds.size());
        size_t dividerPos = decoded.find(':');
        string username = decoded.substr(0, dividerPos);
        string password = decoded.substr(dividerPos + 1);

        // checks if username and password are valid and returns that the user is authorised if so
        if (username == "codeyking350" && password == "123") {
            crow::json::wvalue response;
            response["verified"] = true;
            response["username"] = username;
            return crow::response(200, response);
        }

        // if we got here, the user isn't authorised and 401 is returned
        return crow::response(401, "Invalid credentials");
    });

    app.port(18080).run();
}
