import "./NavBar.scss"
import { NavLink } from "react-router"

const NavBarComponent: React.FC = () => {
    return (
        <>          
            <nav>
                <NavLink to="/">Home</NavLink>
                <NavLink to="/about">About</NavLink>
                <NavLink to="/play">Play</NavLink>
            </nav>
        </>
    )
}

export default NavBarComponent