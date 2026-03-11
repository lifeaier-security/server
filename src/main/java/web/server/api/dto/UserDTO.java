package web.server.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
	
    private String username;    
    private String name;
    private String email;
    private String role;
    private String pictureUrl;
}
