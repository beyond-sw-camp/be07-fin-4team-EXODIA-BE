package com.example.exodia.salary.service;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.dto.SalaryUpdateDto;
import com.example.exodia.salary.repository.SalaryRepository;
import com.example.exodia.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryRepository salaryRepository;

    // 보험 요율
    private final double NATIONAL_PENSION_RATE = 0.045;
    private final double HEALTH_INSURANCE_RATE = 0.03545;
    private final double LONG_TERM_CARE_INSURANCE_RATE = 0.004591;
    private final double EMPLOYMENT_INSURANCE_RATE = 0.009;
    private final double LOCAL_INCOME_TAX_RATE = 0.1; // 지방소득세

    // 소득세 과세표준 구간과 세율, 누진 공제액
    private final double[] INCOME_TAX_BASES = {14000000, 50000000, 88000000, 150000000, 300000000, 500000000, 1000000000};
    private final double[] INCOME_TAX_RATES = {0.06, 0.15, 0.24, 0.35, 0.38, 0.40, 0.42, 0.45};
    private final double[] INCOME_TAX_FIXED_AMOUNTS = {0, 1260000, 5670000, 14900000, 34200000, 62200000, 112200000};

    @Transactional(readOnly = true)
    public Salary getSalarySlip(User user) {
        Optional<Salary> salaryOpt = salaryRepository.findByUser(user);

        if (salaryOpt.isPresent()) {
            Salary salary = salaryOpt.get();
            calculateTaxes(salary); // 세금 계산 호출
            return salary;
        } else {
            throw new IllegalArgumentException("해당 사용자의 연봉 정보를 찾을 수 없습니다.");
        }
    }

    // 세금 항목 계산 및 Salary 객체에 적용
    private void calculateTaxes(Salary salary) {
        double baseSalary = salary.getBaseSalary();

        // 국민연금 계산
        double nationalPension = baseSalary * NATIONAL_PENSION_RATE;

        // 건강보험 계산
        double healthInsurance = baseSalary * HEALTH_INSURANCE_RATE;

        // 장기요양보험 계산
        double longTermCare = healthInsurance * LONG_TERM_CARE_INSURANCE_RATE;

        // 고용보험 계산
        double employmentInsurance = baseSalary * EMPLOYMENT_INSURANCE_RATE;

        // 종합소득세 계산 (소득에 따른 누진세율 적용)
        double taxableIncome = baseSalary - (nationalPension + healthInsurance + longTermCare + employmentInsurance);
        double incomeTax = calculateIncomeTax(taxableIncome);

        // 지방소득세 계산 (소득세의 10%)
        double localIncomeTax = incomeTax * LOCAL_INCOME_TAX_RATE;

        // 세금 총합
        double totalTax = nationalPension + healthInsurance + longTermCare + employmentInsurance + incomeTax + localIncomeTax;

        // 세금 항목들 Salary 객체에 저장
        salary.getTaxAmount().setNationalPension(nationalPension);
        salary.getTaxAmount().setHealthInsurance(healthInsurance);
        salary.getTaxAmount().setLongTermCare(longTermCare);
        salary.getTaxAmount().setEmploymentInsurance(employmentInsurance);
        salary.getTaxAmount().setTotalTax(totalTax);

        // 최종 실수령액
        salary.setFinalSalary(baseSalary - totalTax);
    }

    // 소득세 계산 로직
    private double calculateIncomeTax(double taxableIncome) {
        double tax = 0.0;

        // 과세표준에 따른 세율 적용
        for (int i = INCOME_TAX_BASES.length - 1; i >= 0; i--) {
            if (taxableIncome > INCOME_TAX_BASES[i]) {
                tax = INCOME_TAX_FIXED_AMOUNTS[i] + (taxableIncome - INCOME_TAX_BASES[i]) * INCOME_TAX_RATES[i + 1];
                break;
            }
        }
        return tax;
    }

    @Transactional(readOnly = true)
    public Page<Salary> getAllSalaries(Pageable pageable) {
        return salaryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Salary> getSalaryByUserNum(String userNum) {
        return salaryRepository.findByUser_UserNum(userNum);
    }

//    @Transactional(readOnly = true)
    public Page<Salary> getSalariesByPosition(Long positionId, Pageable pageable) {
        return salaryRepository.findByUser_Position_Id(positionId, pageable);
    }

    // 입사일 기준으로 몇 년차인지 계산
    public int calculateYearsOfService(User user) {
        LocalDate joinDate = user.getCreatedAt().toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(joinDate, currentDate).getYears();
    }

    @Transactional
    public Salary createSalaryForUser(User user) {
        double baseSalary = user.getPosition().getBaseSalary();

        Salary salary = Salary.builder()
                .user(user)
                .baseSalary(baseSalary)
                .build();

        calculateTaxes(salary);
        return salaryRepository.save(salary);
    }

    @Transactional
    public void updateSalary(SalaryUpdateDto salaryUpdateDto) {
        Salary salary = salaryRepository.findByUserUserNum(salaryUpdateDto.getUserNum())
                .orElseThrow(() -> new RuntimeException("Salary not found for user: " + salaryUpdateDto.getUserNum()));
        if (salaryUpdateDto.getBaseSalary() > 0) {
            salary.setBaseSalary(salaryUpdateDto.getBaseSalary());
        }
        if (salaryUpdateDto.getPercentageAdjustment() != null) {
            double adjustmentFactor = 1 + (salaryUpdateDto.getPercentageAdjustment() / 100);
            salary.setBaseSalary(salary.getBaseSalary() * adjustmentFactor);
        }
        if (salaryUpdateDto.getNewFinalSalary() != null) {
            salary.setFinalSalary(salaryUpdateDto.getNewFinalSalary());
        }
        salaryRepository.save(salary);
    }

    @Transactional
    public void updateSalaryByPercentage(String userNum, double percentage) {
        Salary salary = salaryRepository.findByUserUserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found"));
        double updatedBaseSalary = salary.getBaseSalary() + (salary.getBaseSalary() * (percentage / 100));
        salary.setBaseSalary(updatedBaseSalary);
        salary.setFinalSalary(calculateFinalSalary(salary));
        salaryRepository.save(salary);
    }

    private double calculateFinalSalary(Salary salary) {
        double totalTax = salary.getTaxAmount().getTotalTax();
        return salary.getBaseSalary() - totalTax;
    }

}
