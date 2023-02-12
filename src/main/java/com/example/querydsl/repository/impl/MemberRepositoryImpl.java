package com.example.querydsl.repository.impl;

import static com.example.querydsl.domain.QMember.*;
import static com.example.querydsl.domain.QTeam.*;

import com.example.querydsl.controller.dto.MemberSearchCondition;
import com.example.querydsl.controller.dto.MemberTeamDto;
import com.example.querydsl.controller.dto.QMemberTeamDto;
import com.example.querydsl.repository.MemberRepositoryCustom;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;


public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
            .select(new QMemberTeamDto(
                member.id,
                member.name,
                member.age,
                team.id,
                team.name)
            ).from(member)
            .leftJoin(member.team, team)
            .where(
                condition.allCondition()
//                condition.memberNameEq(),
//                condition.teamNameEq(),
//                condition.ageGoe(),
//                condition.ageLoe()
            ).fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
        Pageable pageable) {

        QueryResults<MemberTeamDto> results = queryFactory
            .select(new QMemberTeamDto(
                member.id,
                member.name,
                member.age,
                team.id,
                team.name
            )).from(member)
            .leftJoin(member.team, team)
            .where(
                condition.allCondition()
            ).offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
        Pageable pageable) {

        List<MemberTeamDto> content = queryFactory
            .select(new QMemberTeamDto(
                member.id,
                member.name,
                member.age,
                team.id,
                team.name
            )).from(member)
            .leftJoin(member.team, team)
            .where(
                condition.allCondition()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(member.count())
            .from(member)
            .leftJoin(member.team, team)
            .where(
                condition.allCondition()
            );

        Long total = countQuery.fetchOne();

        assert total != null;

//        return new PageImpl<>(content, pageable, total);
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MemberTeamDto> searchByOrder(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<MemberTeamDto> query = queryFactory
            .select(new QMemberTeamDto(
                member.id,
                member.name,
                member.age,
                team.id,
                team.name
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                condition.allCondition()
            ).offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        // Pageable::getSort()
        // 정렬 조건이 조금만 복잡해져도 Pageable의 Sort 기능을 사용하기 어렵다.
        // Root Entity 범위를 넘어가는 동적 정렬 기능이 필요하면 Pageable의 Sort를 사용하기 보다는
        // 파라미터를 직접 받아서 처리하는 것을 권장한다.
        for (Sort.Order order : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(member.getType(), member.getMetadata());

            query.orderBy(new OrderSpecifier(
                order.isAscending() ? Order.ASC : Order.DESC,
                pathBuilder.get(order.getProperty())
            ));
        }
        return query.fetch();
    }
}
