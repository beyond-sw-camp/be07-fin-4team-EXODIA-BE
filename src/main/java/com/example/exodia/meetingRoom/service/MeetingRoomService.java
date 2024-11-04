package com.example.exodia.meetingRoom.service;

import com.example.exodia.meetingRoom.domain.MeetingRoom;
import com.example.exodia.meetingRoom.dto.MeetingRoomCreateDto;
import com.example.exodia.meetingRoom.dto.MeetingRoomUpdateDto;
import com.example.exodia.meetingRoom.repository.MeetingRoomRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeetingRoomService {

    @Autowired
    private MeetingRoomRepository meetingRoomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    /* 회의실 추가 */
    @Transactional
    public MeetingRoom createMeetRoom(MeetingRoomCreateDto createDto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        userService.checkHrAuthority(user.getDepartment().getId().toString());

        if (meetingRoomRepository.findByName(createDto.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 회의실 이름입니다.");
        }
        MeetingRoom meetingRoom = createDto.toEntity();
        return meetingRoomRepository.save(meetingRoom);
    }

    /* 회의실 목록 조회 */
    public List<MeetingRoom> listMeetRoom() {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return meetingRoomRepository.findAll();
    }

    /* 회의실 이름 변경 */
    @Transactional
    public MeetingRoom updateMeetRoom(Long id, MeetingRoomUpdateDto updateDto) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("해당 사원을 찾을 수 없습니다."));
        userService.checkHrAuthority(user.getDepartment().getId().toString());

        MeetingRoom meetingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));

        meetingRoom.setName(updateDto.getNewName());
        return meetingRoomRepository.save(meetingRoom);
    }

    /* 회의실 삭제 */
    @Transactional
    public void deleteMeetRoom(Long id) {
        String userNum = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        userService.checkHrAuthority(user.getDepartment().getId().toString());

        if (!meetingRoomRepository.existsById(id)) {
            throw new IllegalArgumentException("회의실을 찾을 수 없습니다.");
        }
        meetingRoomRepository.deleteById(id);
    }
}


