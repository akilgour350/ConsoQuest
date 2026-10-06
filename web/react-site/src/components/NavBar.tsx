import "./Components.scss"
import { NavLink } from "react-router"

const NavBarComponent: React.FC = () => {
    return (
        <>          
            <nav>
                <NavLink to="/">Home</NavLink>
                <NavLink to="/news">News</NavLink>
                <NavLink to="/play">Play</NavLink>
            </nav>
        </>
    )
}

export default NavBarComponent