package com.server.s3.service;

import com.server.global.exception.ApplicationException;
import com.server.s3.domain.Partition;
import com.server.s3.exception.S3ErrorCase;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record S3FileKey(
        String partition,
        String relativeId,
        String uuid,
        String tag,
        String extension
) {
    private static final Pattern FILE_KEY_PATTERN = Pattern.compile(
            "(?<partition>[^/]+)/(?<relativeId>[^/]+)/(?<uuid>[^_]+)_(?<tag>[^.]+)\\.(?<ext>.+)"
    );

    public static S3FileKey from (String key) {
        Matcher matcher = FILE_KEY_PATTERN.matcher(key);
        if (!matcher.matches()) {
            throw new ApplicationException(S3ErrorCase.INVALID_FILE_KEY);
        }

        String partition = matcher.group("partition");

        boolean exists = Arrays.stream(Partition.values())
                .anyMatch(p -> p.getValue().equals(partition));

        if (!exists) {
            throw new ApplicationException(S3ErrorCase.INVALID_FILE_KEY);
        }

        return new S3FileKey(
                partition,
                matcher.group("relativeId"),
                matcher.group("uuid"),
                matcher.group("tag"),
                matcher.group("ext")
        );
    }
}
