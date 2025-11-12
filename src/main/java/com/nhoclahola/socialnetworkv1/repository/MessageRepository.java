package com.nhoclahola.socialnetworkv1.repository;

import com.nhoclahola.socialnetworkv1.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String>
{
    @Query("SELECT m FROM Message m WHERE m.chat.chatId = :chatId ORDER BY m.timestamp DESC")
    public List<Message> findMessagesByChatId(@Param("chatId") String chatId, Pageable pageable);
}
