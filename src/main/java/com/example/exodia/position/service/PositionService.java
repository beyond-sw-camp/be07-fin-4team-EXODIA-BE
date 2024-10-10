package com.example.exodia.position.service;

import com.example.exodia.position.domain.Position;
import com.example.exodia.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }
}
