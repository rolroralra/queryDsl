package com.example.querydsl.repository;

import com.example.querydsl.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom,
    QuerydslPredicateExecutor<Member> {
    List<Member> findByName(String name);
}
