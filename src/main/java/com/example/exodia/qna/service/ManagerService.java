package com.example.exodia.qna.service;

import com.example.exodia.qna.domain.Manager;
import com.example.exodia.qna.dto.ManagerListDto;
import com.example.exodia.qna.dto.ManagerSaveDto;
import com.example.exodia.qna.repository.ManagerRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;


    @Transactional
    public ManagerListDto saveManager(ManagerSaveDto managerSaveDto) {
        User user = userRepository.findByUserNum(managerSaveDto.getUserNum())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Manager manager = new Manager();
        manager.setUser(user);

        // 엔티티를 저장하고, DTO로 변환하여 반환
        managerRepository.save(manager);

        return new ManagerListDto(manager.getId(), user.getUserNum(), user.getDepartment().getId(), user.getPosition().getId(), user.getName());
    }



    @Transactional
    public void deleteManager(String userNum) {
        Manager manager = managerRepository.findByUser_UserNum(userNum)
                .orElseThrow(() -> new IllegalArgumentException("매니저를 찾을 수 없습니다."));
        managerRepository.delete(manager);
    }



    public List<ManagerListDto> getAllManagers() {
        List<Manager> managers = managerRepository.findAll();

        // 매니저 리스트를 ManagerListDto로 변환하여 반환
        return managers.stream()
                .map(ManagerListDto::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean isManager(String userNum) {
        return managerRepository.existsByUser_UserNum(userNum);
    }
}
