package net.javaguides.springboot.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Entity
@Getter
@Setter
public class Resume {
    @Id
    private Long resume_id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "resume_id")
    @JsonIgnoreProperties({"password", "role", "resume", "login"})
    private User user;

    @NotNull
    @Column(nullable = false)
    private String education;

    @NotNull
    @Column(nullable = false)
    private String skills;

    @NotNull
    @Column(nullable = false)
    private Date birthday;

    private String gender;

    @NotNull
    @Column(nullable = false)
    private String full_name;

    @NotNull
    @Column(nullable = false)
    private String contact;

    @Column(length = 1000)
    private String description;

    public Resume() {
    }

    public Resume(User user, String education, String skills, Date birthday, String gender, String full_name, String contact, String description) {
        this.user = user;
        this.education = education;
        this.skills = skills;
        this.birthday = birthday;
        this.gender = gender;
        this.full_name = full_name;
        this.contact = contact;
        this.description = description;
    }
}
