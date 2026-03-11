package web.server.api.dto;

import java.util.Map;

public class GoogleReponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public GoogleReponse(Map<String, Object> attribute) {

        this.attribute = attribute;
    }

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {

        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }

    @Override
    public String getName() {

        return attribute.get("name").toString();
    }
    
    @Override
    public String getPictureURL() {

    	if(attribute.containsKey("picture")) {
    		return attribute.get("picture").toString();	
    	} 
    	return null;
    }
    
    @Override
    public String getUsername() {
    	
    	String provider = this.getProvider();
        String providerId = this.getProviderId();
        String username = provider + "_" + providerId;
        return username;
    }
}
