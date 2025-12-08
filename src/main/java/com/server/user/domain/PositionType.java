package com.server.user.domain;

public enum PositionType {
    OWNER("대표/Founder"),
    HR_MANAGER("인사/채용 담당자"),
    TEAM_LEAD("팀장/리더"),
    INTERVIEWER("실무 인터뷰어"),
    OTHER("기타");

    private final String description;

    PositionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
