package com.buccees.vigil.interaction;

import java.util.Set;

public record AuthorizationContext(Set<String> permissions) {
    public AuthorizationContext {
        permissions = Set.copyOf(permissions);
    }

    public boolean permits(String operation) {
        return permissions.contains(operation);
    }
}
