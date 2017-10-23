package no.kij.scheduler.dto;

/**
 * RoomDTO is the Data Transfer Object for all rooms.
 * It contains only information about the room.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class RoomDTO {
    private transient Integer id;
    private int capacity;
    private String name;
    private String campus;

    public RoomDTO(int capacity, String name, String campus) {
        this.id = null;
        this.capacity = capacity;
        this.name = name;
        this.campus = campus;
    }

    public RoomDTO() {
        this.id = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }
}
