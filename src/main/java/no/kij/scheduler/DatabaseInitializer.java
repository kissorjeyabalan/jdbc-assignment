package no.kij.scheduler;

import com.google.gson.*;
import no.kij.scheduler.dao.LecturerDAO;
import no.kij.scheduler.dao.RoomDAO;
import no.kij.scheduler.dao.SubjectDAO;
import no.kij.scheduler.dto.LecturerDTO;
import no.kij.scheduler.dto.RoomDTO;
import no.kij.scheduler.dto.SubjectDTO;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class is used to initialize the database structure, as well as populate it with content.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class DatabaseInitializer {
    private DatabaseConnector connector;

    /**
     * Create an instance of the initializer.
     *
     * @param connector Connector to pool connections from
     */
    public DatabaseInitializer(DatabaseConnector connector) {
        this.connector = connector;
    }


    /**
     * Initializes the database structure.
     */
    public Boolean initializeDatabase(Boolean overwrite) {
        if (isFirstRun() || overwrite) {
            if (!overwrite) {
                System.out.println("Detected first run. Initializing database structure...");
            } else {
                System.out.println("Initialized database overwrite.");
            }
            try {
                // Queries to be run
                String[] sqlStatements = ResourceFetcher.getFile("database.sql").split(";");

                // Get a connection from the pool and create a new statement
                Connection conn = connector.getConnection();
                Statement stmt = conn.createStatement();

                if (overwrite) {
                    System.out.println("Dropping existing tables...");
                    stmt.executeUpdate("SET foreign_key_checks = 0");
                    stmt.executeUpdate("DROP TABLE available, contact, lecturer, room, subject, subject_lecturer");
                    stmt.executeUpdate("SET foreign_key_checks = 1");
                    System.out.println("All tables dropped.");
                    System.out.println("Recreating structure...");
                }
                // Run each query
                for (String sql : sqlStatements) {
                    stmt.executeUpdate(sql);
                }

                // Close the connections
                stmt.close();
                conn.close();

                System.out.println("Database structure successfully created!\n");

                System.out.println("Initializing rows...\n");
                initializeRows();
                System.out.println("Database initialization has been completed.\n\n");
                return true;
            } catch (SQLException e) {
                System.err.println("Something went wrong while initializing the database structure.");
                System.err.println(e.getMessage());
            }
        }
        return false;
    }

    private void initializeRows() {
        // Create a instance of gson, since it's a library we're using to map json to java objects
        Gson gson = new Gson();

        System.out.println("Inserting lecturers...");
        // fetch json containing the lecturers and save it to the database
        String json = ResourceFetcher.getFile("lecturers.json");
        LecturerDTO[] lecturerDTOs = gson.fromJson(json, LecturerDTO[].class);
        LecturerDAO lecturerDAO = new LecturerDAO(connector);

        for (LecturerDTO lecturerDTO : lecturerDTOs) {
            lecturerDAO.create(lecturerDTO);
        }
        System.out.println(lecturerDTOs.length + " lecturers has been inserted!\n");

        System.out.println("Inserting rooms...");
        // fetch json containing rooms and save it to the database
        json = ResourceFetcher.getFile("rooms.json");
        RoomDTO[] roomDTOs = gson.fromJson(json, RoomDTO[].class);
        RoomDAO roomDAO = new RoomDAO(connector);
        for (RoomDTO roomDTO : roomDTOs) {
            roomDAO.create(roomDTO);
        }
        System.out.println(roomDTOs.length + " rooms has been inserted!\n");


        System.out.println("Inserting subjects...");
        // fetch json containing the subjects and save it to the database
        json = ResourceFetcher.getFile("subjects.json");
        SubjectDTO[] subjectDTOs = gson.fromJson(json, SubjectDTO[].class);
        SubjectDAO subjectDAO = new SubjectDAO(connector);
        for (SubjectDTO subjectDTO : subjectDTOs) {
            subjectDAO.create(subjectDTO);
        }
        System.out.println(subjectDTOs.length + " subjects has been inserted!\n");

        // this is where we bind the lecturers to their subjects
        JsonParser parser = new JsonParser();
        JsonArray subjectJsonArr = parser.parse(json).getAsJsonArray();

        System.out.println("Linking subjects to lecturers...");
        for (int i = 0; i < subjectDTOs.length; i++) {
            JsonObject subjectObj = subjectJsonArr.get(i).getAsJsonObject();
            JsonArray lecturerArr = subjectObj.getAsJsonArray("lecturers");

            for (JsonElement lecturerElement : lecturerArr) {
                JsonObject lecturerObj = lecturerElement.getAsJsonObject();
                String lecturerName = lecturerObj.get("name").getAsString();
                if (lecturerName != null) {
                    LecturerDTO lecturerDTO = lecturerDAO.find(lecturerName);
                    if (lecturerDTO != null) {
                        subjectDAO.addLecturer(subjectDTOs[i], lecturerDTO);
                    }
                }
            }
        }
        System.out.println("Linking complete.\n");

    }

    /**
     * Check if the initializer has been run before.
     * @return True if first run, false if not
     */
    private boolean isFirstRun() {
        boolean firstRun = false;
        try {
            File file = new File("firstrun.txt");
            if(!file.exists()) {
                firstRun = true;
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Could not write to directory");
            System.err.println(e.getMessage());
        }
        return firstRun;
    }
}
