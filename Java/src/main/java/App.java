import Dao.FrameDao;
import Dao.InspectionDao;
import Dao.JdbcFrameDao;
import Dao.JdbcInspectionDao;
import Model.Box;
import Model.Frame;
import Model.Inspection;
import org.apache.commons.dbcp2.BasicDataSource;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.sql.DataSource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class App {

    private final FrameDao frameDao;
    private final InspectionDao inspectionDao;
    private final Scanner userInput = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BasicDataSource dataSource = new BasicDataSource();
        /**
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;

            }
            prop.load(input);
            } catch (IIOException e) {
            e.printStackTrace();
        }
        dataSource.setUrl(prop.getProperty("db.url"));
        dataSource.setUsername(prop.getProperty("db.user"));
        dataSource.setPassword(prop.getProperty("db.password"));
         **/
        dataSource.setUrl("jdbc:postgresql://localhost:5432/bees");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres1");
        App app = new App(dataSource);
        app.run();
        }

    public App(DataSource dataSource) {
        frameDao = new JdbcFrameDao(dataSource);
        inspectionDao = new JdbcInspectionDao(dataSource);
    }

    private void run(){
        welcomeMessage();
        boolean running = true;
        while (running) {
            displayMenu();
            int selection = promptForInt("Select an option: ");
            if(selection==1) {
                //start new inspection
                startNewInspection();
            } else if (selection==2) {
                //add notes
                addNotes();
            } else if (selection==3) {
                //view previous inspections
                viewPreviousInspections();
            } else if (selection==4) {
                //exit
                System.out.println("\nGoodbye");
                running = false;
            } else {
                displayError(" Invalid selection ");
            }
        }

    }

    private void welcomeMessage() {
        System.out.println("-----------------------------------------");
        System.out.println("|   Monte's Own Bee Inspection Tracker  |");
        System.out.println("-----------------------------------------");
    }

    private void displayMenu() {
        System.out.println("\n1. Start new Inspection");
        System.out.println("2. Add notes to an inspection");
        System.out.println("3. View previous inspections");
        System.out.println("4. Exit");
    }

    private int promptForInt(String prompt) {
        return (int) promptForDouble(prompt);
    }

    private double promptForDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String response = userInput.nextLine();
            try {
                return Double.parseDouble(response);
            }  catch (NumberFormatException e) {
                if (response.isBlank()) {
                    return -1;
                } else {
                    displayError("Please input a number.");
                }
            }
        }
    }

    private String promptForString(String prompt) {
        System.out.print(prompt);
        return userInput.nextLine();
    }

    private char promptForChar(String prompt) {
        System.out.println(prompt);
        return userInput.nextLine().charAt(0);
    }

    private void displayError(String message) {
        System.out.println();
        System.out.println("***" + message + "***");
        System.out.println();
    }

    private void startNewInspection() {
        Inspection newInspection = new Inspection();
        newInspection.setDateTime(LocalDateTime.now());
        for (int i=3;i>0;i--){
            String skipPrompt = "Skip box " + i + " (Y/N):";
            char skip = promptForChar(skipPrompt);
            if (skip=='Y' || skip=='y'){continue;}
            Box newBox = new Box(i);
            for(int j=0;j<6;j++){
                Frame newFrame = new Frame();
                newFrame.setBoxNumber(i);
                String frameName = "";
                if(j%2==0){
                    frameName = frameName + ((j/2)+1) + "A";
                } else {
                    frameName = frameName + ((j/2)+1) + "B";
                }
                newFrame.setFrameName(frameName);
                System.out.println(i + " - " + frameName);
                newFrame.setCombPattern(promptForString("Comb pattern: "));
                newFrame.setHoney(promptForString("Honey: "));
                newFrame.setNectar(promptForString("Nectar: "));
                newFrame.setBrood(promptForString("Brood: "));
                newFrame.setCells(promptForString("Cells: "));
                String queen = promptForString("Queen Spotted (Y/N): ");
                if(queen.toLowerCase().charAt(0)=='y'){
                    newFrame.setQueenSpotted(true);
                } else {
                    newFrame.setQueenSpotted(false);
                }
                //TODO: add frame to frame table
                newBox.addFrameToBox(newFrame);
                System.out.println("added frame " + newFrame.getFrameName() + ". Box now has " + newBox.getFrames().size() + " frames.");
            }
            newInspection.addBox(newBox);
            System.out.println("Added box " + i + " to inspection. There are now " + newInspection.getBoxes().size() + " boxes in the inspection");
        }
        newInspection.setWeather(promptForString("Current weather (temp condition): "));
        newInspection.setBeeTemperament(promptForString("Bee temperament: "));
        newInspection.setBeePopulation(promptForString("Bee population: "));
        newInspection.setDronePopulation(promptForString("Drone population: "));
        newInspection.setLayingPattern(promptForString("Laying pattern: "));
        newInspection.setHiveBeetles(promptForString("Hive beetles: "));
        newInspection.setOtherPests(promptForString("Other pests: "));
        Inspection returnedInspection = inspectionDao.createInspection(newInspection);
        int inspectionsId = returnedInspection.getInspectionId();
        System.out.println("Inspection " + inspectionsId + " has been added to the table");
        for (Box box : newInspection.getBoxes()) {
            for (Frame frame : box.getFrames()){
                frame.setInspectionId(inspectionsId);
                addFrame(frame);
                System.out.println("Added frame to table");
            }
        }
    }

    public void viewPreviousInspections() {
        List<Inspection> inspections = inspectionDao.list();
        for (Inspection current : inspections) {
            String notes = checkIfNotesIsNull(current.getNotes(),"",1);
            System.out.println("\n" + current.getInspectionId() + " - Weather: " + current.getWeather() + " | Date and Time: " + current.getDateTimeFormatted() +
                    " | Bee temperament: " + current.getBeeTemperament() + " | Bee population: " + current.getBeePopulation() + " | Drone population: " +
                    current.getDronePopulation() + "\nLaying pattern: " + current.getLayingPattern() + " | Hive beetles: " + current.getHiveBeetles() +
                    " | Other pests: " + current.getOtherPests() + "\nNotes: " + notes);
            for (int i=3;i>0;i--){
                displayFramesInBox(current.getInspectionId(),i);
            }
        }

    }

    public void displayFramesInBox(int inspectionId,int boxNum) {
        List<Frame> frames = frameDao.getFrameByInspectionAndBox(inspectionId,boxNum);
        System.out.println("Box " + boxNum + " has " + frames.size() + " frames.");
        if(frames.size()>0){
            String queenSpottedFrame = "";
            for (Frame currentFrame : frames) {
                System.out.println("Box number: " + currentFrame.getBoxNumber() + " | Frame: " + currentFrame.getFrameName() + " | Comb pattern: " +
                        currentFrame.getCombPattern() + " | Honey: " + currentFrame.getHoney() + " | Nectar: " + currentFrame.getNectar() +
                        " | Brood: " + currentFrame.getBrood() + " | Queen spotted: " + currentFrame.isQueenSpotted() + " | Cells: " + currentFrame.getCells());
                if (currentFrame.isQueenSpotted()) {
                    queenSpottedFrame = queenSpottedFrame + currentFrame.getFrameName();
                }
            }
            System.out.println("The queen was spotted in frame " + queenSpottedFrame + " in box " + boxNum);
        }
    }

    private void addFrame(Frame frame) {
        Frame returnedFrame = frameDao.createFrame(frame);
    }

    private String checkIfNotesIsNull(String existingNotes, String notesToAdd,int returnStringType) {
        //1-return "no notes" or existing     2-return empty or existing  3-return existing+new or new notes only
        String returnString;
        if (existingNotes==null) {
            if(returnStringType==1){
                returnString = "No Notes";
            } else if(returnStringType==2){
                returnString = "";
            } else {
                returnString = notesToAdd;
            }
        } else {
            if (returnStringType==1 || returnStringType==2) {
                returnString = existingNotes;
            } else {
                returnString = existingNotes + " " + notesToAdd;
            }
        }
        return returnString;
    }

    private void addNotes() {
        List<Inspection> inspections = inspectionDao.list();
        for (Inspection inspection : inspections) {
            String currentNotes = checkIfNotesIsNull(inspection.getNotes(),"",1);
            System.out.println("Inspection #" + inspection.getInspectionId() + ": " + inspection.getDateTimeFormatted() + " - " + currentNotes);
        }
        int inspectionToAddNotes = promptForInt("Enter inspection # to add notes to: ");
        String returnedNotes = checkIfNotesIsNull(inspectionDao.getNotesByInspectionId(inspectionToAddNotes),"",2);
        System.out.println("Inspection #" + inspectionToAddNotes + " has these notes: " + returnedNotes);
        String notesToAdd = promptForString("Notes to add to the existing notes: ");
        inspectionDao.updateNotes(checkIfNotesIsNull(inspectionDao.getNotesByInspectionId(inspectionToAddNotes),notesToAdd,3),inspectionToAddNotes);
    }

}

