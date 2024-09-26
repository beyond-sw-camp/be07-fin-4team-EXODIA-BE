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

    public VideoRoom createRoom(String roomName, String password, User host) {
        VideoRoom room = new VideoRoom(
                null,
                roomName,
                password,
                host,
                true,
                1,
                LocalDateTime.now()
        );
        VideoRoom savedRoom = videoRoomRepository.save(room);
        VideoRoomRedisDto roomDto = savedRoom.toDto();

        redisTemplate.opsForValue().set("room:" + savedRoom.getId(), roomDto);
        messagePublisher.publish("Room created by " + host.getName() + ": " + roomName);
        return savedRoom;
    }


    public boolean joinRoom(String roomName, String password, User user) {
        try {
            // 방 정보가 올바르게 조회되었는지 확인
            Optional<VideoRoom> roomOpt = videoRoomRepository.findByRoomNameAndIsActiveTrue(roomName);
            if (roomOpt.isPresent()) {
                VideoRoom room = roomOpt.get();
                System.out.println("방 정보: " + room); // 방 정보 로그 출력
                System.out.println("방 비밀번호: " + room.getPassword()); // 방 비밀번호 출력

                if (room.getPassword() == null || room.getPassword().equals(password)) {
                    room.increaseParticipantCount();
                    videoRoomRepository.save(room);

                    // 변경된 정보 Redis에 저장
                    redisTemplate.opsForValue().set("room:" + room.getId(), room.toDto());
                    messagePublisher.publish(user.getName() + " joined room: " + roomName);
                    return true;
                } else {
                    System.out.println("비밀번호 불일치");
                    throw new IllegalArgumentException("비밀번호가 틀렸습니다."); // 비밀번호가 틀릴 때의 예외 처리
                }
            } else {
                System.out.println("방 존재하지 않음");
                throw new IllegalArgumentException("방이 존재하지 않습니다."); // 방이 존재하지 않을 때의 예외 처리
            }
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            e.printStackTrace(); // 콘솔에 예외 출력
            throw e; // 예외 다시 던지기
        }
    }



    public void leaveRoom(String roomName, User user) {
        Optional<VideoRoom> roomOpt = videoRoomRepository.findByRoomName(roomName);
        if (roomOpt.isPresent()) {
            VideoRoom room = roomOpt.get();
            room.decreaseParticipantCount();
            if (room.getParticipantCount() == 0) {
                room.setIsActive(false);
                videoRoomRepository.delete(room);
                redisTemplate.delete("room:" + room.getId());
            } else {
                videoRoomRepository.save(room);
                redisTemplate.opsForValue().set("room:" + room.getId(), room.toDto());
            }
            messagePublisher.publish(user.getName() + " left room: " + roomName);
        }
    }

    public List<VideoRoom> getActiveRooms() {
        return videoRoomRepository.findAllByIsActiveTrue();
    }
}
