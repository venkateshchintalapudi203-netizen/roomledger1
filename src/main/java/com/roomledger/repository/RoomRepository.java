package com.roomledger.repository;

import com.roomledger.entity.Room;
import com.roomledger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.owner = :user OR :user MEMBER OF r.members")
    List<Room> findAllByMemberOrOwner(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Room r WHERE r.id = :roomId AND (:user MEMBER OF r.members OR r.owner = :user)")
    boolean isMember(@Param("roomId") Long roomId, @Param("user") User user);
}
