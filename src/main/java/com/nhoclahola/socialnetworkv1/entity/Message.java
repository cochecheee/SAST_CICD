package com.nhoclahola.socialnetworkv1.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message
{
    @Id
    private String messageId;
    @Column(nullable = false, length = 3000)
    private String content;
    private String imageUrl;
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @PrePersist
    public void prePersist()
    {
        this.messageId = "message-" + UUID.randomUUID().toString();
    }
}
