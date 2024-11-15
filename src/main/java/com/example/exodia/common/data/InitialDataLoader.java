package com.example.exodia.common.data;


import com.example.exodia.car.domain.Car;
import com.example.exodia.car.repository.CarRepository;
import com.example.exodia.chat.domain.ChatRoom;
import com.example.exodia.chat.domain.ChatUser;
import com.example.exodia.chat.repository.ChatRoomRepository;
import com.example.exodia.chat.repository.ChatUserRepository;

import com.example.exodia.department.domain.Department;
import com.example.exodia.document.domain.Tag;
import com.example.exodia.document.repository.TagRepository;
import com.example.exodia.evalutionFrame.evalutionBig.domain.Evalutionb;
import com.example.exodia.evalutionFrame.evalutionBig.repository.EvalutionbRepository;
import com.example.exodia.evalutionFrame.evalutionMiddle.domain.Evalutionm;
import com.example.exodia.evalutionFrame.evalutionMiddle.repository.EvalutionmRepository;
import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.meetingRoom.repository.MeetingRoomRepository;
import com.example.exodia.position.domain.Position;
import com.example.exodia.salary.domain.Salary;
import com.example.exodia.salary.repository.SalaryRepository;
import com.example.exodia.submit.domain.SubmitType;
import com.example.exodia.submit.repository.SubmitTypeRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final EvalutionbRepository evalutionbRepository;
    private final EvalutionmRepository evalutionmRepository;
    private final PasswordEncoder passwordEncoder;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CarRepository carRepository;
    private final SubmitTypeRepository submitTypeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;

    private final double NATIONAL_PENSION_RATE = 0.045;
    private final double HEALTH_INSURANCE_RATE = 0.03545;
    private final double LONG_TERM_CARE_INSURANCE_RATE = 0.004591;
    private final double EMPLOYMENT_INSURANCE_RATE = 0.009;


    public InitialDataLoader(DepartmentRepository departmentRepository,
                             PositionRepository positionRepository,
                             UserRepository userRepository,
                             EvalutionbRepository evalutionbRepository,
                             EvalutionmRepository evalutionmRepository,
                             PasswordEncoder passwordEncoder,
                             MeetingRoomRepository meetingRoomRepository,
                             ChatRoomRepository chatRoomRepository,
                             ChatUserRepository chatUserRepository,
                             CarRepository carRepository,
                             SubmitTypeRepository submitTypeRepository,
                             SalaryRepository salaryRepository, TagRepository tagRepository) {  // Constructor에 추가
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.evalutionbRepository = evalutionbRepository;
        this.evalutionmRepository = evalutionmRepository;
        this.passwordEncoder = passwordEncoder;
        this.meetingRoomRepository = meetingRoomRepository;
        this.carRepository = carRepository;
        this.submitTypeRepository = submitTypeRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatUserRepository = chatUserRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (evalutionbRepository.count() == 0) {
            insertEvalutionData();
        }
        if (carRepository.count() == 0) {
            insertCarData();
        }
        if (submitTypeRepository.count() == 0) {
            insertSubmitTypeData();
        }
        if (chatRoomRepository.count() == 0) {
            insertChatRoomData();
        }
    }

    private void insertEvalutionData() {
        Evalutionb workAbility = new Evalutionb(null, "업무 수행 능력");
        Evalutionb problemSolving = new Evalutionb(null, "문제 해결");
        Evalutionb responsibility = new Evalutionb(null, "책임감");
        Evalutionb teamworkCommunication = new Evalutionb(null, "팀워크/의사소통");

        evalutionbRepository.saveAll(List.of(workAbility, problemSolving, responsibility, teamworkCommunication));

        List<Evalutionm> evalutionms = List.of(
                new Evalutionm(null, "업무 달성도", workAbility),
                new Evalutionm(null, "업무 처리 능력", workAbility),
                new Evalutionm(null, "문제 해결 능력", problemSolving),
                new Evalutionm(null, "주도성", problemSolving),
                new Evalutionm(null, "협력 태도", responsibility),
                new Evalutionm(null, "팀워크", teamworkCommunication),
                new Evalutionm(null, "의사소통", teamworkCommunication)
        );
        evalutionmRepository.saveAll(evalutionms);
    }

    private void insertCarData() {
        List<Car> cars = List.of(
                new Car(null, "121가123", "스타랙스", 11, 2.5, "starrex.jpg"),
                new Car(null, "135나894", "벤츠", 5, 3.0, "benz.jpg"),
                new Car(null, "753호159", "SUV", 7, 2.0, "suv.jpg"),
                new Car(null, "143라3451", "람보르기니", 2, 5.2, "lamborghini.jpg"),
                new Car(null, "429호7318", "G70", 5, 3.3, "g70.jpg"),
                new Car(null, "14라 8222", "소나타", 5, 2.0, "sonata.jpg"),
                new Car(null, "18유3752", "황금마티즈", 4, 0.8, "matiz.jpg")
        );
        carRepository.saveAll(cars);
    }

    private void insertSubmitTypeData() {
        List<SubmitType> submitTypes = List.of(
                new SubmitType(1L, "법인 카드 사용 신청서"),
                new SubmitType(2L, "휴가 신청서"),
                new SubmitType(3L, "경조사 신청서")
        );
        submitTypeRepository.saveAll(submitTypes);
    }

    private void insertChatRoomData() {
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName("test")
                .chatUsers(new ArrayList<>())
                .recentChatTime(LocalDateTime.now())
                .build();
        chatRoomRepository.save(chatRoom);
    }
}