package com.flexydemy.content.config;


import com.flexydemy.content.model.ProfileRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserDetails implements Serializable {

    private String userId;
    private String email;
    private Set<ProfileRole> roles;

    // Optional convenience methods
    public boolean hasRole(ProfileRole role) {
        return roles != null && roles.contains(role);
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }
}
