package com.chessgrinder.chessgrinder.controller;

import com.chessgrinder.chessgrinder.dto.ClubDto;
import com.chessgrinder.chessgrinder.dto.ListDto;
import com.chessgrinder.chessgrinder.entities.ClubEntity;
import com.chessgrinder.chessgrinder.entities.RoleEntity;
import com.chessgrinder.chessgrinder.mappers.ClubMapper;
import com.chessgrinder.chessgrinder.repositories.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;

    @GetMapping
    public ListDto<ClubDto> getAllClubs() {
        List<ClubDto> allClubs = clubRepository.findAll()
                .stream()
                .map(clubMapper::toDto)
                .sorted(Comparator.comparing(ClubDto::getRegistrationDate))
                .collect(Collectors.toList());

        return ListDto.<ClubDto>builder().values(allClubs).build();
    }

    @GetMapping("/{clubId}")
    public ClubDto getClubById(
            @PathVariable UUID clubId
    ) {
        final var club = clubRepository.getById(clubId);
        return clubMapper.toDto(club);
    }

    @Secured(RoleEntity.Roles.ADMIN)
    @PostMapping("/createClub")
    public void createClub(
            @RequestBody ClubDto clubDto
    ) {
        ClubEntity club = ClubEntity.builder()
                .name(clubDto.getName())
                .description(clubDto.getDescription())
                .location(clubDto.getLocation())
                .build();

        clubRepository.save(club);
    }

    @Secured(RoleEntity.Roles.ADMIN)
    @PutMapping("/{clubId}")
    public void updateClub(
            @PathVariable UUID clubId,
            @RequestBody ClubDto clubDto
    ) {
        ClubEntity club = clubRepository.getById(clubId);

        if (clubDto.getName() != null) {
            club.setName(clubDto.getName());
        }
        if (clubDto.getDescription() != null) {
            club.setDescription(clubDto.getDescription());
        }
        if (clubDto.getLocation() != null) {
            club.setLocation(clubDto.getLocation());
        }

        clubRepository.save(club);
    }

    @Secured(RoleEntity.Roles.ADMIN)
    @DeleteMapping("/{clubId}")
    public void deleteClub(
            @PathVariable UUID clubId
    ) {
        if (!clubRepository.existsById(clubId)) {
            throw new ResponseStatusException(HttpStatus.OK, "No club with id " + clubId);
        }

        clubRepository.deleteById(clubId);
    }
}
