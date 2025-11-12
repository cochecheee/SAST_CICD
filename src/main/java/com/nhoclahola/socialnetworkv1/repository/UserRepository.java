package com.nhoclahola.socialnetworkv1.repository;

import com.nhoclahola.socialnetworkv1.dto.admin.response.DashBoardInfo;
import com.nhoclahola.socialnetworkv1.dto.user.UserWithData;
import com.nhoclahola.socialnetworkv1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>
{
    public abstract Optional<User> findByEmail(String email);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.user.UserWithData (u.userId, u.firstName, u.lastName, u.username, u.email, u.description, u.gender, u.avatarUrl, u.coverPhotoUrl, " +
            "(SELECT COUNT(p) FROM u.posts p), " +
            "(SELECT COUNT(fr) FROM u.followers fr), " +
            "(SELECT COUNT(fg) FROM u.followings fg), " +
            "EXISTS (SELECT fr FROM u.followers fr WHERE fr.email = :userRequestEmail)) " +
            "FROM User u WHERE u.userId = :userId")
    public abstract Optional<UserWithData> findUserWithDataByUserId(@Param("userRequestEmail") String userRequestEmail, @Param("userId") String userId);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.user.UserWithData (u.userId, u.firstName, u.lastName, u.username, u.email, u.description, u.gender, u.avatarUrl, u.coverPhotoUrl, " +
            "(SELECT COUNT(p) FROM u.posts p), " +
            "(SELECT COUNT(fr) FROM u.followers fr), " +
            "(SELECT COUNT(fg) FROM u.followings fg), " +
            "EXISTS (SELECT fr FROM u.followers fr WHERE fr.email = :userRequestEmail)) " +
            "FROM User u WHERE u.email = :userRequestEmail")
    public abstract Optional<UserWithData> findUserWithDataByEmail(@Param("userRequestEmail") String userRequestEmail);

    public abstract boolean existsByEmail(String email);

    public abstract boolean existsByUsername(String username);
    @Query("SELECT u " +
            "FROM User u " +
            "WHERE u.firstName LIKE %:query% " +
            "OR u.lastName LIKE %:query% " +
            "OR u.username LIKE %:query% " +
            "OR CONCAT(u.firstName, ' ', u.lastName) LIKE %:query%")
    public abstract List<User> searchUser(@Param("query") String query, Pageable pageable);

    @Query("SELECT EXISTS (SELECT 1 FROM u.followers fr WHERE fr.userId = :userRequestId) " +
            "FROM User u WHERE u.userId = :userId")
    public abstract boolean isFollow(@Param("userRequestId") String userRequestId, @Param("userId") String userId);

    @Modifying
    @Query(value = "INSERT INTO user_follow(follower_id, following_id) " +
            "VALUES (:userRequestId, :userId)", nativeQuery = true)
    public abstract void follow(@Param("userRequestId") String userRequestId, @Param("userId") String userId);

    @Modifying
    @Query(value = "DELETE FROM user_follow " +
            "WHERE follower_id = :userRequestId AND following_id = :userId", nativeQuery = true)
    public abstract void unfollow(@Param("userRequestId") String userRequestId, @Param("userId") String userId);
    @Query("SELECT u.followings FROM User u WHERE u.userId = :userId")
    public abstract List<User> findUsersFollowings(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT u.followers FROM User u WHERE u.userId = :userId")
    public abstract List<User> findUsersFollowers(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT u FROM User u " +
            "JOIN u.posts p " +
            "WHERE u IN (SELECT f FROM User u JOIN u.followings f WHERE u.email = :userRequestEmail) " +
            "GROUP BY u.userId " +
            "ORDER BY MAX(p.createdAt) DESC")
    public abstract List<User> findLatestActivityUsersFollowings(@Param("userRequestEmail") String userRequestEmail, Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN u.followers f WHERE u.email != :userEmailRequest GROUP BY u ORDER BY COUNT(f) DESC")
    public abstract List<User> findSuggestedUsers(@Param("userEmailRequest") String userEmailRequest, Pageable pageable);

    @Query("SELECT p.liked " +
            "FROM Post p " +
            "JOIN p.liked " +
            "WHERE p.postId = :postId")
    public abstract List<User> findUsersLikedPost(@Param("postId") String postId, Pageable pageable);

    @Query("SELECT c.liked " +
            "FROM Comment c " +
            "JOIN c.liked " +
            "WHERE c.commentId = :commentId")
    public abstract List<User> findUsersLikedComment(@Param("commentId") String commentId, Pageable pageable);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.admin.response.DashBoardInfo(" +
            "(SELECT COUNT(u) FROM com.nhoclahola.socialnetworkv1.entity.User u), " +
            "(SELECT COUNT(p) FROM com.nhoclahola.socialnetworkv1.entity.Post p), " +
            "(SELECT COUNT(c) FROM com.nhoclahola.socialnetworkv1.entity.Chat c), " +
            "(SELECT COUNT(cm) FROM com.nhoclahola.socialnetworkv1.entity.Comment cm), " +
            "(SELECT COUNT(p1) FROM com.nhoclahola.socialnetworkv1.entity.Post p1 WHERE p1.imageUrl IS NOT NULL), " +
            "(SELECT COUNT(p2) FROM com.nhoclahola.socialnetworkv1.entity.Post p2 WHERE p2.videoUrl IS NOT NULL)) " +
            "FROM com.nhoclahola.socialnetworkv1.entity.User u")
    public abstract DashBoardInfo adminDashBoardInfo();

    @Query("SELECT u FROM User u")
    public abstract Page<User> findAllUsersAdmin(Pageable pageable);
}
