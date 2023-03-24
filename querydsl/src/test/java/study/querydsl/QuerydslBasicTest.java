package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;
    @BeforeEach
    public void before(){
        jpaQueryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        Member findMember = em.createQuery("select m from Member m" +
                        " where m.username = :username",Member.class)
                .setParameter("username","member1")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");

    }



    @Test
    public void startQuerydsl(){
        Member findmember = jpaQueryFactory.select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();


        Assertions.assertThat(findmember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQuerydsl2(){

        Member findMember = jpaQueryFactory.selectFrom(member).
                where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchQuerydsl(){

        Member findMember = jpaQueryFactory.selectFrom(member).
                where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                ).fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    @Test
    public void searchBetweenTest(){

        List<Member> findMembers=
                jpaQueryFactory.
                        selectFrom(member).
                        where(member.age.between(10,30))
                        .fetch();

        assertThat(findMembers.size()).isEqualTo(3);
    }

    @Test
    public void searchGTTest(){
        List<Member> findMembers = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.gt(30)).fetch();

        assertThat(findMembers.size()).isEqualTo(1);
    }

    @Test
    public void QuerydslPagingTest(){
        QueryResults<Member> findMembers = jpaQueryFactory.
                selectFrom(member).
                fetchResults();


        long count = jpaQueryFactory.selectFrom(member).fetchCount();

        System.out.println(count);
    }

    /* 회원 정렬 순서
    1. 회원 나이 내림차순
    2. 회원 이름 오름 차순
*/
    @Test
    public void sort(){
        em.persist(new Member(null, 100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(),member.username.asc().nullsLast())
                .fetch();
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member5.getUsername()).isEqualTo("member6");
        assertThat(member5.getUsername()).isNull();

    }



    @Test
    public void paging1(){
        List<Member> result =
                jpaQueryFactory.selectFrom(member).orderBy(member.username.desc()).offset(0).limit(2).fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void aggregation(){
        List<Tuple> result = jpaQueryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
    }
    @Test
    public void group() throws Exception {
        //given
        List<Tuple> result = jpaQueryFactory
                .select(team, member.age.avg())
                .from(member)
                .join(member.team ,team)
                .groupBy(team.name)
                .fetch();

        //when

        //then
     }

     @Test
     public void join() throws Exception {
         //given
         List<Member> result = jpaQueryFactory
                 .selectFrom(member)
                 .join(member.team , team)
                 .where(team.name.eq("teamA"))
                 .fetch();

         assertThat(result)
                 .extracting("username")
                 .containsExactly("member1","member2");
         //when

         //then
      }


      @Test
      public void theta_join() throws Exception {
          //given
          em.persist(new Member("teamA"));
          em.persist(new Member("teamB"));


          List<Member> result = jpaQueryFactory
                  .select(member)
                  .from(member, team)
                  .where(member.username.eq(team.name))
                  .fetch();


          assertThat(result)
                  .extracting("username")
                  .containsExactly("teamA","teamB");
          //when

          //then
       }

       @Test
       public void join_on_filtering() throws Exception {
           //given

           List<Tuple> result = jpaQueryFactory
                   .select(member, team)
                   .from(member)
                   .leftJoin(member.team, team).on(team.name.eq("teamA"))
                   .fetch();

           for(Tuple tuple : result){
               System.out.println("tuple = " + tuple);
           }
           //when

           //then
        }

        @Test
        public void join_on_no_relation() throws Exception {
            //given

            em.persist(new Member("teamA"));
            em.persist(new Member("teamB"));
            em.persist(new Member("teamC"));

            List<Tuple> result = jpaQueryFactory
                    .select(member ,team)
                    .from(member)
                    .leftJoin(team).on(member.username.eq(team.name))
                    .where(member.username.eq(team.name))
                    .fetch();

            for(Tuple tuple : result) {
                System.out.println("Tuple =" + tuple);
            }
            //when

            //then
         }

         @Test
         public void fetchJoinNo() throws Exception {
             //given
             em.flush();
             em.clear();

             Member member1 = jpaQueryFactory
                     .selectFrom(member)
                     .join(member.team , team).fetchJoin()
                     .where(member.username.eq("member1")).fetchOne();
             //when

             //then
          }

          // 나이가 가장 많은 회원을 조회
          @Test
          public void subQuery() throws Exception {
                QMember membersub = new QMember("memberSub")
        //given
              List<Member> result =jpaQueryFactory.selectFrom(member)
                      .where(member.age.eq(
                              JPAExpressions.select(membersub.age.max())
                                      .from(membersub)
                      ))
                      .fetch();

                assertThat(result).extracting("age")
                        .containsExactly(40);

              //when

              //then
           }

           @Test
           public void subQueryGoe() throws Exception {
               //given
               QMember memberSub = new QMember("memberSub");

               List<Member> result = jpaQueryFactory
                       .selectFrom(member)
                       .where(member.age.goe(
                               JPAExpressions
                                       .select(memberSub.age.avg())
                                       .from(memberSub)
                       ))
                       .fetch();

               //when

               //then
            }


    @Test
    public void basicCase() throws Exception {
        //given
        
        
        //when
        
        //then
     }
        



}
