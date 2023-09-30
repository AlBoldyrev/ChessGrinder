package com.chessgrinder.chessgrinder.controller;

import com.chessgrinder.chessgrinder.dto.ListDto;
import com.chessgrinder.chessgrinder.dto.MemberDto;
import com.chessgrinder.chessgrinder.entities.RoleEntity;
import com.chessgrinder.chessgrinder.entities.UserEntity;
import com.chessgrinder.chessgrinder.exceptions.UserNotFoundException;
import com.chessgrinder.chessgrinder.repositories.UserRepository;
import com.chessgrinder.chessgrinder.security.CustomOAuth2User;
import com.chessgrinder.chessgrinder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public ListDto<MemberDto> getUsers() {
        return ListDto.<MemberDto>builder().values(userService.getAllUsers()).build();
    }

    @GetMapping("/{userId}")
    public MemberDto addParticipantToTournament(@PathVariable String userId) throws UserNotFoundException {
        try {
            MemberDto user = userService.getUserByUserId(userId);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {

        }
        return userService.getUserByUserName(userId);
    }
    //TODO test

    @GetMapping("/me")
    public MemberDto me(
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new ResponseStatusException(401, "Not logged in", null);
        }
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        String email = principal.getEmail();
        if (email == null) {
            return null;
        }
        return userService.getUserByUserName(email);
    }

    @Secured(RoleEntity.Roles.ADMIN)
    @PostMapping("/guest")
    public void createGuest(
            @RequestBody MemberDto user
    ) {
        userRepository.save(UserEntity.builder()
                .name(user.getName())
                .provider(UserEntity.Provider.GUEST)
                .build());
    }

}