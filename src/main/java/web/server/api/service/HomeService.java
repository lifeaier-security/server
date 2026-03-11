package web.server.api.service;

import org.springframework.stereotype.Service;
import web.server.api.mapper.UserMapper;

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
