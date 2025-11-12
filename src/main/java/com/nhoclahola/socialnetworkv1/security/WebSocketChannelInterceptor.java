package com.nhoclahola.socialnetworkv1.security;

import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor
{
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assert accessor != null;
        String jwt = accessor.getFirstNativeHeader("Authorization");
        if (jwt != null && Objects.equals(accessor.getCommand(), StompCommand.SUBSCRIBE))
        {
            try
            {
                Claims claims = JwtProvider.introspect(jwt);
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Store Authentication in SimpAttributes for later use
                SecurityContext securityContext = SecurityContextHolder.getContext();
                accessor.getSessionAttributes().put("SPRING_SECURITY_CONTEXT", securityContext);
            }
            catch (Exception exception)
            {
                SecurityContextHolder.clearContext();
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
        }
        if (Objects.equals(accessor.getCommand(), StompCommand.SEND))
        {
            System.out.println("send");
            // Retrieve the SecurityContext from SimpSessionAttributes
            SecurityContext securityContext = (SecurityContext) accessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT");
            if (securityContext != null)
                accessor.setUser(securityContext.getAuthentication());
        }
        return message;
    }
}
