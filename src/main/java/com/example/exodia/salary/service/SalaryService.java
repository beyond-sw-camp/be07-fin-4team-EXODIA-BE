package com.example.exodia.salary.service;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.repository.SalaryRepository;
import com.example.exodia.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 사원의 연봉 명세서 조회 (사원 개인용)
    @Transactional(readOnly = true)
    public Salary getSalarySlip(User user) {
        Optional<Salary> salaryOpt = salaryRepository.findByUser(user);

        if (salaryOpt.isPresent()) {
            Salary salary = salaryOpt.get();
            calculateFinalSalary(salary);
            return salary;
        } else {
            throw new IllegalArgumentException("해당 사용자의 연봉 정보를 찾을 수 없습니다.");
        }
    }

    // 세금 차감 후 최종 연봉 계산 로직
    private void calculateFinalSalary(Salary salary) {
        double taxAmount = salary.getBaseSalary() * (NATIONAL_PENSION_RATE + HEALTH_INSURANCE_RATE + LONG_TERM_CARE_INSURANCE_RATE + EMPLOYMENT_INSURANCE_RATE);
        salary.setTaxAmount(taxAmount);
        salary.setFinalSalary(salary.getBaseSalary() - taxAmount);
    }

    @Transactional(readOnly = true)
    public List<Salary> getAllSalaries() {
        return salaryRepository.findAll();
    }


    @Transactional
    public Salary saveSalary(Salary salary) {
        return salaryRepository.save(salary);
    }

    @Transactional
    public void deleteSalary(Long salaryId) {
        salaryRepository.deleteById(salaryId);
    }
}
