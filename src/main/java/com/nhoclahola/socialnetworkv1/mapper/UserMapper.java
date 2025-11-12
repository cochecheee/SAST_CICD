package com.nhoclahola.socialnetworkv1.mapper;

import com.nhoclahola.socialnetworkv1.dto.auth.request.UserCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.user.UserWithData;
import com.nhoclahola.socialnetworkv1.dto.user.request.UserUpdateRequest;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserWithDataResponse;
import com.nhoclahola.socialnetworkv1.entity.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper
{
    public abstract User userLoginRequestToUser(UserCreateRequest request);

    @Mapping(target = "avatarUrl", expression = "java(user.getAvatarUrl() != null ? com.nhoclahola.socialnetworkv1.configuration.WebConfig.getUrl(user.getAvatarUrl()) : null)")
    @Mapping(target = "coverPhotoUrl", expression = "java(user.getCoverPhotoUrl() != null ? com.nhoclahola.socialnetworkv1.configuration.WebConfig.getUrl(user.getCoverPhotoUrl()) : null)")
    public abstract UserResponse toUserResponse(User user);

    public abstract List<UserResponse> toListUserResponse(List<User> users);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "avatarUrl", expression = "java(user.getAvatarUrl() != null ? com.nhoclahola.socialnetworkv1.configuration.WebConfig.getUrl(user.getAvatarUrl()) : null)")
    @Mapping(target = "coverPhotoUrl", expression = "java(user.getCoverPhotoUrl() != null ? com.nhoclahola.socialnetworkv1.configuration.WebConfig.getUrl(user.getCoverPhotoUrl()) : null)")
    @Mapping(source = "follow", target = "isFollow")
    public abstract UserWithDataResponse toUserWithDataResponse(UserWithData user);

    @AfterMapping
    public default Page<UserResponse> pageUserToPageUserResponse(Page<User> users)
    {
        List<UserResponse> userResponses = toListUserResponse(users.getContent());
        return new PageImpl<>(userResponses, users.getPageable(), users.getTotalElements());
    }
}
