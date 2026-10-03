import "./NavBar.scss"
import { useEffect, useState } from "react"

type Status = "checking" | "online" | "offline";

async function checkServerStatus(): Promise<Status> {
    try {
        const response = await fetch("https://conso.akilgour.com/api/status", {
            signal: AbortSignal.timeout(5000),
        });
        return response.ok ? "online" : "offline";
    } catch (error) {
        console.log("ERROR: ", error);
        return "offline";
    }
}

const ServerStatusIndicator: React.FC = () => {
    const [status, setStatus] = useState<Status>("checking");

    useEffect(() => {
        checkServerStatus().then(setStatus);
    }, []); // empty array = run once when the component mounts

    return (
        <>
            <div className="ssi">
                <h1>Server Status</h1>
                <h3>conso.akilgour.com is</h3>
                <h2>{status}</h2>
            </div>
        </>
    )
}

export default ServerStatusIndicator