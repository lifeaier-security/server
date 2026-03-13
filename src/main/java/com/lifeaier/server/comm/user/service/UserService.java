package com.lifeaier.server.comm.user.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.lifeaier.server.comm.user.dto.UserDTO;
import com.lifeaier.server.comm.user.entity.UserEntity;
import com.lifeaier.server.comm.oauth2.mapper.OAuth2Mapper;
import com.lifeaier.server.comm.token.mapper.TokenRefreshMapper;
import com.lifeaier.server.comm.user.mapper.UserMapper;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final OAuth2Mapper oauth2Mapper;
    private final TokenRefreshMapper tokenRefreshMapper;

    public UserService(UserMapper userMapper,
                       OAuth2Mapper oauth2Mapper,
                       TokenRefreshMapper tokenRefreshMapper) {

        this.userMapper = userMapper;
        this.oauth2Mapper = oauth2Mapper;
        this.tokenRefreshMapper = tokenRefreshMapper;
    }

    public UserDTO selectByUsername() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        UserEntity entity = userMapper.selectByUsername(username);
        if(entity != null) {
            return entity.getUserDTO();
        }
        return null;
    }

    public Object delete() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        UserEntity entity = userMapper.selectByUsername(username);
        if(entity == null) {
            return 0;
        }

        // 1. delete vc_word, vc_mean by user_id

        // 2. delete oauth2_authorized_client by username = principal_name
        oauth2Mapper.deleteByUsername(username);

        // 3. delete mt_refresh_token by username
        // token will be deleted in MyLogoutHelder
        //tokenMapper.deleteByUsername(username);

        // 4. delete mt_user by username
        userMapper.deleteByUsername(username);

        // 5. logout in controller

        return 1;
    }
}