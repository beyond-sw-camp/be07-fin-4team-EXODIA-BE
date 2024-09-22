package com.example.exodia.video.service;

import com.example.exodia.user.domain.User;
import com.example.exodia.video.domain.VideoParticipant;
import com.example.exodia.video.domain.VideoRoom;
import com.example.exodia.video.repository.VideoParticipantRepository;
import com.example.exodia.video.repository.VideoRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoRoomService {

    private final VideoRoomRepository videoRoomRepository;
    private final VideoParticipantRepository videoParticipantRepository;

    public VideoRoomService(VideoRoomRepository videoRoomRepository, VideoParticipantRepository videoParticipantRepository) {
        this.videoRoomRepository = videoRoomRepository;
        this.videoParticipantRepository = videoParticipantRepository;
    }

    public VideoRoom createRoom(String roomName, String password, User host) {
        VideoRoom room = new VideoRoom(null, roomName, password, host, true, 0);
        return videoRoomRepository.save(room);
    }

    public boolean joinRoom(String roomName, String password, User user) {
        Optional<VideoRoom> roomOpt = videoRoomRepository.findByRoomNameAndIsActiveTrue(roomName);
        if (roomOpt.isPresent()) {
            VideoRoom room = roomOpt.get();
            if (room.getPassword() == null || room.getPassword().equals(password)) {
                VideoParticipant participant = new VideoParticipant(null, room, user);
                videoParticipantRepository.save(participant);
                room.increaseParticipantCount();
                videoRoomRepository.save(room);
                return true;
            }
        }
        return false;
    }

    public void leaveRoom(String roomName, User user) {
        VideoParticipant participant = videoParticipantRepository.findByRoomRoomNameAndUserUserNum(roomName, user.getUserNum())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 방에 참여하지 않았습니다."));
        videoParticipantRepository.delete(participant);

        VideoRoom room = participant.getRoom();
        room.decreaseParticipantCount();
        videoRoomRepository.save(room);

        if (room.getParticipantCount() == 0) {
            room.setIsActive(false);
            videoRoomRepository.deleteByRoomName(roomName);
        }
    }

    public List<VideoRoom> getActiveRooms() {
        return videoRoomRepository.findAll();
    }
}
