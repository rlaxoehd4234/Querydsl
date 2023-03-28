package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberFormDto;
import study.querydsl.dto.MemberSearchCondition;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberFormDto> search(MemberSearchCondition condition);
    Page<MemberFormDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberFormDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);


}
