package com.lifeaier.base.comm.oauth2.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2Mapper {

    int deleteByUsername(String username);
}
