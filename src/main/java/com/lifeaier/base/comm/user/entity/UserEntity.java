package com.lifeaier.base.comm.user.entity;

import lombok.Getter;
import lombok.Setter;
import com.lifeaier.base.comm.user.dto.UserDTO;
import com.lifeaier.base.comm.utility.Base64Util;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class UserEntity {

    private int userId;
    private String provider;
    private String username;
    private String password;
    private String name;
    private String email;
    private String role;
    private byte[] picture;
    private char verified;

    public UserDTO getUserDTO() {

        String pictureSrc = picture != null
                ? "data:image/png;base64," + Base64Util.encodeToBase64(picture)
                : null;

        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setName(name);
        dto.setEmail(email);
        dto.setRole(role);
        dto.setPictureUrl(pictureSrc);

        return dto;
    }

    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();

        map.put("userId", userId);
        map.put("username", username);
        map.put("name", name);
        map.put("email", email);
        map.put("role", role);
        map.put("picture", picture);

        return map;
    }
}