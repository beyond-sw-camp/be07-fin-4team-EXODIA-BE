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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UserRepository userRepository;

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

        User user1 = new User(
                "20240901001",
                null,
                "Kim Minho",
                Gender.M,
                Status.재직,
                "testtest",
                "minho.kim@example.com",
                "Seoul, Korea",
                "01012345678",
                DelYN.N,
                "123456-1234567",
                HireType.정규직,
                NowStatus.출근,
                hrDepartment,
                teamLeader,
                0
        );

        User user2 = new User(
                "20240901002",
                null,
                "Lee Jiyoon",
                Gender.W,
                Status.재직,
                "testtest",
                "jiyoon.lee@example.com",
                "Busan, Korea",
                "01098765432",
                DelYN.N,
                "234567-2345678",
                HireType.계약직,
                NowStatus.회의,
                webDevDepartment,
                director,
                0
        );

        userRepository.save(user1);
        userRepository.save(user2);
    }
}
