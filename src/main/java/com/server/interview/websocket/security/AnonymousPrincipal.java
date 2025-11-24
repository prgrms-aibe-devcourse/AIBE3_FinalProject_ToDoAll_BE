package com.server.interview.websocket.security;

import java.security.Principal;

public class AnonymousPrincipal implements Principal {

    @Override
    public String getName() {
        return "anonymous";
    }
}
