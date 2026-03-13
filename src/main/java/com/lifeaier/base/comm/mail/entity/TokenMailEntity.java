package com.lifeaier.base.comm.mail.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class TokenMailEntity {

    private String username;
    private String token;
    private Instant expiration;
}