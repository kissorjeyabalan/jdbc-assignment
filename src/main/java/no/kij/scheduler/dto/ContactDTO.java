package no.kij.scheduler.dto;

/**
 * ContactDTO is the Data Transfer Object for all contact information. It only contains information regarding the contact,
 * as well as the information from other DTOs it is dependent of.
 *
 * @author Kissor Jeyabalan
 * @see LecturerDTO
 * @since 1.0
 */
public class ContactDTO {
    private transient Integer id;
    private String number;
    private String email;

    public ContactDTO(String number, String email) {
        this.id = null;
        this.number = number;
        this.email = email;
    }

    public ContactDTO() {
        this.id = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
