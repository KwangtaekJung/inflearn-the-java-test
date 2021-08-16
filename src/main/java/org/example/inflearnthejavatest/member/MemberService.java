package org.example.inflearnthejavatest.member;

import org.example.inflearnthejavatest.domain.Member;
import org.example.inflearnthejavatest.domain.Study;

import java.util.Optional;

public interface MemberService {

    Optional<Member> findById(Long memberId);

    void validate(Long memberId);

    void notify(Study newStudy);

    void notify(Member member);
}
