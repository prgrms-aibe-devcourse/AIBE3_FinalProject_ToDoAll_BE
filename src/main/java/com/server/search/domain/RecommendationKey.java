package com.server.search.domain;

import java.io.Serializable;
import java.util.Objects;

public class RecommendationKey implements Serializable {
    private Long jdId;
    private Long resumeId;

    public RecommendationKey() {}

    public RecommendationKey(Long jdId, Long resumeId) {
        this.jdId = jdId;
        this.resumeId = resumeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecommendationKey)) return false;
        RecommendationKey that = (RecommendationKey) o;
        return Objects.equals(jdId, that.jdId) &&
                Objects.equals(resumeId, that.resumeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jdId, resumeId);
    }
}
