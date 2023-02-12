package com.example.querydsl.domain;

import static com.example.querydsl.domain.QMember.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.controller.dto.MemberDto;
import com.example.querydsl.controller.dto.QMemberDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@ActiveProfiles(value = {"test"})
public class QueryDslInterTest {
    @PersistenceContext
    private EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    void beforeEach() {
        queryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    @Order(1)
    void projectionOne() {
        List<String> result = queryFactory
            .select(member.name)
            .from(member)
            .fetch();

        assertThat(result).hasSize(4)
            .contains("member1", "member2", "member3", "member4");
    }

    @Test
    @Order(2)
    void projectionTuple() {
        List<Tuple> result = queryFactory
            .select(member.name, member.age)
            .from(member)
            .fetch();

        assertThat(result).hasSize(4);

        result.forEach(tuple -> {
            System.out.println("tuple.get(member.name) = " + tuple.get(member.name));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));
        });
    }

    @Test
    @Order(3)
    void projectionDtoByJpql() {
        List<MemberDto> result = em.createQuery(
                "select new com.example.querydsl.controller.dto.MemberDto(m.name, m.age)"
                    + " from Member m", MemberDto.class)
            .getResultList();

        assertThat(result).hasSize(4);

        result.forEach(System.out::println);
    }

    @Test
    @Order(4)
    void projectionDtoByQueryDsl() {
        List<MemberDto> result = queryFactory
//            .select(Projections.bean(MemberDto.class, member.name, member.age))       // 1. By Property (Setter)
//            .select(Projections.fields(MemberDto.class, member.name, member.age))     // 2. By Field
            .select(Projections.constructor(MemberDto.class, member.name, member.age))  // 3. By Constructor
            .from(member)
            .fetch();

        assertThat(result).hasSize(4);

        result.forEach(System.out::println);
    }

    @Test
    @Order(5)
    void projectionDtoWithDifferentFieldNames() {
        QMember memberSub = new QMember("memberSub");

        List<MemberDto> result = queryFactory
            .select(Projections.constructor(
                    MemberDto.class,
                    member.name.as("name"),
                    ExpressionUtils.as(
                        JPAExpressions
                            .select(memberSub.age.max())
                            .from(memberSub), "age")
                )
            ).from(member)
            .fetch();

        assertThat(result).hasSize(4);
        result.forEach(memberDto ->
            assertThat(memberDto.getAge()).isEqualTo(40)
        );
    }

    @Test
    @Order(6)
    void projectionDtoWithQueryProjection() {
        List<MemberDto> result = queryFactory
            .select(new QMemberDto(member.name, member.age))
            .from(member)
            .fetch();

        assertThat(result).hasSize(4);

        result.forEach(System.out::println);
    }

    @Test
    @Order(7)
    void distinct() {
        List<String> result = queryFactory
            .select(member.name).distinct()
            .from(member)
            .fetch();

        assertThat(result).hasSize(4);
    }

    @Test
    @Order(8)
    void dynamicQueryByBooleanBuilder() {
        String nameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMemberByBooleanBuilder(nameParam, ageParam);

        assertThat(result).hasSize(1);
        result.forEach(member ->
            assertThat(member).isNotNull()
                .hasFieldOrPropertyWithValue("name", nameParam)
                .hasFieldOrPropertyWithValue("age", ageParam));
    }

    @Test
    @Order(9)
    void dynamicQueryByWhereVargs() {
        String nameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMemberByWhere(nameParam, ageParam);

        assertThat(result).hasSize(1);
        result.forEach(member ->
            assertThat(member).isNotNull()
                .hasFieldOrPropertyWithValue("name", nameParam)
                .hasFieldOrPropertyWithValue("age", ageParam));
    }

    @Test
    @Order(10)
    void bulkUpdate() {
        long batchCount = queryFactory
            .update(member)
            .set(member.name, member.name.concat("-YOUNG"))
            .where(member.age.lt(28))
            .execute();

        assertThat(batchCount).isEqualTo(2);

        List<Member> result = queryFactory
            .selectFrom(member)
            .fetch();

        result.forEach(member -> {
            if (member.getAge() < 28) {
                assertThat(member.getName()).endsWith("-YOUNG");
            }
        });
    }

    @Test
    @Order(11)
    void bulkUpdateByAddOrMultiply() {
        long batchCount = queryFactory
            .update(member)
            .set(member.age, member.age.add(1))
//            .set(member.age, member.age.multiply(2))
//            .set(member.age, member.age.subtract(10))
//            .set(member.age, member.age.divide(2))
            .execute();

        assertThat(batchCount).isEqualTo(4);

        List<Member> result = queryFactory
            .selectFrom(member)
            .fetch();

        assertThat(result).extracting(Member::getAge)
            .containsOnly(11, 21, 31, 41);
    }

    @Test
    @Order(12)
    void bulkDelete() {
        long batchCount = queryFactory
            .delete(member)
            .where(member.age.gt(18))
            .execute();

        assertThat(batchCount).isEqualTo(3);

        Long count = queryFactory.select(member.countDistinct()).from(member).fetchOne();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Order(13)
    void sqlFunctionByExpressionsStringTemplate() {
        List<String> result = queryFactory
            .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.name,
                "member", "M"))
            .from(member)
            .fetch();

        result.forEach(name -> assertThat(name).startsWith("M"));
    }

    @Test
    @Order(14)
    void sqlFunctionByExpressionsStringTemplate2() {
        List<String> result = queryFactory
            .select(member.name)
            .from(member)
            .where(member.name.eq(Expressions.stringTemplate("function('lower', {0})", member.name)))
//            .where(member.name.eq(member.name.lower()))
            .fetch();

        result.forEach(name -> assertThat(name).isLowerCase());
    }

    private List<Member> searchMemberByBooleanBuilder(String name, Integer age) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.isNotBlank(name)) {
            builder.and(member.name.eq(name));
        }

        if (Objects.nonNull(age)) {
            builder.and(member.age.eq(age));
        }

        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch();
    }

    private List<Member> searchMemberByWhere(String name, Integer age) {
        return queryFactory
            .selectFrom(member)
            .where(
                Optional.ofNullable(name).map(member.name::eq).orElse(null),
                Optional.ofNullable(age).map(member.age::eq).orElse(null))
            .fetch();
    }
}
