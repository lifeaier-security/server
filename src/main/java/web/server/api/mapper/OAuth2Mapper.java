package web.server.api.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2Mapper {

    int deleteByUsername(String username);
}
