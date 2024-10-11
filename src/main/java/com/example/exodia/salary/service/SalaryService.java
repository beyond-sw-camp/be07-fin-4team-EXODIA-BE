package com.example.exodia.salary.service;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.repository.SalaryRepository;
import com.example.exodia.user.domain.User;
import lombok.RequiredArgsConstructor;
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

    private final double NATIONAL_PENSION_RATE = 0.045;
    private final double HEALTH_INSURANCE_RATE = 0.03545;
    private final double LONG_TERM_CARE_INSURANCE_RATE = 0.004591;
    private final double EMPLOYMENT_INSURANCE_RATE = 0.009;

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
        double nationalPension = salary.getBaseSalary() * NATIONAL_PENSION_RATE;
        double healthInsurance = salary.getBaseSalary() * HEALTH_INSURANCE_RATE;
        double longTermCare = salary.getBaseSalary() * LONG_TERM_CARE_INSURANCE_RATE;
        double employmentInsurance = salary.getBaseSalary() * EMPLOYMENT_INSURANCE_RATE;
        double totalTax = nationalPension + healthInsurance + longTermCare + employmentInsurance;

        salary.getTaxAmount().setNationalPension(nationalPension);
        salary.getTaxAmount().setHealthInsurance(healthInsurance);
        salary.getTaxAmount().setLongTermCare(longTermCare);
        salary.getTaxAmount().setEmploymentInsurance(employmentInsurance);
        salary.getTaxAmount().setTotalTax(totalTax);

        salary.setFinalSalary(salary.getBaseSalary() - totalTax);
    }

    @Transactional(readOnly = true)
    public List<Salary> getAllSalaries() {
        return salaryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Salary> getSalaryByUserNum(String userNum) {
        return salaryRepository.findByUser_UserNum(userNum);
    }

    @Transactional(readOnly = true)
    public List<Salary> getSalariesByPosition(Long positionId) {
        return salaryRepository.findByUser_Position_Id(positionId);
    }

    // 입사일 기준으로 몇 년차인지 계산
    public int calculateYearsOfService(User user) {
        LocalDate joinDate = user.getCreatedAt().toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(joinDate, currentDate).getYears();
    }

}


