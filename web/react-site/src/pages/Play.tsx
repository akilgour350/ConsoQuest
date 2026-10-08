import '../styles.scss'
import './Play.scss'
import { GameSession } from '../GameSession';
import { useEffect, useState } from 'react';

function Play() {
  const [game] = useState(() => new GameSession());
  const [loggedIn, setLoggedIn] = useState(game.loggedIn);
  const [checking, setChecking] = useState(true);
  const [error, setError] = useState("");
  const [currentForm, setCurrentForm] = useState<"login" | "register">("login"); // used to figure out whether to show login or register forms

  async function login(formData: FormData) {
    const uname = formData.get("username") as string;
    const pwd = formData.get("password") as string;

    const result: string | null = await game.login(uname, pwd);
    if (result === null) {
      console.log("SUCCESS!");
      setLoggedIn(true);
    } else {
      console.log("FAILURE");
      setError(result);
    }
  }

  async function register(formData: FormData) {
    const uname = formData.get("username") as string;
    const pwd = formData.get("password") as string;
    const pwd2 = formData.get("password-confirm") as string;

    if (pwd !== pwd2) {
      setError("Passwords did not match!");
      return;
    }

    const result: string | null = await game.register(uname, pwd);
    if (result === null) {
      console.log("SUCCESS!");
    } else {
      console.log("FAILURE");
      setError(result);
    }
  }

  useEffect(() => {
    game.restore().then(ok => {
      setLoggedIn(ok === null);
      setChecking(false);
    });
  }, []);

  if (checking) {
    return (
      <>
        <h1> == Play == </h1>
        <h2 className="loading">Loading</h2>
      </>
    )

  }

  return (
    <>
      <h1> == Play == </h1>

      {
        !loggedIn ? (
          currentForm === "login" ? (
            <form id="loginForm" action={login}>
              <h2>Log In</h2>

              <input name="username" id="username" placeholder="Username" required />

              <br />
              <input name="password" id="password" type="password" placeholder="Password" required />

              <br />
              <button type="submit">Log In</button><br />
              {error && <p className="error">{error}</p>}
              <a onClick={() => setCurrentForm("register")}>No account?</a>
            </form>
          )
            :
            (
              <form id="register" action={register}>
                <h2>Register</h2>

                <input name="username" id="username" placeholder="Username" required />

                <br />
                <input name="password" id="password" type="password" placeholder="Password" required />

                <br />
                <input name="password-confirm" id="password-confirm" type="password" placeholder="Confirm password" required />

                <br />
                <button type="submit">Register</button><br />
                {error && <p className="error">{error}</p>}
                <a onClick={() => setCurrentForm("login")}>Have an account?</a>
              </form>
            )
        ) :
          (
            <div className="play-stage">
              <p>Pick an option to start!</p>
            </div>
          )
      }

    </>
  )
}

export default Play
