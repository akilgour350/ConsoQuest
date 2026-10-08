interface LoginResponse {
    token: string;
    x: number;
    y: number;
    username: string;
}

export class GameSession {
    username: string = "";
    x: number = 0;
    y: number = 0;
    loggedIn: boolean = false;

    async restore(): Promise<string | null> {
        try {
            const token: string | null = sessionStorage.getItem("cq-token");
            if (token !== null) {
                // sends the login request to the API
                const response = await fetch('https://conso.akilgour.com/api/restore', {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (!response.ok) { // returns if request failed
                    return await response.text();
                }

                const data: LoginResponse = await response.json(); // parses returned JSON to LoginResponse interface
                // saves all needed values
                this.username = data.username;
                this.x = data.x;
                this.y = data.y;
                this.loggedIn = true;

                sessionStorage.setItem("cq-token", data.token); // stores the JWT token in session storage

                return null; // indicate that login was a success

            }

            return "Your current session has expired!";
        } catch (error) {
            console.error("[ERROR] Session restore failed: ", error);
            return "An error occurred - check the console for details!";
        }
    }

    // logs the user in with the given username and password
    async login(uname: string, pwd: string): Promise<string | null> {
        try {
            // sends the login request to the API
            const response = await fetch('https://conso.akilgour.com/api/login', {
                method: 'POST',
                body: JSON.stringify({ username: uname, password: pwd }),
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) { // returns if request failed
                return await response.text();
            }

            const data: LoginResponse = await response.json(); // parses returned JSON to LoginResponse interface
            // saves all needed values
            this.username = uname;
            this.x = data.x;
            this.y = data.y;
            this.loggedIn = true;

            sessionStorage.setItem("cq-token", data.token); // stores the JWT token in session storage

            return null; // indicate that login was a success

        } catch (error) {
            console.error("[ERROR] Login failed: ", error);
            return "An error occurred - check the console for details!";
        }
    }

    // registers the user in with the given username and password
    async register(uname: string, pwd: string): Promise<string | null> {
        try {
            // sends the login request to the API
            const response = await fetch('https://conso.akilgour.com/api/register', {
                method: 'POST',
                body: JSON.stringify({ username: uname, password: pwd }),
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) { // returns if request failed
                return await response.text();
            }

            const data: LoginResponse = await response.json(); // parses returned JSON to LoginResponse interface
            // saves all needed values
            this.username = uname;
            this.x = 0;
            this.y = 0;
            this.loggedIn = true;

            sessionStorage.setItem("cq-token", data.token);  // stores the JWT token in session storage

            return null; // indicate that login was a success

        } catch (error) {
            console.error("[ERROR] Registration failed: ", error);
            return "An error occurred - check the console for details!";
        }
    }
}