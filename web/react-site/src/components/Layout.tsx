import { Outlet } from "react-router"
import NavBar from "./NavBar.tsx"
import SSI from "./ServerStatusIndicator.tsx"
import "./Components.scss"
import QuickLinks from "./QuickLinks.tsx"

const Layout: React.FC = () => {
    return (
        <>
            <div className="banner-container">
                <h1 className="banner">ConsoQuest</h1>
            </div>

            <div className="flexbox">
                <div className="sidebar">
                    <SSI />

                    <QuickLinks />
                </div>


                <div className="content-wrapper">
                    <NavBar />
                    <main>
                        <Outlet />
                    </main>
                </div>
            </div>


        </>
    )
}

export default Layout