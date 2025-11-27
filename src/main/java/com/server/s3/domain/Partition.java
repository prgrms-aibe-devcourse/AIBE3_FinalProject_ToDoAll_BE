package com.server.s3.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Partition {
    USER("user"),
    JOB("job"),
    RESUME("resume");

    private final String value;

}
