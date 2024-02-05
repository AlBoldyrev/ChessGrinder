package com.chessgrinder.chessgrinder.controller;

import com.chessgrinder.chessgrinder.dto.*;
import com.chessgrinder.chessgrinder.entities.*;
import com.chessgrinder.chessgrinder.exceptions.UserNotFoundException;
import com.chessgrinder.chessgrinder.mappers.ParticipantMapper;
import com.chessgrinder.chessgrinder.mappers.TournamentMapper;
import com.chessgrinder.chessgrinder.mappers.UserMapper;
import com.chessgrinder.chessgrinder.repositories.*;
import com.chessgrinder.chessgrinder.security.AuthenticatedUserArgumentResolver.AuthenticatedUser;
import com.chessgrinder.chessgrinder.service.UserService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final TournamentMapper tournamentMapper;
    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;
    private final UserReputationHistoryRepository userReputationHistoryRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${chessgrinder.feature.auth.signupWithPasswordEnabled:true}")
    private boolean isSignupWithPasswordEnabled;

    private static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z0-9]+$";

    @GetMapping
    public ListDto<UserDto> getUsers() {
        return ListDto.<UserDto>builder().values(userService.getAllUsers()).build();
    }

    @GetMapping("/{userId}")
    public UserDto addParticipantToTournament(@PathVariable String userId) throws UserNotFoundException {
        try {
            UserDto user = userService.getUserByUserId(userId);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {

        }
        return userService.getUserByUserName(userId);
    }

    @Transactional
    @GetMapping("/me")
    public UserDto me(
            @AuthenticatedUser UserEntity authenticatedUser
    ) {
        return userMapper.toDto(authenticatedUser);
    }

    @GetMapping("/{userIdOrUsername}/history")
    public ListDto<UserHistoryRecordDto> history(
            @PathVariable String userIdOrUsername
    ) {
        UserEntity user = findUserByIdOrUsername(userIdOrUsername);
        List<ParticipantEntity> participants = participantRepository.findAllByUserId(user.getId());
        Map<UUID, Integer> placesPerTournament = getPlacesPerTournament(participants);
        List<UserHistoryRecordDto> history = participants.stream()
                .map(participant ->
                        UserHistoryRecordDto.builder()
                                .tournament(tournamentMapper.toDto(participant.getTournament()))
                                .participant(participantMapper.toDto(participant))
                                //TODO сделать как participant.getPlace(как-то так, хотя можно засунуть в toDto выше)
                                .place(placesPerTournament.get(participant.getTournament().getId()))
//                                .place(participant.getPlace())
                                .build()
                )
                .toList();
        return ListDto.<UserHistoryRecordDto>builder().values(history).build();
    }

    //Returns table of participant's places who this user was, not all participants of tournament
    //One tournament per participant
    //First it should be sorted by points, then by Buchholz, and only then by nickname (all in descending order)
    //TODO если в турнире у всех по 0 очков, то у всех будет последнее место

    // + по идее, место должно ставиться там же, где устанавливается и score
    //Это в public List<ParticipantDto> getResult() <- public List<MatchDto> matchUp
    // <- public void makeMatchUp <- @PostMapping("/{roundNumber}/action/matchup")
    //Этот метод вызывается при нажатии кнопки "Draw" (это странно, т.к. в таком случае, очков не присваивается)
    //Очки присваиваются только после кнопки завершить (запрос @PostMapping("/{roundNumber}/action/finish"))

    //Как присвоить значения существующим участникам в миграции в БД?
    //1. В цикле пройтись по каждому участнику в participants_table
    //2. В цикле надо получить id каждого турнира, в котором участвовал участник
    //3. По id турнира получить список всех участников турнира
    //3.1 Отсортировать этот список (думаю, это можно сделать по ORDER BY)
    //4. Берем индекс из получившегося списка по id участника
    private Map<UUID, Integer> getPlacesPerTournament(List<ParticipantEntity> allParticipants) {
        Map<UUID, Integer> placesPerTournament = new HashMap<>();
        for (var participant : allParticipants) {
            final var tournament = participant.getTournament();
            if (tournament == null) continue;
            final var tId = tournament.getId();
            var tournamentParticipants = participantRepository.findByTournamentId(tId);
            tournamentParticipants.sort(Comparator
                    .comparing(ParticipantEntity::getScore, Comparator.reverseOrder())
                    .thenComparing(ParticipantEntity::getBuchholz, Comparator.reverseOrder())
                    //Я уверен, что ники тоже должны сортироваться в обратном порядке
                    .thenComparing(ParticipantEntity::getNickname, Comparator.reverseOrder()));
            final int place = IntStream.range(0, tournamentParticipants.size())
                    .filter(i -> tournamentParticipants.get(i).equals(participant))
                    .findFirst()
                    .orElse(-1) + 1;
            placesPerTournament.put(tId, place);
        }

        return placesPerTournament;
    }

    @Nonnull
    private UserEntity findUserByIdOrUsername(String userIdOrUsername) {
        UserEntity user = userRepository.findByUsername(userIdOrUsername);
        if (user == null) {
            UUID userId;
            try {
                userId = UUID.fromString(userIdOrUsername);
            } catch (IllegalArgumentException e) {
                throw new UserNotFoundException("No user with id " + userIdOrUsername, e);
            }
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("No user with id " + userIdOrUsername));
        }
        return user;
    }

    @Secured(RoleEntity.Roles.ADMIN)
    @PostMapping("/{userId}/badge/{badgeId}")
    public void assignBadge(
            @PathVariable UUID userId,
            @PathVariable UUID badgeId
    ) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        BadgeEntity badge = badgeRepository.findById(badgeId).orElseThrow();
        UserBadgeEntity assignment = UserBadgeEntity.builder().badge(badge).user(user).build();
        userBadgeRepository.save(assignment);
    }

    @Secured(RoleEntity.Roles.ADMIN)
    @PostMapping("/{userId}/reputation")
    @Transactional
    public void assignReputation(
            @PathVariable UUID userId,
            @RequestBody UserReputationHistoryRecordDto data
    ) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        userReputationHistoryRepository.save(UserReputationHistoryEntity.builder()
                .amount(data.getAmount())
                .comment(data.getComment())
                .user(user)
                .build());
        userRepository.addReputation(userId, data.getAmount());
    }

    @PatchMapping("/{userName}")
    public void updateUser(
            @PathVariable String userName,
            @RequestBody UserDto userDto,
            @AuthenticatedUser UserEntity authenticatedUser
    ) {
        if (!userName.equals(authenticatedUser.getUsername())) {
            throw new ResponseStatusException(403, "Not allowed to change other's name", null);
        }

        authenticatedUser.setName(userDto.getName());
        userRepository.save(authenticatedUser);
    }

    @PostMapping("/signUp")
    public void signUp(
            @RequestBody UserSignUpRequest signUpRequest,
            @AuthenticatedUser(required = false) UserEntity authenticatedUser
    ) {
        if (!isSignupWithPasswordEnabled) {
            throw new ResponseStatusException(400, "Sign Up with password is disabled.", null);
        }

        if (signUpRequest == null || signUpRequest.getUsername() == null || signUpRequest.getUsername().isBlank()) {
            throw new ResponseStatusException(400, "Invalid username", null);
        }

        if (signUpRequest.getUsername().contains("@")) {
            throw new ResponseStatusException(400, "Email could not be a username", null);
        }

        boolean userNameAlreadyExists = userRepository.findByUsername(signUpRequest.getUsername()) != null;
        if (authenticatedUser != null || userNameAlreadyExists) {
            throw new ResponseStatusException(400, "Already registered", null);
        }

        String password = signUpRequest.getPassword();
        if (password == null || password.isBlank() || password.length() < 4) {
            throw new ResponseStatusException(400, "Invalid password. Min 4 chars.", null);
        }

        if (!signUpRequest.getUsername().matches(USERNAME_REGEX)) {
            throw new ResponseStatusException(400, "Invalid username", null);
        }

        userRepository.save(UserEntity.builder()
                .username(signUpRequest.getUsername())
                .name(signUpRequest.getFullName())
                .password(passwordEncoder.encode(password))
                .build()
        );
    }
}
