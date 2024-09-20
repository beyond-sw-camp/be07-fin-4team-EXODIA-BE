package com.example.exodia.video.service;

import com.example.exodia.video.dto.JoinRoomDto;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.example.exodia.video.domain.VideoParticipant;
import com.example.exodia.video.domain.VideoRoom;
import com.example.exodia.video.dto.CreateRoomDto;
import com.example.exodia.video.repository.VideoParticipantRepository;
import com.example.exodia.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class VideoService {

    private final UserRepository userRepository;
    private final VideoParticipantRepository videoParticipantRepository;
    private final VideoRepository videoRepository;

    public VideoService(UserRepository userRepository, VideoParticipantRepository videoParticipantRepository, VideoRepository videoRepository) {
        this.userRepository = userRepository;
        this.videoParticipantRepository = videoParticipantRepository;
        this.videoRepository = videoRepository;
    }

    public VideoRoom createRoom(CreateRoomDto createRoomDto, String userNum) {
        User host = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        VideoRoom videoRoom = new VideoRoom(null, createRoomDto.getRoomName(), createRoomDto.getPassword(), host, true);
        return videoRepository.save(videoRoom);
    }

    public void joinRoom(JoinRoomDto joinRoomDto, String userNum) {
        VideoRoom room = videoRepository.findByRoomName(joinRoomDto.getRoomName())
                .orElseThrow(() -> new RuntimeException("방을 찾을 수 없습니다."));

        if (room.getPassword() != null && !room.getPassword().equals(joinRoomDto.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<VideoParticipant> participantOpt = videoParticipantRepository.findByRoomAndUser(room, user);
        if (participantOpt.isPresent()) {
            throw new RuntimeException("이미 방에 참여 중입니다.");
        }

        VideoParticipant participant = new VideoParticipant(null, room, user);
        videoParticipantRepository.save(participant);
    }

    public void leaveRoom(String roomName, String userNum) {
        VideoRoom room = videoRepository.findByRoomName(roomName)
                .orElseThrow(() -> new RuntimeException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUserNum(userNum)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        VideoParticipant participant = videoParticipantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new RuntimeException("참여 기록이 없습니다."));

        videoParticipantRepository.delete(participant);
    }

    public void deleteRoomIfEmpty(VideoRoom room) {
        long participantCount = videoParticipantRepository.countByRoom(room);
        if (participantCount == 0) {
            videoRepository.delete(room);
        }
    }
}
