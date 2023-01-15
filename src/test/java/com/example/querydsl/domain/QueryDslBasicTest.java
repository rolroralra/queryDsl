package com.example.querydsl.domain;

import static com.example.querydsl.domain.QMember.*;
import static com.example.querydsl.domain.QTeam.*;
import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueryDslBasicTest {
    @PersistenceContext
    private EntityManager em;

    private EntityManagerFactory emf;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    void beforeEach() {
        queryFactory = new JPAQueryFactory(em);
        emf = em.getEntityManagerFactory();

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
    void startJPQL() {
        String query = "select m from Member m where m.name = :name";

        Member result = em.createQuery(query, Member.class)
            .setParameter("name", "member3")
            .getSingleResult();

        assertThat(result).isNotNull()
            .hasFieldOrPropertyWithValue("name", "member3")
            .hasFieldOrPropertyWithValue("age", 30);
    }

    @Test
    @Order(2)
    void startQueryDSL() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");

        Member result = queryFactory.select(m)
            .from(m)
            .where(m.name.eq("member3"))
            .fetchOne();

        assertThat(result).isNotNull()
            .hasFieldOrPropertyWithValue("name", "member3")
            .hasFieldOrPropertyWithValue("age", 30);
    }

    @Test
    @Order(3)
    void startQueryDSLWithStaticImport() {
        Member result = queryFactory.select(member)
            .from(member)
            .where(member.name.eq("member1"))
            .fetchOne();

        assertThat(result).isNotNull()
            .hasFieldOrPropertyWithValue("name", "member1")
            .hasFieldOrPropertyWithValue("age", 10);
    }

    @Test
    @Order(4)
    void search() {
        Member result = queryFactory
            .selectFrom(member)
            .where(member.name.eq("member1")
                .and(member.age.eq(10)))
            .fetchOne();
        assertThat(result).isNotNull()
            .hasFieldOrPropertyWithValue("name", "member1")
            .hasFieldOrPropertyWithValue("age", 10);
    }

    @Test
    @Order(5)
    void searchAndVargs() {
        List<Member> result = queryFactory
            .selectFrom(member)
            .where(
                member.name.eq("member1"),
                member.age.eq(10)
            ).fetch();

        assertThat(result).hasSize(1);

        Member findMember = result.get(0);
        assertThat(findMember).isNotNull()
            .hasFieldOrPropertyWithValue("name", "member1")
            .hasFieldOrPropertyWithValue("age", 10);
    }

    @Test
    @Order(6)
    void fetch() {
        List<Member> result = queryFactory
            .selectFrom(member)
            .fetch();

        assertThat(result).hasSize(4);
    }

    @Test
    @Order(7)
    void fetchOne() {
        assertThatExceptionOfType(NonUniqueResultException.class).isThrownBy(() ->
                queryFactory
                    .selectFrom(member)
                    .fetchOne()
        );
    }

    @Test
    @Order(8)
    void fetchFirst() {
        Member result = queryFactory.selectFrom(member)
            .orderBy(member.id.desc())
            .fetchFirst();

        assertThat(result).isNotNull()
            .hasFieldOrPropertyWithValue("name", "member4")
            .hasFieldOrPropertyWithValue("age", 40);
    }

    @Test
    @Order(9)
    @SuppressWarnings("deprecation")
    void fetchResults() {
        QueryResults<Member> result = queryFactory.selectFrom(member)
            .fetchResults();    // Deprecated

        List<Member> results = result.getResults();

        assertThat(results).hasSize(4);
        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getOffset()).isEqualTo(0);
        System.out.println("result.getLimit() = " + result.getLimit());
    }

    @Test
    @Order(10)
    @SuppressWarnings("deprecation")
    void fetchCount() {
        long count = queryFactory.selectFrom(member)
            .fetchCount();// Deprecated

        assertThat(count).isEqualTo(4);
    }

    @Test
    @Order(11)
    void sort() {
        Member memberNull = new Member(null, 100);
        Member member5 = new Member("member5", 100);
        Member member6  = new Member("member6", 100);

        em.persist(memberNull);
        em.persist(member5);
        em.persist(member6);


        List<Member> result = queryFactory.selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.name.asc().nullsLast())
            .fetch();

        assertThat(result).hasSize(3)
            .containsExactly(member5, member6, memberNull);
    }

    @Test
    @Order(12)
    void paging() {
        List<Member> result = queryFactory.selectFrom(member)
            .orderBy(member.name.desc())
            .offset(1)  // zero base index
            .limit(2)
            .fetch();

        assertThat(result).hasSize(2);
    }

    @Test
    @Order(13)
    void aggregation() {
        List<Tuple> result = queryFactory
            .select(
                member.countDistinct(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min())
            .from(member)
            .fetch();

        assertThat(result).hasSize(1);
        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.countDistinct())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    @Order(14)
    void grouping() {
        List<Tuple> result = queryFactory.select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .orderBy(team.name.asc())
            .fetch();

        Long teamCount = queryFactory.select(team.count()).from(team).fetchOne();
        assertThat(teamCount).isNotNull().isPositive();
        assertThat(result).hasSize(teamCount.intValue());

        Tuple tupleOfTeamA = result.get(0);
        Tuple tupleOfTeamB = result.get(1);

        assertThat(tupleOfTeamA.get(team.name)).isEqualTo("teamA");
        assertThat(tupleOfTeamA.get(member.age.avg())).isEqualTo(15);

        assertThat(tupleOfTeamB.get(team.name)).isEqualTo("teamB");
        assertThat(tupleOfTeamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    @Order(15)
    void having() {
        List<Tuple> result = queryFactory.select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .having(member.age.avg().goe(20))
            .orderBy(team.name.asc())
            .fetch();


        assertThat(result).hasSize(1);

        Tuple tupleOfTeamB = result.get(0);

        assertThat(tupleOfTeamB.get(team.name)).isEqualTo("teamB");
        assertThat(tupleOfTeamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    @Order(16)
    void join() {
        List<Member> result = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .orderBy(member.name.desc())
            .fetch();

        assertThat(result).hasSize(2)
            .extracting(Member::getName)
            .containsExactly("member2", "member1");
    }

    @Test
    @Order(17)
    void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
            .select(member)
            .from(member, team)
            .where(member.name.eq(team.name))
            .orderBy(member.name.asc())
            .fetch();

        assertThat(result).hasSize(2)
            .extracting(Member::getName)
            .containsExactly("teamA", "teamB");
    }

    @Test
    @Order(18)
    void joinOnFiltering() {
        List<Tuple> result = queryFactory.select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq("teamA"))
            .fetch();

        result.forEach(tuple -> System.out.println("tuple = " + tuple));

        assertThat(result).hasSize(4);
    }

    @Test
    @Order(19)
    void joinOnThetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.name.eq(team.name))   // 일반 left 조인: leftJoin(member.team, team)
            .fetch();

        result.forEach(tuple -> System.out.println("tuple = " + tuple));

        assertThat(result).hasSize(6);
    }

    @Test
    @Order(20)
    void withOutFetchJoin() {
        Member findMember = queryFactory.selectFrom(member)
            .where(member.name.eq("member1"))
            .fetchOne();

        assertThat(findMember).isNotNull();

        assertThat(Hibernate.isInitialized(findMember.getTeam())).isFalse();
        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isFalse();

        System.out.println("findMember.getTeamName() = " + findMember.getTeamName());

        assertThat(Hibernate.isInitialized(findMember.getTeam())).isTrue();
        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isTrue();
    }

    @Test
    @Order(21)
    void fetchJoin() {
        Member findMember = queryFactory
            .selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.name.eq("member1"))
            .fetchOne();

        assertThat(findMember).isNotNull();
        assertThat(Hibernate.isInitialized(findMember.getTeam())).isTrue();
        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isTrue();
    }

    @Test
    @Order(22)
    void subQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.eq(
                JPAExpressions
                    .select(memberSub.age.max())
                    .from(memberSub)
            )).fetch();

        assertThat(result).extracting("age")
            .containsExactly(40);
    }

    @Test
    @Order(23)
    void subQueryWithGoe() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.goe(
                JPAExpressions
                    .select(memberSub.age.avg())
                    .from(memberSub)
            ))
            .orderBy(member.age.asc())
            .fetch();

        assertThat(result).extracting("age")
            .containsExactly(30, 40);
    }

    @Test
    @Order(24)
    void subQueryWithIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.in(
                JPAExpressions
                    .select(memberSub.age)
                    .from(memberSub)
                    .where(memberSub.age.gt(10))
            ))
            .orderBy(member.age.asc())
            .fetch();

        assertThat(result).extracting(Member::getAge)
            .containsExactly(20, 30, 40);
    }

    @Test
    @Order(25)
    void subQueryInSelect() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
            .select(member.name, JPAExpressions.select(memberSub.age.avg()).from(memberSub))
            .from(member)
            .fetch();

        result.forEach(tuple -> {
            System.out.println("tuple = " + tuple);
            System.out.println("tuple.get(member.name) = " + tuple.get(member.name));
            System.out.println("tuple.get(0, String.class) = " + tuple.get(0, String.class));
            System.out.println("tuple.get(1, Double.class) = " + tuple.get(1, Double.class));
        });

        assertThat(result).hasSize(4)
            .extracting(tuple -> tuple.get(1, Double.class))
            .containsOnly(25.0);
    }

    @Test
    @Order(26)
    void subQueryWithStaticImport() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.eq(
                select(memberSub.age.max())
                    .from(memberSub)
            )).fetch();

        assertThat(result)
            .extracting("age")
            .containsExactly(40);
    }

    @Test
    @Order(27)
    void caseQuery() {
        List<String> result = queryFactory
            .select(member.age
                .when(10).then("열살")
                .when(20).then("스무살")
                .otherwise("기타"))
            .from(member)
            .orderBy(member.age.asc())
            .fetch();

        assertThat(result)
            .hasSize(4)
            .containsExactly("열살", "스무살", "기타", "기타");

        result.forEach(System.out::println);
    }

    @Test
    @Order(28)
    void caseQueryWithCaseBuilder() {
        List<String> result = queryFactory
            .select(new CaseBuilder()
                .when(member.age.between(0, 20)).then("0-20")
                .when(member.age.between(21, 30)).then("21-30")
                .otherwise("기타"))
            .from(member)
            .orderBy(member.age.asc())
            .fetch();

        assertThat(result)
            .hasSize(4)
            .containsExactly("0-20", "0-20", "21-30", "기타");

        result.forEach(System.out::println);
    }

    @Test
    @Order(29)
    void caseQueryInSelect() {
        NumberExpression<Integer> rankPath = new CaseBuilder()
            .when(member.age.between(0, 20)).then(2)
            .when(member.age.between(21, 30)).then(1)
            .otherwise(3);

        List<Tuple> result = queryFactory.select(member.name, member.age, rankPath)
            .from(member)
            .orderBy(rankPath.desc())
            .fetch();

        assertThat(result).hasSize(4)
            .extracting(tuple -> tuple.get(rankPath))
            .containsExactly(3, 2, 2, 1);

        result.forEach(tuple -> {
            System.out.println("member.name = " + tuple.get(member.name));
            System.out.println("member.age = " + tuple.get(member.age));
            System.out.println("rankPath = " + tuple.get(rankPath));
        });
    }

    @Test
    @Order(30)
    void constant() {
        Expression<String> constant = Expressions.constant("A");

        Tuple result = queryFactory
            .select(member.name, constant)
            .from(member)
            .fetchFirst();

        // 실제로 쿼리 수행할때는 DB쪽에 상수를 넘기지 않는다.

        assertThat(result.get(constant)).isEqualTo("A");
    }

    @Test
    @Order(31)
    void constantConcat() {
        List<String> result = queryFactory
            .select(member.name.concat("_").concat(member.age.stringValue()))
            .from(member)
            .fetch();

        assertThat(result).hasSize(4);

        result.forEach(System.out::println);
    }
}
