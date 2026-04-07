package com.roomledger.controller;

import com.roomledger.dto.*;
import com.roomledger.entity.User;
import com.roomledger.repository.UserRepository;
import com.roomledger.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Create and manage shared rooms")
@SecurityRequirement(name = "Bearer Authentication")
public class RoomController {

    private final RoomService roomService;
    private final UserRepository userRepository;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping
    @Operation(summary = "Get all rooms", description = "Pass optional ?month=YYYY-MM to filter totals by month")
    public ResponseEntity<List<RoomDto>> getAllRooms(
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(roomService.getAllRooms(getUser(ud), month));
    }

    @PostMapping
    @Operation(summary = "Create a room")
    public ResponseEntity<RoomDto> createRoom(
            @Valid @RequestBody CreateRoomRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(roomService.createRoom(req, getUser(ud)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room details", description = "Pass optional ?month=YYYY-MM to filter totals by month")
    public ResponseEntity<RoomDto> getRoom(
            @Parameter(description = "Room ID") @PathVariable Long id,
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(roomService.getRoom(id, getUser(ud), month));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update room name, description, or budget")
    public ResponseEntity<RoomDto> updateRoom(
            @Parameter(description = "Room ID") @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(roomService.updateRoom(id, req, getUser(ud)));
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Invite a member by email")
    public ResponseEntity<UserDto> inviteMember(
            @Parameter(description = "Room ID") @PathVariable Long id,
            @Valid @RequestBody InviteRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(roomService.inviteMember(id, req, getUser(ud)));
    }
}