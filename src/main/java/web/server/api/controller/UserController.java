package web.server.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.server.api.jwt.MyLogoutHelper;
import web.server.api.service.UserService;

import static java.lang.Integer.parseInt;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final MyLogoutHelper myLogoutHelper;

    public UserController(UserService userService,
                          MyLogoutHelper myLogoutHelper) {

        this.userService = userService;
        this.myLogoutHelper = myLogoutHelper;
    }

    @GetMapping("")
    public Object user() {

        return userService.selectByUsername();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> delete(HttpServletRequest request,
                                       HttpServletResponse response) {
        if(parseInt(userService.delete().toString()) > 0) {
            myLogoutHelper.logout(request, response);
        }
        return ResponseEntity.ok().build();
    }
}