package web.server.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import web.server.api.dto.JoinDTO;
import web.server.api.entity.MailVerificationEntity;
import web.server.api.entity.UserEntity;
import web.server.api.mapper.UserMapper;
import web.server.api.utility.MailVerificationUtility;

import java.time.Instant;

@Service
public class JoinService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final MailVerificationService mailVerificationService;
    private final SecretService secretService;

    private final MailService mailService;

    @Value("${app.url}")
    private String appUrl;

    public JoinService(UserMapper userMapper,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       MailVerificationService mailVerificationService,
                       SecretService secretService,
                       MailService mailService) {

        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mailVerificationService = mailVerificationService;
        this.secretService = secretService;
        this.mailService = mailService;
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

            MailVerificationEntity entity = new MailVerificationEntity();
            entity.setUsername(username);
            entity.setToken(token);
            entity.setExpiration(Instant.now().plusMillis(secretService.getMailVerificationTokenExpire()));
            mailVerificationService.insert(entity);

            String url = appUrl + "/verify?token=" + token;

            mailService.sendMail(
                    dto.getEmail(),
                    "[SHINE-UG] Mail Verification",
                    url
            );
        }
    }
}
