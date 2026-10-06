import "./Components.scss"
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
    }, []);

    return (
        <>
            <div className="ssi">
                <h2>Server Status</h2>
                <h1 className={status}>{status}</h1>
            </div>
        </>
    )
}

export default ServerStatusIndicator