package com.example.exodia.car.controller;

import com.example.exodia.car.domain.Car;
import com.example.exodia.car.dto.CarResponseDto;
import com.example.exodia.car.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/car")
public class CarController {

    @Autowired
    private CarRepository carRepository;

    @GetMapping("/list")
    public List<CarResponseDto> getCarList() {
        List<Car> cars = carRepository.findAll();
        return cars.stream()
                .map(CarResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
