package com.nhoclahola.socialnetworkv1.repository;

import com.nhoclahola.socialnetworkv1.dto.post.PostWithData;
import com.nhoclahola.socialnetworkv1.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String>
{
    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :requestUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :requestUserEmail)) " +
            "FROM Post p WHERE p.user.id = :userId")
    public abstract List<PostWithData> findPostByUserId(@Param("requestUserEmail") String requestUserEmail,
                                                        @Param("userId") String userId, Pageable pageable);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :requestUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :requestUserEmail)) " +
            "FROM Post p WHERE p.user.email = :requestUserEmail")
    public abstract List<PostWithData> findPostByEmail(@Param("requestUserEmail") String requestUserEmail, Pageable pageable);

    // Used for API
    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :requestUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :requestUserEmail)) " +
            "FROM Post p WHERE p.postId = :postId")
    public abstract Optional<PostWithData> findPostWithDataById(@Param("requestUserEmail") String requestUserEmail,
                                                               @Param("postId") String postId);

    // Get a list of posts from the users who the current user is following
    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p WHERE p.user IN (SELECT f FROM User u JOIN u.followings f WHERE u.email = :currentUserEmail)")
    public abstract List<PostWithData> findPostsFromFollowedUsers(@Param("currentUserEmail") String currentUserEmail, Pageable pageable);

    // Get a list of random posts from the other users whom the currentUser is not following
    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p WHERE p.user NOT IN (SELECT f FROM User u JOIN u.followings f WHERE u.email = :currentUserEmail) ORDER BY function('RAND')")
    public abstract List<PostWithData> findRandomPostsFromOtherUsers(@Param("currentUserEmail") String currentUserEmail, Pageable pageable);

    // Check if a user liked a post or not (Use native query to prevent JOIN)
    @Query(value = "SELECT EXISTS (SELECT 1 " +
            "FROM post_liked p " +
            "WHERE p.post_id = :postId AND p.user_id = :userId)", nativeQuery = true)
    public abstract int isLikedByUserId(@Param("postId") String postId, @Param("userId") String userId);

    @Modifying
    @Query(value = "INSERT INTO post_liked (post_id, user_id) " +
            "VALUES (:postId, :userId)", nativeQuery = true)
    public abstract void addLikeToPost(@Param("postId") String postId, @Param("userId") String userId);

    @Modifying
    @Query(value = "DELETE FROM post_liked " +
            "WHERE post_id = :postId " +
            "AND user_id = :userId", nativeQuery = true)
    public abstract void removeLikeFromPost(@Param("postId") String postId, @Param("userId") String userId);

    // Check if a user saved a post or not (Use native query to prevent JOIN)
    @Query(value = "SELECT EXISTS (SELECT 1 " +
            "FROM post_saved p " +
            "WHERE p.post_id = :postId AND p.user_id = :userId)", nativeQuery = true)
    public abstract int isSavedByUserId(@Param("postId") String postId, @Param("userId") String userId);

    @Modifying
    @Query(value = "INSERT INTO post_saved (post_id, user_id) " +
            "VALUES (:postId, :userId)", nativeQuery = true)
    public abstract void savePost(@Param("postId") String postId, @Param("userId") String userId);

    @Modifying
    @Query(value = "DELETE FROM post_saved " +
            "WHERE post_id = :postId " +
            "AND user_id = :userId", nativeQuery = true)
    public abstract void unsavePost(@Param("postId") String postId, @Param("userId") String userId);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l) AS likedCount, " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p WHERE p.videoUrl IS NOT NULL " +
            "ORDER BY likedCount DESC, p.createdAt DESC")
    public abstract List<PostWithData> findPopularVideoPosts(@Param("currentUserEmail") String currentUserEmail, Pageable pageable);


    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p " +
            "WHERE p.caption LIKE %:query%")
    public abstract List<PostWithData> searchPost(@Param("currentUserEmail") String currentUserEmail,
                                                  @Param("query") String query,
                                                  Pageable pageable);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p " +
            "WHERE p.videoUrl IS NOT NULL AND p.user.userId = :userId " +
            "ORDER BY p.createdAt DESC")
    public abstract List<PostWithData> findVideoPostsByUserId(@Param("userId") String userId,
                                                              @Param("currentUserEmail") String currentUserEmail,
                                                              Pageable pageable);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p " +
            "WHERE p.videoUrl IS NOT NULL AND p.user.email = :currentUserEmail " +
            "ORDER BY p.createdAt DESC")
    public abstract List<PostWithData> findVideoPostsByEmail(@Param("currentUserEmail") String currentUserEmail,
                                                              Pageable pageable);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p " +
            "JOIN p.saved s " +
            "WHERE s.userId = :userId " +
            "ORDER BY p.createdAt DESC")
    public abstract List<PostWithData> findSavedPostsByUserId(@Param("userId") String userId,
                                                              @Param("currentUserEmail") String currentUserEmail,
                                                              Pageable pageable);

    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.post.PostWithData(p.postId, p.caption, p.imageUrl, p.videoUrl, p.createdAt, p.user, " +
            "(SELECT COUNT(l) FROM p.liked l), " +
            "(SELECT COUNT(c) FROM p.comments c), " +
            "EXISTS (SELECT l FROM p.liked l WHERE l.email = :currentUserEmail), " +
            "EXISTS (SELECT l FROM p.saved l WHERE l.email = :currentUserEmail)) " +
            "FROM Post p " +
            "JOIN p.saved s " +
            "WHERE s.email = :currentUserEmail " +
            "ORDER BY p.createdAt DESC")
    public abstract List<PostWithData> findSavedPostsByEmail(@Param("currentUserEmail") String currentUserEmail,
                                                              Pageable pageable);

    @Query("SELECT p FROM Post p")
    public abstract Page<Post> findAllPostsAdmin(Pageable pageable);
}
