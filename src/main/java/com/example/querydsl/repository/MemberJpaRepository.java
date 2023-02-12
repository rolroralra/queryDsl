package com.example.querydsl.repository;

import static com.example.querydsl.domain.QMember.*;
import static com.example.querydsl.domain.QTeam.*;
import static java.util.Objects.*;
import static org.apache.commons.lang3.StringUtils.*;

import com.example.querydsl.controller.dto.MemberSearchCondition;
import com.example.querydsl.controller.dto.MemberTeamDto;
import com.example.querydsl.controller.dto.QMemberTeamDto;
import com.example.querydsl.domain.Member;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class MemberJpaRepository {
    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }

    public List<Member> findAllByQueryDsl() {
        return queryFactory
            .selectFrom(member)
            .fetch();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
            .setParameter("name", name)
            .getResultList();
    }

    public List<Member> findByNameByQueryDsl(String name) {
        return queryFactory
            .selectFrom(member)
            .where(member.name.eq(name))
            .fetch();
    }

    public List<MemberTeamDto> searchByConditionByBooleanBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        if (isNotBlank(condition.getMemberName())) {
            builder.and(member.name.eq(condition.getMemberName()));
        }

        if (isNotBlank(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (nonNull(condition.getAgeGoe())) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (nonNull(condition.getAgeLoe())) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
            .select(new QMemberTeamDto(
                member.id,
                member.name,
                member.age,
                team.id,
                team.name))
            .from(member)
            .leftJoin(member.team, team)
            .where(builder)
            .fetch();
    }

    public List<MemberTeamDto> searchByCondition(MemberSearchCondition condition) {
        return queryFactory
            .select(new QMemberTeamDto(
                member.id,
                member.name,
                member.age,
                team.id,
                team.name))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                condition.memberNameEq(),
                condition.teamNameEq(),
                condition.ageGoe(),
                condition.ageLoe()
            ).fetch();
    }

    public List<Member> findAll(MemberSearchCondition condition) {
        return queryFactory
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                condition.memberNameEq(),
                condition.teamNameEq(),
                condition.ageGoe(),
                condition.ageLoe()
            ).fetch();
    }
}
