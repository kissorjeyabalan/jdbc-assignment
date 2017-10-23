package no.kij.scheduler.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LecturerDTO is the Data Transfer Object for all lecturers. It only contains information regarding the lecturer,
 * as well as the information from other DTOs the lecturer is dependent on.
 *
 * @author Kissor Jeyabalan
 * @see ContactDTO
 * @see AvailableDTO
 * @see SubjectDTO
 * @since 1.0
 */
public class LecturerDTO {
    private transient Integer id;
    private String name;
    private List<ContactDTO> contact;
    private List<AvailableDTO> available;

    public LecturerDTO(String name) {
        this.id = null;
        this.name = name;
        this.contact = new ArrayList<>();
        this.available = new ArrayList<>();
    }

    public LecturerDTO() {
        this.id = null;
        this.contact = new ArrayList<>();
        this.available = new ArrayList<>();
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

    public List<ContactDTO> getContact() {
        return contact;
    }

    public void setContact(List<ContactDTO> contact) {
        this.contact = contact;
    }

    public List<AvailableDTO> getAvailable() {
        return available;
    }

    public void setAvailable(List<AvailableDTO> available) {
        this.available = available;
    }

    public void addAvailable(AvailableDTO available) {
        this.available.add(available);
    }

    public void addContact(ContactDTO contact) {
        this.contact.add(contact);
    }

}
