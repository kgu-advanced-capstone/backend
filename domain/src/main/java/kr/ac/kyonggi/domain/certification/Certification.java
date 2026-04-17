package kr.ac.kyonggi.domain.certification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "certifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String issuingOrganization;

    @Column(nullable = false)
    private LocalDate issuedDate;

    public static Certification create(Long userId, String name, String issuingOrganization,
                                       LocalDate issuedDate) {
        Certification certification = new Certification();
        certification.userId = userId;
        certification.name = name;
        certification.issuingOrganization = issuingOrganization;
        certification.issuedDate = issuedDate;
        return certification;
    }

    public void update(String name, String issuingOrganization, LocalDate issuedDate) {
        this.name = name;
        this.issuingOrganization = issuingOrganization;
        this.issuedDate = issuedDate;
    }
}
