package com.lifeaier.base.comm.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.lifeaier.base.comm.user.entity.UserEntity;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserEntity> select();

    UserEntity selectByUsername(String username);

    int insert(UserEntity entity);

    int update(UserEntity entity);

    int deleteByUsername(String username);

    int existsByUsername(UserEntity entity);

    int verify(String username);
}