package com.example.exodia.salary.domain;

import com.example.exodia.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double baseSalary;

    @Embedded
    private TaxAmount taxAmount = new TaxAmount();

    private double finalSalary;

    @Embeddable
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxAmount {
        private double nationalPension;
        private double healthInsurance;
        private double longTermCare;
        private double employmentInsurance;
        private double totalTax;
    }
}
