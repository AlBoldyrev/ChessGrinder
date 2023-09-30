package com.chessgrinder.chessgrinder.dto;

import java.math.*;

import lombok.*;

@Data
@Builder
public class ParticipantDto {

    private String userId;
    private String name; //NICKNAME
    private BigDecimal score;
    private BigDecimal buchholz;
}