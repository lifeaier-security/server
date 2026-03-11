package web.server.api.dto;

import java.util.Map;

public class FacebookResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public FacebookResponse(Map<String, Object> attribute) {

        this.attribute = attribute;
    }

    @Override
    public String getProvider() {

        return "facebook";
    }

    @Override
    public String getProviderId() {

        return attribute.get("id").toString();
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
            Map<String, Object> picture = (Map<String, Object>) attribute.get("picture");
            Map<String, Object> data = (Map<String, Object>) picture.get("data");
            return (String) data.get("url");
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
