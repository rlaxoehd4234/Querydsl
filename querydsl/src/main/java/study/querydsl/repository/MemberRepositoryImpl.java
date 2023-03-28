package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberFormDto;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.QMemberFormDto;

import javax.persistence.EntityManager;
import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory  queryFactory;

    public MemberRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberFormDto> search(MemberSearchCondition condition){


        return queryFactory
                .select(new QMemberFormDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();

    }

    @Override
    public Page<MemberFormDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {

        List<MemberFormDto> content =
                         queryFactory
                        .select(new QMemberFormDto(
                                member.id,
                                member.username,
                                member.age,
                                team.id,
                                team.name))
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
                        .offset(pageable.getOffset()) //
                        .limit(pageable.getPageSize()) // 반환 사이즈
                        .fetch();


        return new PageImpl<>(content, pageable, content.size());
    }

    @Override
    public Page<MemberFormDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {

        List<MemberFormDto> content = queryFactory
                        .select(new QMemberFormDto(
                                member.id,
                                member.username,
                                member.age,
                                team.id,
                                team.name))
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
                        .offset(pageable.getOffset()) //
                        .limit(pageable.getPageSize()) // 반환 사이즈
                        .fetch();

        //fetch result 및 fetchCount 가 deprecated 가 되어서 사용이 불가능하다.

        return new PageImpl<>(content, pageable, content.size());
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? team.name.eq(username) : null;
    }
}
