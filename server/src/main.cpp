#include <iostream>
#include <string>
#include "crow.h"

using namespace std;

int main() {
    crow::SimpleApp app;

    CROW_ROUTE(app, "/status")([]() {
        return crow::response(crow::status::OK);
    });

    app.port(18080).run();
}
