package com.nhoclahola.socialnetworkv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;


@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class SocialNetworkV1Application
{

    public static void main(String[] args)
    {
        SpringApplication.run(SocialNetworkV1Application.class, args);
    }

}
