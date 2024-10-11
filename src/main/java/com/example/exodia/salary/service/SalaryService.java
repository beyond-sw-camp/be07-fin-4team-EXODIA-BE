package com.example.exodia.salary.service;

import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.repository.SalaryRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
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
    private final UserRepository userRepository;  // 사용자 정보 조회를 위해 추가

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

    // 사번을 기반으로 급여 상세 정보 조회
    @Transactional(readOnly = true)
    public Optional<Salary> getSalaryByUserNum(String userNum) {
        Optional<User> user = userRepository.findByUserNum(userNum);
        if (user.isPresent()) {
            return salaryRepository.findByUser(user.get());
        }
        return Optional.empty();
    }

    // 세금 차감 후 최종 연봉 계산 로직
    private void calculateFinalSalary(Salary salary) {
        double taxAmount = salary.getBaseSalary() * (NATIONAL_PENSION_RATE + HEALTH_INSURANCE_RATE + LONG_TERM_CARE_INSURANCE_RATE + EMPLOYMENT_INSURANCE_RATE);
        salary.getTaxAmount().setTotalTax(taxAmount);
        salary.setFinalSalary(salary.getBaseSalary() - taxAmount);
    }

    @Transactional(readOnly = true)
    public List<Salary> getAllSalaries() {
        return salaryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Salary> getSalariesByPosition(Long positionId) {
        return salaryRepository.findByUser_Position_Id(positionId);
    }

    // 입사일을 기준으로 몇 년차인지 계산
    public int calculateYearsOfService(User user) {
        LocalDate joinDate = user.getCreatedAt().toLocalDate();  // 입사일
        LocalDate currentDate = LocalDate.now();
        return Period.between(joinDate, currentDate).getYears();  // 몇 년차인지 계산
    }

}
