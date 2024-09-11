package com.chessgrinder.chessgrinder.service;

import com.chessgrinder.chessgrinder.dto.EloUpdateResultDto;
import com.chessgrinder.chessgrinder.enums.MatchResult;
import org.springframework.stereotype.Component;

@Component
public class AdvancedEloCalculationStrategy implements EloCalculationStrategy {

    private static final double K_FACTOR = 32;
    private static final int UNRATED_WIN_POINTS = 5;
    private static final int UNRATED_LOSE_POINTS = -5;
    private static final int UNRATED_DRAW_POINTS = 1;

    public static double calculateExpectedScore(int whiteElo, int blackElo) {
        return 1.0 / (1 + Math.pow(10, (blackElo - whiteElo) / 400.0));
    }

    public static int calculateNewElo(int whiteElo, int blackElo, double score) {
        double expectedScore = calculateExpectedScore(whiteElo, blackElo);
        return (int) Math.round(whiteElo + K_FACTOR * (score - expectedScore));
    }


    @Override
    public EloUpdateResultDto calculateElo(int whiteElo, int blackElo, MatchResult result, boolean bothUsersAuthorized) {

        if (result == null) {
            return EloUpdateResultDto.builder()
                    .whiteNewElo(whiteElo)
                    .blackNewElo(blackElo)
                    .build();
        }

        int whitePoints = 0;
        int blackPoints = 0;

        if (result == MatchResult.WHITE_WIN) {
            whitePoints = bothUsersAuthorized ? (calculateNewElo(whiteElo, blackElo, 1.0) - whiteElo) : UNRATED_WIN_POINTS;
            blackPoints = bothUsersAuthorized ? (calculateNewElo(blackElo, whiteElo, 0) - blackElo) : UNRATED_LOSE_POINTS;
        } else if (result == MatchResult.BLACK_WIN) {
            whitePoints = bothUsersAuthorized ? (calculateNewElo(whiteElo, blackElo, 0) - whiteElo) : UNRATED_LOSE_POINTS;
            blackPoints = bothUsersAuthorized ? (calculateNewElo(blackElo, whiteElo, 1.0) - blackElo) : UNRATED_WIN_POINTS;
        } else if (result == MatchResult.DRAW) {
            whitePoints = bothUsersAuthorized ? (calculateNewElo(whiteElo, blackElo, 0.5) - whiteElo) : UNRATED_DRAW_POINTS;
            blackPoints = bothUsersAuthorized ? (calculateNewElo(blackElo, whiteElo, 0.5) - blackElo) : UNRATED_DRAW_POINTS;
        }

        int whiteNewElo = whiteElo + whitePoints;
        int blackNewElo = blackElo + blackPoints;

        return EloUpdateResultDto.builder()
                .whiteNewElo(whiteNewElo)
                .blackNewElo(blackNewElo)
                .build();
    }

}
