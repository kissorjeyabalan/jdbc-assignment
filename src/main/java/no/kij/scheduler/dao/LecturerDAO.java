package no.kij.scheduler.dao;

import static no.kij.scheduler.dao.DAOUtil.*;

import no.kij.scheduler.DatabaseConnector;
import no.kij.scheduler.dto.AvailableDTO;
import no.kij.scheduler.dto.ContactDTO;
import no.kij.scheduler.dto.LecturerDTO;
import no.kij.scheduler.dto.SubjectDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DAO for lecturers. It's job is to do CRUD operations related to a lecturer,
 * without letting the user know how the database is implemented. It requires a database connector to reuse pooled
 * connections.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class LecturerDAO {
    private final String INSERT_QUERY = "INSERT INTO Lecturer(name) VALUES (?)";
    private final String FIND_BY_NAME_QUERY = "SELECT id, name FROM Lecturer WHERE name = ?";
    private final String VIEW_ALL_QUERY = "SELECT id, name FROM Lecturer";
    private final String FIND_BY_ID = "SELECT id, name FROM Lecturer WHERE id = ?";
    private DatabaseConnector connector;

    /**
     * Used to create a DAO for the LecturerDTO.
     * @param connector DatabaseConnector to pool connections from
     */
    public LecturerDAO(DatabaseConnector connector) {
        this.connector = connector;
    }


    /**
     * Used to persist a lecturer to the database
     * @param lecturerDTO Lecturer to save to the db
     * @throws IllegalArgumentException If lecturer ID is not null
     * @throws DAOException If lecturer could not be saved to the database
     */
    public void create(LecturerDTO lecturerDTO) throws IllegalArgumentException, DAOException {
        if (lecturerDTO.getId() != null) {
            // unchecked exception, since the programmer should know to pass lecturer with id (so it shouldn't happen)
            throw new IllegalArgumentException("Lecturer ID must be null. This lecturer already exists.");
        }

        Object [] values = { lecturerDTO.getName() };

        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn, INSERT_QUERY, true, values)
        ) {
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Could not create lecturer, no rows in database were affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    lecturerDTO.setId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating lecturer failed. No ID was returned by the database.");
                }
            }

            ContactDAO contactDAO = new ContactDAO();
            AvailableDAO availableDAO = new AvailableDAO();
            if (lecturerDTO.getContact().size() != 0) {
                for (ContactDTO contactDTO : lecturerDTO.getContact()) {
                    contactDAO.create(contactDTO, lecturerDTO);
                }
            }
            if (lecturerDTO.getAvailable().size() != 0) {
                for (AvailableDTO availableDTO : lecturerDTO.getAvailable()) {
                    availableDAO.create(availableDTO, lecturerDTO);
                }
            }

        } catch (SQLException e) {
            System.err.println("Something went wrong while creating the lecturer.");
            throw new DAOException(e);
        }
    }

    /**
     * Returns a list of all lecturers in the database
     * @return List of lecturers
     */
    public List<LecturerDTO> list() {
        List<LecturerDTO> lecturerDTOs = new ArrayList<>();

        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn, VIEW_ALL_QUERY, false);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lecturerDTOs.add(bind(rs));
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while fetching a list of lecturers.");
            System.err.println(e.getMessage());
        }
        return lecturerDTOs;
    }

    /**
     * Find a lecturer using ID.
     * @param id Database ID for the lecturer
     * @return LecturerDTO if lecturer was found, null if not
     */
    public LecturerDTO find(int id) {
        return find(FIND_BY_ID, id);
    }

    /**
     * Find a lecturer using their name.
     * @param name The name of the lecturer
     * @return LecturerDTO if lecturer was found, null if not
     */
    public LecturerDTO find(String name) {
        return find(FIND_BY_NAME_QUERY, name);
    }

    /**
     * The private implemention doing the operation for retrieving the lecturer from the database.
     *
     * @param query Query to be ran
     * @param values The value(s) to fill the query with
     * @return LecturerDTO if found, null if not
     */
    private LecturerDTO find(String query, Object... values) {
        LecturerDTO lecturerDTO = null;
        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn, query, false, values);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                lecturerDTO = bind(rs);
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while finding the lecturer.");
            System.err.println(e.getMessage());
        }
        return lecturerDTO;
    }

    /**
     * Private implementation to bind the resultset to a LecturerDTO.
     *
     * @param rs Resultset to be bound
     * @return LecturerDTO containing the resultset or empty LecturerDTO upon failure
     */
    private LecturerDTO bind(ResultSet rs) {
        LecturerDTO lecturerDTO = new LecturerDTO();
        try {
            lecturerDTO.setId(rs.getInt("id"));
            lecturerDTO.setName(rs.getString("name"));
            lecturerDTO.setContact(new ContactDAO().find(rs.getInt("id")));
            lecturerDTO.setAvailable(new AvailableDAO().find(rs.getInt("id")));

        } catch (SQLException e) {
            System.err.println("Something went wrong while binding the resultset to the DTO, returning empty LecturerDTO.");
            System.err.println(e.getMessage());
        }
        return lecturerDTO;
    }

    /**
     * Inner class for manipulating a contact in the database.
     * This is not public, since a contact should always go through a lecturer.
     */
    private class ContactDAO {
        private final String INSERT_QUERY = "INSERT INTO Contact(lecturer, number, email) VALUES (?, ?, ?)";
        private final String FIND_BY_LECTURER_ID =
                "SELECT id, lecturer, number, email FROM Contact WHERE lecturer = ?";

        /**
         * Used to persist a contact to the database.
         * @param contactDTO ContactDTO containing the contact information, ID must be null
         * @param lecturerDTO The lecturer to bind the contact information to - ID can not be null
         * @throws IllegalArgumentException If lecturer ID is null or contact ID is not null
         * @throws DAOException If contact could not be saved to the database
         */
        public void create(ContactDTO contactDTO, LecturerDTO lecturerDTO) throws IllegalArgumentException, DAOException {
            if (lecturerDTO.getId() == null) {
                throw new IllegalArgumentException("Lecturer ID can not be null.");
            } else if (contactDTO.getId() != null) {
                throw new IllegalArgumentException("Contact ID must be null.");
            }

            Object[] values = {
                    lecturerDTO.getId(),
                    contactDTO.getNumber(),
                    contactDTO.getEmail()
            };

            try(
                Connection conn = connector.getConnection();
                PreparedStatement stmt = prepareStatement(conn, INSERT_QUERY, true, values)
            ) {
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DAOException("Could not create contact, no rows were affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        contactDTO.setId(generatedKeys.getInt(1));
                    } else {
                        throw new DAOException("Creating contact failed. No ID was returned by the database.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Something went wrong while inserting to the database.");
                throw new DAOException(e);
            }
        }

        /**
         * Fetches all contact information for a specific lecturer.
         * @param lecturerId Lecturer ID to get the contact information for
         * @return List of contact information for that lecturer
         */
        public List<ContactDTO> find(int lecturerId) {
            List<ContactDTO> contactDTOs = new ArrayList<>();
            try(
                Connection conn = connector.getConnection();
                PreparedStatement stmt = prepareStatement(conn, FIND_BY_LECTURER_ID, false, lecturerId);
                ResultSet rs = stmt.executeQuery()
            ) {
                while (rs.next()) {
                    contactDTOs.add(bind(rs));
                }
            } catch (SQLException e) {
                System.err.println("Something went wrong while fetching the contact information for lecturer ID " + lecturerId);
                System.err.println(e.getMessage());
            }
            return contactDTOs;
        }

        /**
         * Private implementation to bind a resultset to a contact
         * @param rs Resultset to bind to contact
         * @return Contact containing the resulset - returns empty contact upon failure
         */
        private ContactDTO bind(ResultSet rs) {
            ContactDTO contactDTO = new ContactDTO();
            try {
                contactDTO.setId(rs.getInt("id"));
                contactDTO.setNumber(rs.getString("number"));
                contactDTO.setEmail(rs.getString("email"));
            } catch (SQLException e) {
                System.err.println("Something went wrong while binding the contact information.");
                System.err.println(e.getMessage());
            }
            return contactDTO;
        }
    }


    /**
     * Inner class for manipulating available times in the database.
     * This is not public, since availability is based on a lecturer - and therefore should go through said lecturer.
     */
    private class AvailableDAO {
        private final String INSERT_QUERY = "INSERT INTO Available(lecturer, start, end) VALUES (?, ?, ?)";
        private final String FIND_BY_LECTURER_ID =
                "SELECT id, start, end FROM Available WHERE lecturer = ?";


        /**
         * Persists the available time to the database
         * @param availableDTO AvailableDTO containing the information to be saved
         * @param lecturerDTO LecturerDTO to bind it to
         * @throws IllegalArgumentException If lecturer ID is null or available ID is not null
         * @throws DAOException If available could not be saved to the database
         */
        public void create(AvailableDTO availableDTO, LecturerDTO lecturerDTO) throws IllegalArgumentException, DAOException {
            if (lecturerDTO.getId() == null) {
                throw new IllegalArgumentException("Lecturer ID can not be null.");
            } else if (availableDTO.getId() != null) {
                throw new IllegalArgumentException("Available ID must be null.");
            }

            Object[] values = {
                    lecturerDTO.getId(),
                    availableDTO.getStart(),
                    availableDTO.getEnd()
            };

            try (
                Connection conn = connector.getConnection();
                PreparedStatement stmt = prepareStatement(conn, INSERT_QUERY, true, values)
            ) {
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DAOException("Could not create available, no rows were affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        availableDTO.setId(generatedKeys.getInt(1));
                    } else {
                        throw new DAOException("Creating available failed, no ID was returned by database.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Could not save available.");
                throw new DAOException(e);
            }
        }

        /**
         * Returns a list of available times a lecturer has
         * @param lecturerId ID of lecturer to look up available times for
         * @return List of available times to said lecturer
         */
        public List<AvailableDTO> find(int lecturerId) {
            List<AvailableDTO> availablesDTOs = new ArrayList<>();

            try (
                Connection conn = connector.getConnection();
                PreparedStatement stmt = prepareStatement(conn, FIND_BY_LECTURER_ID, false, lecturerId);
                ResultSet rs = stmt.executeQuery()
            ) {
                while (rs.next()) {
                    availablesDTOs.add(bind(rs));
                }
            } catch (SQLException e) {
                System.err.println("Something went wrong while fetching the list of available times for lecturer ID "
                        + lecturerId + ", returning empty lecturer list.");
                System.err.println(e.getMessage());
            }
            return availablesDTOs;
        }

        /**
         * Binds the given ResultSet to a AvailableDTO.
         * @param rs ResultSet to be bound to AvailableDTO
         * @return Bound AvailableDTO - empty upon failure
         */
        private AvailableDTO bind(ResultSet rs) {
            AvailableDTO availableDTO = new AvailableDTO();
            try {
                availableDTO.setId(rs.getInt("id"));
                availableDTO.setStart(rs.getInt("start"));
                availableDTO.setEnd(rs.getInt("end"));

            } catch (SQLException e) {
                System.err.println("Something went wrong while binding the available, returning empty available.");
                System.err.println(e.getMessage());
            }
            return availableDTO;
        }
    }
}
