package com.roomledger.service;

import com.roomledger.dto.*;
import com.roomledger.entity.Room;
import com.roomledger.entity.User;
import com.roomledger.repository.ExpenseRepository;
import com.roomledger.repository.MonthlyBudgetRepository;
import com.roomledger.repository.RoomRepository;
import com.roomledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final MonthlyBudgetRepository budgetRepository;

    private String currentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public List<RoomDto> getAllRooms(User user, String month) {
        String m = (month != null && !month.isEmpty()) ? month : currentMonth();
        return roomRepository.findAllByMemberOrOwner(user).stream()
                .map(r -> toDto(r, false, m))
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDto createRoom(CreateRoomRequest req, User user) {
        Set<User> members = new HashSet<>();
        members.add(user);
        Room room = Room.builder()
                .name(req.getName())
                .description(req.getDescription())
                .budget(0.0)   // legacy field, monthly budgets take precedence
                .owner(user)
                .members(members)
                .build();
        room = roomRepository.save(room);
        return toDto(room, true, currentMonth());
    }

    public RoomDto getRoom(Long roomId, User user, String month) {
        Room room = getRoomAndCheckAccess(roomId, user);
        String m = (month != null && !month.isEmpty()) ? month : currentMonth();
        return toDto(room, true, m);
    }

    @Transactional
    public RoomDto updateRoom(Long roomId, UpdateRoomRequest req, User user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only room owner can update");
        }
        room.setName(req.getName());
        room.setDescription(req.getDescription());
        return toDto(roomRepository.save(room), true, currentMonth());
    }

    @Transactional
    public UserDto inviteMember(Long roomId, InviteRequest req, User user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only room owner can invite members");
        }
        User invitee = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("No user found with that email"));
        if (room.getMembers() == null) room.setMembers(new HashSet<>());
        if (room.getMembers().contains(invitee)) throw new RuntimeException("User is already a member");
        room.getMembers().add(invitee);
        roomRepository.save(room);
        return new UserDto(invitee.getId(), invitee.getName(), invitee.getEmail());
    }

    public Room getRoomAndCheckAccess(Long roomId, User user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!roomRepository.isMember(roomId, user)) throw new RuntimeException("Access denied");
        return room;
    }

    public RoomDto toDto(Room room, boolean includeMembers, String month) {
        Double totalSpent = expenseRepository.getTotalSpentByRoomAndMonth(room.getId(), month);
        if (totalSpent == null) totalSpent = 0.0;
        long count = expenseRepository.countByRoomId(room.getId());

        // Get monthly budget (base + carryover)
        double totalBudget = budgetRepository.findByRoomIdAndMonth(room.getId(), month)
                .map(mb -> mb.getTotalBudget())
                .orElse(0.0);

        double carriedOver = budgetRepository.findByRoomIdAndMonth(room.getId(), month)
                .map(mb -> mb.getCarriedOver())
                .orElse(0.0);

        double baseBudget = budgetRepository.findByRoomIdAndMonth(room.getId(), month)
                .map(mb -> mb.getBaseBudget())
                .orElse(0.0);

        List<UserDto> members = includeMembers && room.getMembers() != null
                ? room.getMembers().stream()
                    .map(m -> new UserDto(m.getId(), m.getName(), m.getEmail()))
                    .collect(Collectors.toList())
                : List.of();

        return RoomDto.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .budget(totalBudget)
                .baseBudget(baseBudget)
                .carriedOver(carriedOver)
                .totalSpent(totalSpent)
                .remaining(totalBudget - totalSpent)
                .expenseCount(count)
                .owner(new UserDto(room.getOwner().getId(), room.getOwner().getName(), room.getOwner().getEmail()))
                .members(members)
                .createdAt(room.getCreatedAt())
                .build();
    }
}