package com.lifeaier.server.menu._1_home.service;

import org.springframework.stereotype.Service;
import com.lifeaier.server.comm.user.mapper.UserMapper;

@Service
public class HomeService {

    private final UserMapper userMapper;

    public HomeService(UserMapper userMapper) {

        this.userMapper = userMapper;
    }

    public Object select() {

        return userMapper.select();
    }
}
