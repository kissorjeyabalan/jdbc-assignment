package no.kij.scheduler.dao;

import static no.kij.scheduler.dao.DAOUtil.*;
import no.kij.scheduler.DatabaseConnector;
import no.kij.scheduler.dto.LecturerDTO;
import no.kij.scheduler.dto.SubjectDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DAO for subjects. It's job is to do CRUD operations related to a subject,
 * without letting the user know how the database is implemented. It requires a database connector to reuse pooled
 * connections.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class SubjectDAO {
    private final String INSERT_QUERY = "INSERT INTO Subject(name, shortname, enrolled) VALUES (?, ?, ?)";
    private final String ADD_LECTURER_QUERY = "INSERT INTO Subject_Lecturer(subject, lecturer) VALUES (?, ?)";
    private final String VIEW_ALL_QUERY = "SELECT id, name, shortname, enrolled FROM Subject";
    private final String FIND_BY_ID = "SELECT id, name, shortname, enrolled FROM Subject WHERE id = ?";
    private final String FIND_BY_SHORT = "SELECT id, name, shortname, enrolled FROM Subject WHERE shortname = ?";
    private final String FIND_BY_NAME = "SELECT id, name, shortname, enrolled FROM Subject WHERE name = ?";
    private final String FIND_BY_NAME_LIKE = "SELECT id, name, shortname, enrolled FROM Subject WHERE name LIKE ?";
    private DatabaseConnector connector;

    /**
     * Used to create a DAO for the SubjectDTO.
     *
     * @param connector DatabaseConnector to pool connections from
     */
    public SubjectDAO(DatabaseConnector connector) {
        this.connector = connector;
    }

    /**
     * Find a subject using it's ID in the database.
     * @param subjectId ID to query for
     * @return The subject if found, null if not
     */
    public SubjectDTO find(int subjectId) {
        return find(FIND_BY_ID, subjectId);
    }

    /**
     * Find a subject using a string as input, which is either short name, full name or a partial
     * @param subjectName Shortname, full name or partial name
     * @return
     */
    public SubjectDTO find(String subjectName) {
        SubjectDTO subjectDTO = null;
        subjectDTO = find(FIND_BY_SHORT, subjectName);

        if (subjectDTO == null) {
            subjectDTO = find(FIND_BY_NAME, subjectName);
        }

        if (subjectDTO == null) {
            subjectDTO = find(FIND_BY_NAME_LIKE, "%" + subjectName + "%");
        }

        return subjectDTO;
    }

    /**
     * The private implementation that will fetch the subject from the database.
     *
     * @param query Query to retrieve from subject table
     * @param values Values to insert into query
     * @return The subject that was found in the database
     */
    private SubjectDTO find(String query, Object... values) {
        SubjectDTO subjectDTO = null;
        try (
                Connection conn = connector.getConnection();
                PreparedStatement stmt = prepareStatement(conn, query, false, values);
                ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                subjectDTO = bind(rs);
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while finding the subject.");
            System.err.println(e.getMessage());
        }
        return subjectDTO;
    }

    /**
     * Persists the subject to the database.
     *
     * @param subjectDTO SubjectDTO containing the information regarding the subject
     * @throws IllegalArgumentException If subject ID is not null
     * @throws DAOException If something goes wrong while persisting to the database
     */
    public void create(SubjectDTO subjectDTO) {
        if (subjectDTO.getId() != null) {
            throw new IllegalArgumentException("Subject ID must be null.");
        }

        Object[] values = {
            subjectDTO.getName(),
            subjectDTO.getShortName(),
            subjectDTO.getEnrolled()
        };

        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn, INSERT_QUERY, true, values)
        ) {
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Could not create subject, no rows were affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    subjectDTO.setId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating subject failed, no ID was returned by the DB");
                }
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong with saving the subject to the database.");
            throw new DAOException(e);
        }
    }

    /**
     * Fetches a list containing all the subjects in the database.
     *
     * @return List of subjects
     * @throws DAOException If something goes wrong while fetching the list of subjects
     */
    public List<SubjectDTO> list() {
        List<SubjectDTO> subjectDTOs = new ArrayList<>();

        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn, VIEW_ALL_QUERY, false);
            ResultSet rs = stmt.executeQuery();
        ) {
            while (rs.next()) {
                subjectDTOs.add(bind(rs));
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while fetching the subjects.");
            throw new DAOException(e);
        }
        return subjectDTOs;
    }

    /**
     * Link a given lecturer to a given subject.
     *
     * @param subjectDTO Subject to add the lecturer to
     * @param lecturerDTO Lecturer to add the subject to
     * @throws IllegalArgumentException Throws if subject ID or lecturerID is null
     * @throws DAOException If something goes wrong with the query link the lecturer and subject
     */
    public void addLecturer(SubjectDTO subjectDTO, LecturerDTO lecturerDTO) {
        if (subjectDTO.getId() == null) {
            throw new IllegalArgumentException("Subject ID can not be null");
        } else if (lecturerDTO.getId() == null) {
            throw new IllegalArgumentException("Lecturer ID can not be null");
        }

        Object[] values = {
                subjectDTO.getId(),
                lecturerDTO.getId()
        };

        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn, ADD_LECTURER_QUERY, false, values)
        ) {
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Could not link lecturer to subject.");
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong when linking lecturer and subject together.");
            throw new DAOException(e);
        }
    }

    /**
     * Binds given ResultSet to a SubjectDTO.
     *
     * @param rs ResultSet to bind
     * @return Bound SubjectDTO
     */
    private SubjectDTO bind(ResultSet rs) {
        SubjectDTO subjectDTO = new SubjectDTO();
        try {
            subjectDTO.setId(rs.getInt("id"));
            subjectDTO.setName(rs.getString("name"));
            subjectDTO.setShortName(rs.getString("shortname"));
            subjectDTO.setEnrolled(rs.getInt("enrolled"));

            LecturerDAO lecturerDAO = new LecturerDAO(connector);
            for (Integer lecturerId : findLecturerId(subjectDTO.getId())) {
                subjectDTO.addLecturer(lecturerDAO.find(lecturerId));
            }
        } catch (SQLException e) {
            System.err.println("Could not bind ResultSet to Subject. Returning empty.");
            System.err.println(e.getMessage());
        }
        return subjectDTO;
    }

    /**
     * Find subjects belong to a specific lecturer.
     * @param lecturerDTO lecturer to find subjects for
     * @return List of subjects
     */
    public List<SubjectDTO> findSubject(LecturerDTO lecturerDTO) {
        List<Integer> subjectIds = new ArrayList<>();
        List<SubjectDTO> subjectDTOs = new ArrayList<>();
        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn,
                    "SELECT subject FROM subject_lecturer WHERE lecturer = ?",
                    false,
                    lecturerDTO.getId());
            ResultSet rs = stmt.executeQuery();
        ) {
            while (rs.next()) {
                subjectIds.add(rs.getInt("subject"));
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while querying for subjects. Returning empty.");
            System.err.println(e.getMessage());
        }

        if (subjectIds.size() != 0) {
            for (Integer subjectId : subjectIds) {
                subjectDTOs.add(find(subjectId));
            }
        }
        return subjectDTOs;
    }

    /**
     * Private implementation to retrieve all lecturers linked to the given subject ID.
     * @param subjectId ID of subject to find lecturers for
     * @return List of IDs belonging to the lecturers of this subjects
     */
    private List<Integer> findLecturerId(int subjectId) {
        List<Integer> lecturerIds = new ArrayList<>();
        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn,
                    "SELECT lecturer FROM Subject_Lecturer WHERE subject = ?", false, subjectId);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lecturerIds.add(rs.getInt("lecturer"));
            }
        } catch (SQLException e) {
            System.err.println("Could not find list of lecturer IDs for the subject.");
            System.err.println(e.getMessage());
        }
        return lecturerIds;
    }
}
