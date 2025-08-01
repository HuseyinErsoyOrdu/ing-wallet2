package com.ing.digital.wallet.util;

import com.ing.digital.wallet.prm.PRM;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    public String getUserNameFromToken() {
        return String.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public boolean isEmployee() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(PRM.Role.EMPLOYEE.name()));
    }

    public boolean isSameUser(String userName) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getName().equalsIgnoreCase(userName);
    }
}

