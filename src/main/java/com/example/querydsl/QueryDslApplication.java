package com.example.querydsl;

import com.example.querydsl.domain.Member;
import com.example.querydsl.domain.Team;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class QueryDslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryDslApplication.class, args);
	}
	@Profile(value = {"local", "default"})
	@Component
	@RequiredArgsConstructor
	static class InitMember {
		private final InitMemberService initMemberService;

		@PostConstruct
		public void init() {
			initMemberService.init();
		}
	}

	@Component
	@RequiredArgsConstructor
	static class InitMemberService {
		@PersistenceContext
		private final EntityManager em;

		@Transactional
		public void init() {
			Team teamA = new Team("teamA");
			Team teamB = new Team("teamB");
			em.persist(teamA);
			em.persist(teamB);

			for (int i = 0; i < 100; i++) {
				Team seleectedTeam = i % 2 == 0 ? teamA : teamB;
				em.persist(new Member("member" + i, i + 1, seleectedTeam));
			}
		}
	}
}

