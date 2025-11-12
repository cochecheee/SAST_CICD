package com.nhoclahola.socialnetworkv1.repository;

import com.nhoclahola.socialnetworkv1.entity.Chat;
import com.nhoclahola.socialnetworkv1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String>
{
    @Query("SELECT c FROM Chat c JOIN c.users u " +
            "LEFT JOIN c.messages m " +
            "WHERE u.userId = :userId " +
            "GROUP BY c.chatId " +
            "ORDER BY MAX(m.timestamp) DESC")
    public abstract List<Chat> findByUsersId(@Param("userId") String userId);

    @Query("SELECT c FROM Chat c " +
            "WHERE :reqUser MEMBER OF c.users AND :targetUser MEMBER OF c.users")
    public abstract Chat findChatByUserId(@Param("reqUser") User reqUser,
                                          @Param("targetUser") User targetUser);
}
