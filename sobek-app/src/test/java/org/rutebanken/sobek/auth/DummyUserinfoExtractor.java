package org.rutebanken.sobek.auth;

import javax.annotation.Nullable;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
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
