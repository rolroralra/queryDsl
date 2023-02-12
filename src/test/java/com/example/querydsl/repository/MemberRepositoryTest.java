package com.example.querydsl.repository;

import static com.example.querydsl.domain.QMember.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.controller.dto.MemberSearchCondition;
import com.example.querydsl.controller.dto.MemberTeamDto;
import com.example.querydsl.domain.Member;
import com.example.querydsl.domain.Team;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles(value = {"test"})
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class MemberRepositoryTest {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("basicTest")
    @Order(1)
    void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        assertThat(memberRepository.findById(member.getId()))
            .isPresent()
            .get()
            .isEqualTo(member);

        List<Member> findAll = memberRepository.findAll();
        assertThat(findAll).hasSize(1)
            .containsExactly(member);

        List<Member> findByName = memberRepository.findByName("member1");
        assertThat(findByName).hasSize(1)
            .containsExactly(member);
    }

    @Test
    @DisplayName("searchTest")
    @Order(2)
    void searchTest() {
        // Given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // When
        MemberSearchCondition condition = MemberSearchCondition.builder()
            .teamName("teamB")
            .ageLoe(40)
            .ageGoe(35)
            .build();

        List<MemberTeamDto> result = memberRepository.search(condition);

        // Then
        assertThat(result).hasSize(1)
            .extracting(MemberTeamDto::getMemberName)
            .containsExactly("member4");
    }

    @Test
    @DisplayName("querydslPredicateExecutor")
    @Order(2)
    void querydslPredicateExecutor() {
        // Given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // When
        Iterable<Member> result = memberRepository.findAll(
            member.age.between(10, 40)
                .and(member.name.eq("member1"))
        );

        // Then
        assertThat(result).hasSize(1)
            .extracting(Member::getName)
            .containsExactly("member1");
    }
}
