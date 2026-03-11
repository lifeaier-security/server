package web.server.api.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadUtility {
	
	public static byte[] downloadImage(String imageUrl) throws IOException {
		
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        connection.connect();

        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes(); // Convert input stream to byte array
        }
    }

}
