import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import { startNewInspection } from '../Services/InspectionService';
import "./Page.css";
function Home(props) {
    let navigate = useNavigate();

    function handleStartClick() {
        startNewInspection()
    .then(data => {
        navigate(`newInspection/${data}`);
    });
    }
    return <div>
        <Header />
        <main>
            <section className="button-group-column">
                <button onClick={handleStartClick} className="large full button green">Start a New Inspection</button>
                <Link to="/pastInspections" className="large full button orange">View Past Inspections</Link>
            </section>
        </main>
    </div>;
};

export default Home;