package com.example.querydsl.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberSearchCondition {
    private final String memberName;
    private final String teamName;
    private final Integer ageGoe;
    private final Integer ageLoe;
}
