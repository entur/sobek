package org.rutebanken.sobek.auth;

import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Service
public class DummyUserinfoExtractor implements UserInfoExtractor {
    @Nullable
    @Override
    public String getPreferredName() {
        return "test";
    }

    @Nullable
    @Override
    public String getPreferredUsername() {
        return "test";
    }
}
