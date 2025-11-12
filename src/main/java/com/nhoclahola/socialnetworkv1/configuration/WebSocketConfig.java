package com.nhoclahola.socialnetworkv1.configuration;

import com.nhoclahola.socialnetworkv1.security.WebSocketChannelInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/group", "/user", "/chat");
        registry.setUserDestinationPrefix("/user");
    }

    @Bean
    public WebSocketChannelInterceptor webSocketChannelInterceptor()
    {
        return new WebSocketChannelInterceptor();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration)
    {
        registration.interceptors(webSocketChannelInterceptor());
    }
}
