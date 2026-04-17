package kr.ac.kyonggi.domain.education;

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
@Table(name = "educations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String schoolName;

    private String major;

    @Column(length = 50)
    private String degree;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    public static Education create(Long userId, String schoolName, String major, String degree,
                                   LocalDate startDate, LocalDate endDate) {
        Education education = new Education();
        education.userId = userId;
        education.schoolName = schoolName;
        education.major = major;
        education.degree = degree;
        education.startDate = startDate;
        education.endDate = endDate;
        return education;
    }

    public void update(String schoolName, String major, String degree,
                       LocalDate startDate, LocalDate endDate) {
        this.schoolName = schoolName;
        this.major = major;
        this.degree = degree;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
