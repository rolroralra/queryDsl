package com.example.querydsl.controller;

import com.example.querydsl.controller.dto.MemberSearchCondition;
import com.example.querydsl.controller.dto.MemberTeamDto;
import com.example.querydsl.domain.Member;
import com.example.querydsl.repository.MemberJpaRepository;
import com.example.querydsl.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMembersV1(MemberSearchCondition condition) {
        return memberJpaRepository.searchByCondition(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }

    @GetMapping("/v4/members")
    public List<MemberTeamDto> searchMembersByOrder(MemberSearchCondition condition, Pageable pageable) {
        // Pageable::getSort()
        // 정렬 조건이 조금만 복잡해져도 Pageable의 Sort 기능을 사용하기 어렵다.
        // Root Entity 범위를 넘어가는 동적 정렬 기능이 필요하면 Pageable의 Sort를 사용하기 보다는
        // 파라미터를 직접 받아서 처리하는 것을 권장한다.
         return memberRepository.searchByOrder(condition, pageable);
    }

    @GetMapping("/v5/members")
    public Page<Member> searchMembersByQuerydslPredicateExecutor(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }
}
