package com.nhoclahola.socialnetworkv1.entity;


import javax.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User
{
    @Id
    private String userId;
    @Column(nullable = false, length = 20)
    private String firstName;
    @Column(nullable = false, length = 40)
    private String lastName;
    @Column(unique = true, nullable = false, length = 25)
    private String username;
    @Column(unique = true, nullable = false, length = 40)
    private String email;
    @Column(nullable = false)
    private String password;
    private String description;
    private Boolean gender;
    private String avatarUrl;
    private String coverPhotoUrl;
    @Column(nullable = false)
    private Role role;


    // | follower_id | following_id |
    // |   (join)    |  (inverse)   |

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_follow",
            joinColumns = @JoinColumn(name = "follower_id"),        // userId of this entity
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<User> followings;

    @ManyToMany(mappedBy = "followings",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<User> followers;

    // Post

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)  // Field user of Post class
    private List<Post> posts;

    @ManyToMany(mappedBy = "liked", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Post> likedPost;

    @ManyToMany(mappedBy = "saved", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Post> savedPost;

    // Comment

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments;

    @ManyToMany(mappedBy = "liked", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Comment> likedComments;

    // Chat

    @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Chat> chats;

    @PrePersist
    public void prePersist()
    {
        this.userId = "user-" + UUID.randomUUID().toString();
    }
}


