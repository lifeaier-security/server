package com.lifeaier.base.comm.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.lifeaier.base.comm.user.dto.MyUserDetails;
import com.lifeaier.base.comm.user.entity.UserEntity;
import com.lifeaier.base.comm.user.mapper.UserMapper;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public MyUserDetailsService(UserMapper userMapper) {

        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userMapper.selectByUsername(username);

        if (userEntity != null) {

            return new MyUserDetails(userEntity);
        }

        return null;
    }
}
