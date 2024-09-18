package com.example.exodia.common.data;

import com.example.exodia.common.domain.DelYN;
import com.example.exodia.department.domain.Department;
import com.example.exodia.position.domain.Position;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.domain.Gender;
import com.example.exodia.user.domain.Status;
import com.example.exodia.user.domain.HireType;
import com.example.exodia.user.domain.NowStatus;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.department.repository.DepartmentRepository;
import com.example.exodia.position.repository.PositionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(DepartmentRepository departmentRepository,
                             PositionRepository positionRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        Department hrDepartment = new Department(null, "인사팀");
        Department webDevDepartment = new Department(null, "웹개발팀");
        departmentRepository.save(hrDepartment);
        departmentRepository.save(webDevDepartment);

        Position teamLeader = new Position(null, "팀장");
        Position director = new Position(null, "부장");
        positionRepository.save(teamLeader);
        positionRepository.save(director);

        String Password1 = passwordEncoder.encode("testtest");
        String Password2 = passwordEncoder.encode("testtest");

        User user1 = new User(
                "20240901001",
                null,
                "test1",
                Gender.M,
                Status.재직,
                Password1,
                "test1@test",
                "어양동",
                "01012345678",
                DelYN.N,
                "123456-1234567",
                HireType.정규직,
                NowStatus.출근,
                15,
                hrDepartment,
                teamLeader,
                0
        );

        User user2 = new User(
                "20240901002",
                null,
                "test2",
                Gender.W,
                Status.재직,
                Password2,
                "test2@test",
                "영등동",
                "01098765432",
                DelYN.N,
                "123456-2345678",
                HireType.계약직,
                NowStatus.회의,
                15,
                webDevDepartment,
                director,
                0
        );

        userRepository.save(user1);
        userRepository.save(user2);
    }
}
