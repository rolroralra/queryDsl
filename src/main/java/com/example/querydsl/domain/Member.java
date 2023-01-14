package com.example.querydsl.domain;

import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "MEMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(value = AccessLevel.PROTECTED)
@ToString(of = {"id", "name", "age"})
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    public Member(String name) {
        this(name, 0);
    }

    public Member(String name, Integer age) {
        this(name, age, null);
    }

    public Member(String name, Integer age, Team team) {
        this.name = name;
        this.age = age;

        if (team != null) {
            changeTeam(team);
        }
    }

    public void changeTeam(Team team) {
        if (this.team == team) {
            return;
        }

        if (this.team != null) {
            this.team.removeMember(this);
        }

        this.team = team;
        team.addMember(this);
    }

    public String getTeamName() {
        return Optional.ofNullable(team).map(Team::getName).orElseThrow(IllegalStateException::new);
    }
}
