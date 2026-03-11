package web.server.api.oauth2;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class MyClientRegistrationRepository {

	private final MyClientRegistration socialClientRegistration;
	
	public MyClientRegistrationRepository(MyClientRegistration socialClientRegistration) {

        this.socialClientRegistration = socialClientRegistration;
    }
	
	public ClientRegistrationRepository clientRegistrationRepository() {

        return new InMemoryClientRegistrationRepository(
				socialClientRegistration.googleClientRegistration()
        		, socialClientRegistration.facebookClientRegistration());
    }
}
