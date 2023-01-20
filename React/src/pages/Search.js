import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import "./Page.css";
import { SearchResultsSingle, SearchResultsMinMax } from '../components/SearchResults';
import { getListOfInspections } from '../Services/InspectionService';

function Search(props) {
    const [inspectionList, setInspectionList] = useState([]);
    const [notesSearchTerm, setNotesSearchTerm] = useState("");
    const [weatherSearchTerm, setWeatherSearchTerm] = useState("");
    const [tempSearch, setTempSearch] = useState({
        minTemp: "",
        maxTemp: ""
    })

    const weatherConditions = [
        {label: '', value: ''},
        {label: 'Clear Sky', value: 'Clear Sky'},
        {label: 'Partly Cloudy', value: 'Partly Cloudy'},
        {label: 'Overcast', value: 'Overcast'},
        {label: 'Fog', value: 'Fog'},
        {label: 'Drizzle', value: 'Drizzle'},
        {label: 'Rain', value: 'Rain'},
        {label: 'Freezing Rain', value: 'Freezing Rain'},
        {label: 'Snow', value: 'Snow'},
        {label: 'Thunderstorm', value: 'Thunderstorm'},
        {label: 'Other', value: 'Unknown'}
    ]

    function setTemps(number, position) {
        if (position === "min") {
            setTempSearch({
                minTemp: number,
                maxTemp: tempSearch.maxTemp
            });
        } else {
            setTempSearch({
                minTemp: tempSearch.minTemp,
                maxTemp: number
            });
        }
    }

    function clearAll() {
        setNotesSearchTerm("");
        setWeatherSearchTerm("");
        setTempSearch({
            minTemp: "",
            maxTemp: ""
        })
    }

    useEffect(() => {
        getListOfInspections()
            .then((result) => {
                setInspectionList(result);
            })
    });

    const hasNotes = inspectionList.filter(inspection => {
        return inspection.notes !== null;
    })

    const searchNotes = hasNotes.filter(inspection => {
        return inspection.notes.toLowerCase().includes(notesSearchTerm.toLowerCase());
    });

    const searchWeather = inspectionList.filter(inspection => {
        return inspection.weatherCondition.toLowerCase().includes(weatherSearchTerm.toLowerCase());
    });

    const searchTemperature = inspectionList.filter(inspection => {
        if (tempSearch.minTemp !== "" && tempSearch.maxTemp !== "") {
            return (inspection.weatherTemp >= tempSearch.minTemp && inspection.weatherTemp <= tempSearch.maxTemp);
        } else {
            return inspectionList
        }
    })

    return <div>
        <Header />
        <main>
            <button className='button red right' onClick={clearAll}>Clear all inputs</button>
            <label for="notesSearch"><h3>Search notes: </h3></label>
            <input
                type="text"
                id="notesSearch"
                onChange={e => setNotesSearchTerm(e.target.value)}
                value={notesSearchTerm}
                placeholder="search"
                className='searchBox'
            />
            <hr />
            <h3>Search Weather</h3>
            <Select
                label="Condition: "
                options={weatherConditions}
                value={weatherSearchTerm}
                onChange={e => setWeatherSearchTerm(e.target.value)}
            />
            <br />
            <br />
            <label for="minTemp">Temperature between </label>
            <input
                type="number"
                id="minTemp"
                onChange={e => setTemps(e.target.value, "min")}
            />
            <label for="maxTemp"> and </label>
            <input
                type="number"
                id="maxTemp"
                onChange={e => setTemps(e.target.value, "max")}
            />
            <SearchResultsSingle searchTerm={notesSearchTerm} inspections={searchNotes} />
            <SearchResultsSingle searchTerm={weatherSearchTerm} inspections={searchWeather} />
            <SearchResultsMinMax min={tempSearch.minTemp} max={tempSearch.maxTemp} term="temperatures" inspections={searchTemperature} />
        </main>
    </div>;
};

function Select({ label, value, options, onChange }) {
    return <label>
        {label}
        <select value={value} onChange={onChange}>
            {options.map((option) => (
                <option value={option.value}>{option.label}</option>
            ))}
        </select>
    </label>
}

export default Search;