package com.example.exodia.video.service;

import com.example.exodia.common.config.RedisMessagePublisher;
import com.example.exodia.user.domain.User;
import com.example.exodia.video.domain.VideoRoom;
import com.example.exodia.video.dto.VideoRoomRedisDto;
import com.example.exodia.video.repository.VideoRoomRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VideoRoomService {

    private final VideoRoomRepository videoRoomRepository;
    private final RedisMessagePublisher messagePublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    public VideoRoomService(VideoRoomRepository videoRoomRepository,
                            RedisMessagePublisher messagePublisher,
                            @Qualifier("videoRoomRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.videoRoomRepository = videoRoomRepository;
        this.messagePublisher = messagePublisher;
        this.redisTemplate = redisTemplate;
    }

    public VideoRoom createRoom(String roomName, String password, Long janusRoomId, User host) {
        VideoRoom room = new VideoRoom(
                janusRoomId,
                roomName,
                password,
                host,
                true,
                1,
                LocalDateTime.now()
        );
        VideoRoom savedRoom = videoRoomRepository.save(room);

        VideoRoomRedisDto roomDto = savedRoom.toDto();
        redisTemplate.opsForValue().set("room:" + savedRoom.getJanusRoomId(), roomDto);  // janusRoomId 사용
        messagePublisher.publish("Room created by " + host.getName() + ": " + roomName);
        return savedRoom;
    }

    public boolean joinRoom(Long janusRoomId, String password, User user) {
        Optional<VideoRoom> roomOpt = videoRoomRepository.findByJanusRoomIdAndIsActiveTrue(janusRoomId);
        if (!roomOpt.isPresent()) {
            throw new IllegalArgumentException("방이 존재하지 않습니다.");
        }
        VideoRoom room = roomOpt.get();
        if (room.getPassword() == null || room.getPassword().equals(password)) {
            room.increaseParticipantCount();
            videoRoomRepository.save(room);
            return true;
        } else {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
    }


//    public boolean joinRoom(Long janusRoomId, String password, User user) {
//        try {
//            Optional<VideoRoom> roomOpt = videoRoomRepository.findByJanusRoomIdAndIsActiveTrue(janusRoomId);
//            if (roomOpt.isPresent()) {
//                VideoRoom room = roomOpt.get();
//                System.out.println("방 정보: " + room);
//                System.out.println("방 비밀번호: " + room.getPassword());
//
//                if (room.getPassword() == null || room.getPassword().equals(password)) {
//                    room.increaseParticipantCount();
//                    videoRoomRepository.save(room);
//
//                    redisTemplate.opsForValue().set("room:" + room.getId(), room.toDto());
//                    messagePublisher.publish(user.getName() + " joined room: " + room.getRoomName());
//                    return true;
//                } else {
//                    System.out.println("비밀번호 불일치");
//                    throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
//                }
//            } else {
//                System.out.println("방 존재하지 않음");
//                throw new IllegalArgumentException("방이 존재하지 않습니다.");
//            }
//        } catch (Exception e) {
//            System.out.println("예외 발생: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//    }

    public void leaveRoom(Long janusRoomId, User user) {
        Optional<VideoRoom> roomOpt = videoRoomRepository.findByJanusRoomId(janusRoomId);
        if (roomOpt.isPresent()) {
            VideoRoom room = roomOpt.get();
            room.decreaseParticipantCount();
            if (room.getParticipantCount() == 0) {
                room.setIsActive(false);
                videoRoomRepository.delete(room);
                redisTemplate.delete("room:" + room.getJanusRoomId());  // janusRoomId로 삭제
            } else {
                videoRoomRepository.save(room);
                redisTemplate.opsForValue().set("room:" + room.getJanusRoomId(), room.toDto());  // janusRoomId로 저장
            }
            messagePublisher.publish(user.getName() + " left room: " + room.getRoomName());
        }
    }

    public List<VideoRoom> getActiveRooms() {
        return videoRoomRepository.findAllByIsActiveTrue();
    }

}
