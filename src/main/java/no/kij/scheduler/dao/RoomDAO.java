package no.kij.scheduler.dao;

import static no.kij.scheduler.dao.DAOUtil.*;

import no.kij.scheduler.DatabaseConnector;
import no.kij.scheduler.dto.RoomDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DAO for rooms. It's job is to do CRUD operations related to a room,
 * without letting the user know how the database is implemented. It requires a database connector to reuse pooled
 * connections.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class RoomDAO {
    private DatabaseConnector connector;
    private final String INSERT_QUERY = "INSERT INTO Room(name, capacity, campus) VALUES (?, ?, ?)";
    private final String VIEW_ALL_QUERY = "SELECT id, name, capacity, campus FROM Room";
    private final String FIND_BY_NAME = "SELECT id, name, capacity, campus FROM Room WHERE name = ?";

    /**
     * Used to create a DAO for the RoomDTO.
     * @param connector DatabaseConnector to pool connections from
     */
    public RoomDAO(DatabaseConnector connector) {
        this.connector = connector;
    }

    public RoomDTO find(String name) {
        return find(FIND_BY_NAME, name);
    }

    /**
     * Private implementation to find the given room
     * @param query Query to be ran towards the database
     * @param values Value to bind to the query
     * @return RoomDTO if found, null if not
     */
    private RoomDTO find(String query, Object... values) {
        RoomDTO roomDTO = null;
        try (
                Connection conn = connector.getConnection();
                PreparedStatement stmt = prepareStatement(conn, query, false, values);
                ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                roomDTO = bind(rs);
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while finding the room.");
            System.err.println(e.getMessage());
        }
        return roomDTO;
    }

    /**
     * Retrieve a list of rooms in the database.
     * @return List containing all the rooms in the database.
     */
    public List<RoomDTO> list() {
        List<RoomDTO> roomDTOs = new ArrayList<>();

        try (
                Connection conn = connector.getConnection();
                PreparedStatement stmt = prepareStatement(conn, VIEW_ALL_QUERY, false);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                roomDTOs.add(bind(rs));
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while fetching the list of rooms.");
            System.err.println(e.getMessage());
        }
        return roomDTOs;
    }


    /**
     * Persists the room to the database
     * @param roomDTO RoomDTO containing the information regarding the room
     * @throws IllegalArgumentException If Room ID is not null
     */
    public void create(RoomDTO roomDTO) throws IllegalArgumentException, DAOException {
        if (roomDTO.getId() != null) {
            throw new IllegalArgumentException("Room ID must be null");
        }

        Object[] values = {
                roomDTO.getName(),
                roomDTO.getCapacity(),
                roomDTO.getCampus()
        };

        try (
            Connection conn = connector.getConnection();
            PreparedStatement stmt = prepareStatement(conn, INSERT_QUERY, true, values)
        ) {
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Could not save room, no rows were affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    roomDTO.setId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating room failed, no ID was returned by the database");
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not save the room to the database.");
            throw new DAOException(e);
        }
    }

    /**
     * Binds the given ResultSet to a RoomDTO.
     * @param rs ResultSet to be bound to RoomDTO
     * @return Bound RoomDTO - empty upon failure
     */
    private RoomDTO bind(ResultSet rs) {
        RoomDTO roomDTO = new RoomDTO();
        try {
            roomDTO.setId(rs.getInt("id"));
            roomDTO.setName(rs.getString("name"));
            roomDTO.setCapacity(rs.getInt("capacity"));
            roomDTO.setCampus(rs.getString("campus"));
        } catch (SQLException e) {
            System.err.println("Something went wrong while binding the room, returning empty room.");
            System.err.println(e.getMessage());
        }
        return roomDTO;
    }
}
