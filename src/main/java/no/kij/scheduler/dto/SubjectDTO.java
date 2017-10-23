package no.kij.scheduler.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SubjectDTO is the Data Transfer Object for all subjects. It only contains information regarding the subject,
 * as well as the information from other DTOs it is dependent of.
 *
 * @author Kissor Jeyabalan
 * @see LecturerDTO
 * @since 1.0
 */
public class SubjectDTO {
    private transient Integer id;
    private String name;
    private String shortName;
    private int enrolled;
    private transient ArrayList<LecturerDTO> lecturers;

    public SubjectDTO(String name, String shortName, int enrolled) {
        this.id = null;
        this.name = name;
        this.shortName = shortName;
        this.enrolled = enrolled;
        this.lecturers = new ArrayList<>();
    }

    public SubjectDTO() {
        this.id = null;
        this.lecturers = new ArrayList<>();
    }

    public void addLecturer(LecturerDTO lecturerDTO) {
        lecturers.add(lecturerDTO);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(int enrolled) {
        this.enrolled = enrolled;
    }

    public ArrayList<LecturerDTO> getLecturers() {
        return lecturers;
    }

    public void setLecturers(ArrayList<LecturerDTO> lecturers) {
        this.lecturers = lecturers;
    }

    /**
     * Get only the names of the lecturers, delimited by comma.
     *
     * @return  String of lecturers
     */
    public String getLecturerNames() {
        List<String> lecturers = new ArrayList<>();
        for (LecturerDTO lecturer : this.lecturers) {
            lecturers.add(lecturer.getName());
        }

        if (lecturers.size() == 0) {
            return "None";
        } else {
            return lecturers.stream().collect(Collectors.joining(", "));
        }
    }
}
