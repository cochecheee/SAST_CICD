package com.nhoclahola.socialnetworkv1.repository;

import com.nhoclahola.socialnetworkv1.dto.comment.CommentWithData;
import com.nhoclahola.socialnetworkv1.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String>
{
    // JOIN FETCH to make it fetch eagerly, even I set lazy fetch in entity
    @Query("SELECT new com.nhoclahola.socialnetworkv1.dto.comment.CommentWithData(c.commentId, c.content, c.createdAt, c.user, " +
            "(SELECT COUNT(l) FROM c.liked l), " +
            "EXISTS (SELECT l FROM c.liked l WHERE l.email = :requestUserEmail)) " +
            "FROM Comment c WHERE c.post.postId = :postId")
    public abstract List<CommentWithData> findCommentsByPostId(@Param("requestUserEmail") String requestUserEmail,
                                                               @Param("postId") String postId, Pageable pageable);

    @Query(value = "SELECT EXISTS (SELECT 1 " +
            "FROM comment_liked p " +
            "WHERE p.comment_id = :commentId AND p.user_id = :userId)", nativeQuery = true)
    public abstract int isLikedByUserId(@Param("commentId") String commentId, @Param("userId") String userId);

    @Modifying
    @Query(value = "DELETE FROM comment_liked " +
            "WHERE comment_id = :commentId " +
            "AND user_id = :userId", nativeQuery = true)
    public abstract void removeLikeFromComment(@Param("commentId") String commentId, @Param("userId") String userId);

    @Modifying
    @Query(value = "INSERT INTO comment_liked (comment_id, user_id) " +
            "VALUES (:commentId, :userId)", nativeQuery = true)
    public abstract void addLikeToComment(@Param("commentId") String commentId, @Param("userId") String userId);
}
