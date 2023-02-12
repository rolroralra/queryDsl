package com.example.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.controller.dto.MemberSearchCondition;
import com.example.querydsl.controller.dto.MemberTeamDto;
import com.example.querydsl.domain.Member;
import com.example.querydsl.domain.Team;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles(value = {"test"})
class MemberJpaRepositoryTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Optional<Member> findById = memberJpaRepository.findById(member.getId());
        assertThat(findById).isPresent()
            .get()
            .isEqualTo(member);

        List<Member> findALl = memberJpaRepository.findAll();
        assertThat(findALl).contains(member);

        List<Member> findByName = memberJpaRepository.findByName("member1");

        assertThat(findByName).contains(member);
    }

    @Test
    void basicQueryDslTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Optional<Member> findById = memberJpaRepository.findById(member.getId());
        assertThat(findById).isPresent()
            .get()
            .isEqualTo(member);

        List<Member> findALl = memberJpaRepository.findAllByQueryDsl();
        assertThat(findALl).contains(member);

        List<Member> findByName = memberJpaRepository.findByNameByQueryDsl("member1");

        assertThat(findByName).contains(member);
    }

    @Test
    void booleanBuilder() {
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

        MemberSearchCondition condition = MemberSearchCondition.builder()
            .teamName("teamB")
            .ageGoe(35)
            .ageLoe(40)
            .build();

        List<MemberTeamDto> result = memberJpaRepository.searchByConditionByBooleanBuilder(condition);

        assertThat(result).hasSize(1)
            .extracting(MemberTeamDto::getMemberName)
            .containsExactly("member4");
    }

    @Test
    void dynamicQuery() {
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

        MemberSearchCondition condition = MemberSearchCondition.builder()
            .teamName("teamB")
            .ageGoe(35)
            .ageLoe(40)
            .build();

        List<MemberTeamDto> result = memberJpaRepository.searchByCondition(condition);

        assertThat(result).hasSize(1)
            .extracting(MemberTeamDto::getMemberName)
            .containsExactly("member4");
    }

    @Test
    void dynamicQuery2() {
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

        MemberSearchCondition condition = MemberSearchCondition.builder()
            .teamName("teamB")
            .ageGoe(35)
            .ageLoe(40)
            .build();

        List<Member> result = memberJpaRepository.findAll(condition);

        assertThat(result).hasSize(1)
            .extracting(Member::getName)
            .containsExactly("member4");
    }
}
