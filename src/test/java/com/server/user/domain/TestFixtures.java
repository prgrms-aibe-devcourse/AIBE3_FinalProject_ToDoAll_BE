package com.server.user.domain;

import org.springframework.test.util.ReflectionTestUtils;

public class TestFixtures {
    public static User createUser(String email, String name) {
        User user = new User();
        ReflectionTestUtils.setField(user, "email", email);
        ReflectionTestUtils.setField(user, "name", name);
        ReflectionTestUtils.setField(user, "companyName", "테스트 회사");
        ReflectionTestUtils.setField(user, "position", "테스트 직무");
        ReflectionTestUtils.setField(user, "status", EmailStatus.VERIFIED);
        return user;
    }
}