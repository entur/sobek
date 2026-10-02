package org.rutebanken.sobek.permission;

import org.entur.auth.permission.client.model.UserInformation;
import org.entur.auth.permission.client.spring.bean.UserInformationProvider;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Extracts user information from JWT token for the new permission store.
 */
public class PermissionStoreUserInfoExtractor implements UserInfoExtractor {

    private static final Logger logger = LoggerFactory.getLogger(
        PermissionStoreUserInfoExtractor.class
    );

    private final UserInformationProvider userInformationProvider;

    public PermissionStoreUserInfoExtractor(UserInformationProvider userInformationProvider) {
        this.userInformationProvider = userInformationProvider;
    }

    @Override
    public String getPreferredName() {

        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            logger.debug("No JWT authentication found");
            return null;
        }

        UserInformation userInformation = userInformationProvider.getUserInformation(authentication);

        if (userInformation != null) {
            return userInformation.getName();
        } else {
            logger.debug("No user information found in permission store");
        }

        return null;
    }

    @Override
    public String getPreferredUsername() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        if (authentication == null) {
            logger.debug("No authentication found");
            return null;
        }
        return getPreferredUsername(authentication);
    }

    public String getPreferredUsername(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            logger.debug("No JWT authentication found");
            return null;
        }

        UserInformation userInformation = userInformationProvider.getUserInformation(authentication);

        if (userInformation != null) {
            return userInformation.getSubject();
        } else {
            logger.debug("No user information found in permission store");
        }

        return null;
    }
}