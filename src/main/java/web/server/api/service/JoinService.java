package web.server.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import web.server.api.dto.JoinDTO;
import web.server.api.entity.TokenMailEntity;
import web.server.api.entity.UserEntity;
import web.server.api.mapper.UserMapper;
import web.server.api.utility.MailVerificationUtility;

import java.time.Instant;

@Service
public class JoinService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final MailVerifyService mailVerifyService;
    private final SecretService secretService;

    private final MailSendService mailSendService;

    @Value("${app.url}")
    private String appUrl;

    public JoinService(UserMapper userMapper,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       MailVerifyService mailVerifyService,
                       SecretService secretService,
                       MailSendService mailSendService) {

        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mailVerifyService = mailVerifyService;
        this.secretService = secretService;
        this.mailSendService = mailSendService;
    }

    public void join(JoinDTO dto) {

        UserEntity userEntity = new UserEntity();
        userEntity.setProvider("local");
        userEntity.setUsername(dto.getUsername());
        userEntity.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        userEntity.setName(dto.getName());
        userEntity.setEmail(dto.getEmail());
        userEntity.setRole("ROLE_USER");
        userEntity.setVerified('N');

        if (userMapper.existsByUsername(userEntity) > 0) {
            throw new IllegalStateException("Username already exists");
        }

        int result = userMapper.insert(userEntity);
        if(result > 0) {

            String username = dto.getUsername();
            String token = MailVerificationUtility.generateToken();

            TokenMailEntity entity = new TokenMailEntity();
            entity.setUsername(username);
            entity.setToken(token);
            entity.setExpiration(Instant.now().plusMillis(secretService.getTokenMailExpire()));
            mailVerifyService.insert(entity);

            String url = appUrl + "/verify?token=" + token;

            mailSendService.sendMail(
                    dto.getEmail(),
                    "Verify Mail",
                    url
            );
        }
    }
}
