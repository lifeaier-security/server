package web.server.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class TokenEntity {

    private String username;
    private String token;
    private Instant expiration;
}