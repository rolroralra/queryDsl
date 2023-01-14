package com.example.querydsl;

import static com.example.querydsl.domain.QMember.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.querydsl.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class QueryDslApplicationTests {
	@PersistenceContext
	private EntityManager em;

	@Test
	void contextLoads() {
		Member m1 = new Member("test");
		em.persist(m1);

		JPAQueryFactory query = new JPAQueryFactory(em);
		Member result = query.selectFrom(member)
			.fetchOne();

		assertThat(result).isNotNull()
			.hasFieldOrPropertyWithValue("id", m1.getId())
			.hasFieldOrPropertyWithValue("name", "test");

	}

}
