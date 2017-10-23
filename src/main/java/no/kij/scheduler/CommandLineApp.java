package no.kij.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

import no.kij.scheduler.dao.LecturerDAO;
import no.kij.scheduler.dao.RoomDAO;
import no.kij.scheduler.dao.SubjectDAO;
import no.kij.scheduler.dto.*;
import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

/**
 * This is the class that contains the code to allow the user to interact with the application,
 * using the console.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class CommandLineApp {
    private Scanner scanner;
    private boolean running;
    private SubjectDAO subjectDAO;
    private LecturerDAO lecturerDAO;
    private RoomDAO roomDAO;

    /**
     * The actual code that the user interacts with.
     * This is the method that listens for input from the user,
     * as well as sending it further in the stack to parse the input
     * and do whatever the user asked for.
     */
    public void start() {
        setup();
        scanner = new Scanner(System.in);
        running = true;

        // we are using jansi to color our console output, so this is to enable the ANSI escape sequences to System.out
        //AnsiConsole.systemInstall();
        System.out.println(
                ansi().eraseScreen().render(
                        "@|green " +
                                "This is the CLI  for browsing the database." +
                                "\nType \"help\" for a list of available commands." +
                        "|@"
                )
        );


        while (running) {
            System.out.print(">>> ");
            String input = "";
            input = scanner.nextLine();
            findCommand(input);
        }
    }

    /**
     * Find a command and run it.
     *
     * @param input Command to be run
     */
    private void findCommand(String input) {
        String[] splitInput = input.toLowerCase().split(" ");
        if (splitInput.length > 0)
        switch (splitInput[0]) {
            case "list":
                if (splitInput.length > 1) {
                    listAll(splitInput[1]);
                } else {
                    printUsage("list");
                }
                break;
            case "search":
                if (splitInput.length > 2) {
                    List<String> refinedInput = new ArrayList<>();
                    for (int i = 1; i < splitInput.length; i++) {
                        refinedInput.add(splitInput[i]);
                    }
                    search(refinedInput);
                } else {
                    printUsage("search");
                }
                break;
            case "help":
                if (splitInput.length > 1) {
                    printHelp(splitInput[1]);
                } else {
                    printHelp();
                }
                break;
        }
    }

    /**
     * The list of usages for a command.
     *
     * @param cmd Command to list usage for
     */
    private void printUsage(String cmd) {
        System.out.print(ansi().fgBright(BLUE));
        switch (cmd) {
            case "list":
                System.out.println("Usage: list lecturer|subject|room");
                break;
            case "search":
                System.out.println("Usage: search (lecturer|subject|room <search term>)");
                break;
        }
        System.out.print(ansi().reset());
    }


    private void search(List<String> args) {
        switch (args.get(0)) {
            case "lecturer":
                args.remove(0);
                String lecturerName = args.stream().collect(Collectors.joining(" "));
                LecturerDTO lecturerDTO = lecturerDAO.find(lecturerName);
                printTableHeader("lecturer");
                if (lecturerDTO != null)
                    viewLecturer(lecturerDTO);
                else
                    System.out.println("No result was found.");
                break;
            case "subject":
                args.remove(0);
                String subjectName = args.stream().collect(Collectors.joining(" "));
                SubjectDTO subjectDTO = subjectDAO.find(subjectName);
                printTableHeader("subject");
                if (subjectDTO != null)
                    viewSubject(subjectDTO);
                else
                    System.out.println("No result was found.");
                break;
            case "room":
                RoomDTO roomDTO = roomDAO.find(args.get(1));
                printTableHeader("room");
                if (roomDTO != null)
                    viewRoom(roomDTO);
                else
                    System.out.println("No result was found.");
                break;
            default:
                printUsage("search");
                break;
        }
    }


    /**
     * Prints the header for the table.
     *
     * @param header The header to print
     */
    private void printTableHeader(String header) {
        switch (header) {
            case "lecturer":
                System.out.println(ansi().fg(CYAN).a(String.format("%-30s %-30s %-10s %-10s %s",
                        "Name", "Email", "Number", "Subject", "Available")));
                System.out.println(ansi().a("-----------------------------------------------------------------------------------------------").reset());
                break;
            case "subject":
                System.out.println(ansi().fg(CYAN).a(String.format("%-30s %-10s %-10s %s", "Subject", "Code", "Enrolled", "Lecturer(s)")));
                System.out.println(ansi().a("-------------------------------------------------------------------").reset());
                break;
            case "room":
                System.out.println(ansi().fg(CYAN).a(String.format("%-15s %-15s %-15s", "Room", "Capacity", "Campus")));
                System.out.println(ansi().a("---------------------------------------------").reset());
                break;
        }
    }

    /**
     * This method is used to list all information about a specific item.
     *
     * @param arg The item to view all of
     */
    private void listAll(String arg) {
        switch (arg) {
            case "lecturer":
                List<LecturerDTO> lecturers = lecturerDAO.list();
                printTableHeader("lecturer");
                if (lecturers.size() == 0) System.out.println("None");
                for (LecturerDTO lecturer : lecturers) {
                   viewLecturer(lecturer);
                }
                break;
            case "subject":
                List<SubjectDTO> subjects = subjectDAO.list();
                printTableHeader("subject");
                if (subjects.size() == 0) System.out.println("None");
                for (SubjectDTO subject : subjects) {
                    viewSubject(subject);
                }
                break;
            case "room":
                List<RoomDTO> rooms = roomDAO.list();
                printTableHeader("room");
                if (rooms.size() == 0) System.out.println("None");
                for (RoomDTO room : rooms) {
                    viewRoom(room);
                }
                break;
            default:
                printUsage("list");
                break;

        }
    }

    /**
     * Prints information about a room to the console.
     *
     * @param roomDTO The room to print information about
     */
    private void viewRoom(RoomDTO roomDTO) {
        if (roomDTO != null) {
            System.out.println(String.format("%-15s %-15s %-15s",
                    roomDTO.getName(),
                    roomDTO.getCapacity(),
                    roomDTO.getCampus()
            ));
        }
    }

    /**
     * Prints information about a subject to the console.
     *
     * @param subjectDTO The subject to print information about
     */
    private void viewSubject(SubjectDTO subjectDTO) {
        if (subjectDTO != null) {
            System.out.println(String.format("%-30s %-10s %-10d %s",
                    subjectDTO.getName(),
                    subjectDTO.getShortName(),
                    subjectDTO.getEnrolled(),
                    subjectDTO.getLecturerNames()));
        }
    }

    /**
     * Prints information about a lecturer to the console.
     *
     * @param lecturerDTO The lecturer to print information about
     */
    private void viewLecturer(LecturerDTO lecturerDTO) {
        if (lecturerDTO != null) {
            // here we copy all the info from the lecturer object into their own array (for readability),
            // as well as fetch any subjects the lecturer has
            List<SubjectDTO> subjects = subjectDAO.findSubject(lecturerDTO);
            List<ContactDTO> contacts = lecturerDTO.getContact();
            List<AvailableDTO> availables = lecturerDTO.getAvailable();

            // here we check which of the three arrays we copied has the most content, so we know how many
            // times to loop later in the code.
            int maxLength = 0;
            if (subjects.size() > maxLength)
                maxLength = subjects.size();
            if (contacts.size() > maxLength)
                maxLength = contacts.size();
            if (availables.size() > maxLength)
                maxLength = availables.size();

            // very bad hack to prevent IndexOutOfBoundsException later in the code
            // we make all the arrays the same size, so we can easily iterate through them all later
            while (subjects.size() != maxLength)
                subjects.add(null);
            while (contacts.size() != maxLength)
                contacts.add(null);
            while (availables.size() != maxLength)
                availables.add(null);


            System.out.println(String.format("%-30s %-30s %-10s %-10s %s",
                    lecturerDTO.getName(),
                    contacts.get(0) != null ? contacts.get(0).getEmail() : "None",
                    contacts.get(0) != null ? contacts.get(0).getNumber() : "None",
                    subjects.get(0) != null ? subjects.get(0).getShortName() : "None",
                    availables.get(0) != null ? availables.get(0).getStart() + " - " + availables.get(0).getEnd() : "None"

            ));

            // we break out of listing lecturers if the highest array
            // was empty or only had one item.
            if (maxLength == 0 || maxLength == 1)
                return;

            // we iterate through every array we copied to display new info
            for (int i = 1; i < maxLength; i++) {
                System.out.println(String.format("%-30s %-30s %-10s %-10s %s",
                        "",
                        contacts.get(i) != null ? contacts.get(i).getEmail() : "",
                        contacts.get(i) != null ? contacts.get(i).getNumber() : "",
                        subjects.get(i) != null ? subjects.get(0).getShortName() : "",
                        availables.get(i) != null ? availables.get(i).getStart() + " - " + availables.get(i).getEnd() : ""
                ));
            }
            System.out.println();
        }
    }

    /**
     * Used to print the help menu.
     * Tells the user how to interact with the application.
     *
     * @param cmd Optional array of commands, used to get help about a specific command
     */
    private void printHelp(String... cmd) {
        if (cmd.length > 0) {
            switch (cmd[0]) {
                case "search":
                    System.out.println(ansi().fg(CYAN).a("Search:" + "\n-------------------------------").reset());
                    printUsage("search");
                    System.out.println("The search command is used to find information.");
                    System.out.println("You can use search using lecturer name, subject code, room number, phone number or email.");
                    System.out.println(ansi().fgBright(MAGENTA).a("Usage examples:").reset());
                    System.out.println("search lecturer Praskovya Pokrovskaya");
                    System.out.println("search subject PGR200");
                    System.out.println("search email pl@jaworska.com");
                    System.out.println(ansi().fg(CYAN).a("-------------------------------").reset());
                    break;
                case "list":
                    System.out.println(ansi().fg(CYAN).a("List:" + "\n-------------------------------").reset());
                    printUsage("list");
                    System.out.println("The list command is used to list everything about a single item.");
                    System.out.println("You can use it to view all lecturers, subjects or rooms.");
                    System.out.println(ansi().fgBright(MAGENTA).a("Usage examples:").reset());
                    System.out.println("list lecturer");
                    System.out.println(ansi().fg(CYAN).a("-------------------------------").reset());
                    break;
                default:
                    System.out.println(ansi().fg(RED).a("The command '" + cmd[0] + "' does not exist.\n").reset());
                    break;
            }
        } else {

            System.out.println(ansi().fg(CYAN).a("Help:\n----------------------------").reset());
            System.out.println(ansi().fg(MAGENTA).a("The following commands are available. \nFor more information, type \"help <cmd>\".").reset());
            System.out.println(ansi().fg(RED).a("search"));
            System.out.println("list");
            System.out.print(ansi().reset());
        }
    }

    /**
     * The for starting the database initialization, as well as setting up the DAOs to be used by the application
     */
    private void setup() {
        Properties creds = ResourceFetcher.getCredentials();
        DatabaseConnector connector =
                new DatabaseConnector(
                        creds.getProperty("db_user"),
                        creds.getProperty("db_password"),
                        creds.getProperty("db_host"),
                        creds.getProperty("db_database")
                );
        DatabaseInitializer initializer = new DatabaseInitializer(connector);
        initializer.initializeDatabase(false);
        subjectDAO = new SubjectDAO(connector);
        lecturerDAO = new LecturerDAO(connector);
        roomDAO = new RoomDAO(connector);
    }
}
