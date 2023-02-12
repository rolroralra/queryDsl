package com.example.querydsl.controller.dto;

import static com.example.querydsl.domain.QMember.member;
import static com.example.querydsl.domain.QTeam.team;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.Arrays;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberSearchCondition {
    private final String memberName;
    private final String teamName;
    private final Integer ageGoe;
    private final Integer ageLoe;

    public BooleanExpression[] allCondition() {
        return Arrays.asList(memberNameEq(), teamNameEq(), ageGoe(), ageLoe())
            .toArray(new BooleanExpression[0]);
    }

    public BooleanExpression ageLoe() {
        return Optional.ofNullable(getAgeLoe()).map(member.age::loe).orElse(null);
    }

    public BooleanExpression ageGoe() {
        return Optional.ofNullable(getAgeGoe()).map(member.age::goe).orElse(null);
    }

    public BooleanExpression teamNameEq() {
        return Optional.ofNullable(getTeamName()).map(team.name::eq).orElse(null);
    }

    public BooleanExpression memberNameEq() {
        return Optional.ofNullable(getMemberName()).map(member.name::eq).orElse(null);
    }
}
