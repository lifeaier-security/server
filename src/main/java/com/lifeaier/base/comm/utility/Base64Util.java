package com.lifeaier.base.comm.utility;

import java.util.Base64;

public class Base64Util {

	public static String encodeToBase64(byte[] imageBytes) {
		
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
