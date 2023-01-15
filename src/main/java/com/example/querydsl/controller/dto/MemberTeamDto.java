package com.example.querydsl.controller.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberTeamDto {
    private final Long memberId;
    private final String memberName;
    private final Integer age;
    private final Long teamId;
    private final String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, String memberName, Integer age, Long teamId,
        String teamName) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
