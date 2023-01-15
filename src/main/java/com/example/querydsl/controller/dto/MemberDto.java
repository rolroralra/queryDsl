package com.example.querydsl.controller.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberDto {
    private final String name;

    private final Integer age;

    @QueryProjection
    public MemberDto(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
