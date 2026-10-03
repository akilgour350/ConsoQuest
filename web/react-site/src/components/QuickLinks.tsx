import "./NavBar.scss"
import { NavLink } from "react-router"


const QuickLinks: React.FC = () => {
    return (
        <>
            <div className="quicklinks">
                <ul>
                    <li><NavLink to="/acknowledgements">Acknowledgements</NavLink></li>
                    <li><NavLink to="/privacy">Privacy Policy</NavLink></li>
                </ul>                
            </div>

        </>
    )
}

export default QuickLinks