package com.example.exodia.car.dto;

import com.example.exodia.car.domain.Car;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarResponseDto {
    private Long id;
    private String carNum;
    private String carType;
    private int seatingCapacity;
    private double engineDisplacement;
    private String carImage;

    public static CarResponseDto fromEntity(Car car) {
        return CarResponseDto.builder()
                .id(car.getId())
                .carNum(car.getCarNum())
                .carType(car.getCarType())
                .seatingCapacity(car.getSeatingCapacity())
                .engineDisplacement(car.getEngineDisplacement())
                .carImage(car.getCarImage())
                .build();
    }
}