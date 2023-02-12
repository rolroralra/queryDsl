package com.example.querydsl.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles(value = {"test"})
//@Commit
class MemberTest {
    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("testEntity")
    void testEntity() {
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

        em.flush();
        em.clear();

        // When
        List<Member> members = em.createQuery("select m from Member m", Member.class)
            .getResultList();

        // Then
        members.forEach(member -> {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());
        });
    }
}
