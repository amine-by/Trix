package com.amine.trix.security;

import java.util.Map;

import com.amine.trix.enums.Provider;
import com.amine.trix.exception.OAuth2AuthenticationProcessingException;

public class OAuth2UserInfoFactory {
	 public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
	        if(registrationId.equalsIgnoreCase(Provider.google.toString())) {
	            return new GoogleOAuth2UserInfo(attributes);
	        } else if (registrationId.equalsIgnoreCase(Provider.facebook.toString())) {
	            return new FacebookOAuth2UserInfo(attributes);
	        } else {
	            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
	        }
	    }
}
