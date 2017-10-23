package no.kij.scheduler.dto;

/**
 * AvailableDTO is the Data Transfer Object for all available timeslots. It only contains information regarding the available times,
 * as well as the information from other DTOs it is dependent of.
 *
 * @author Kissor Jeyabalan
 * @see LecturerDTO
 * @since 1.0
 */
public class AvailableDTO {
    private transient Integer id;
    private int start;
    private int end;

    public AvailableDTO(int start, int end) {
        this.id = null;
        this.start = start;
        this.end = end;
    }

    public AvailableDTO() {
        this.id = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
