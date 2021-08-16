package org.example.inflearnthejavatest.study;

import org.example.inflearnthejavatest.domain.Member;
import org.example.inflearnthejavatest.domain.Study;
import org.example.inflearnthejavatest.member.MemberService;

import java.util.Optional;

public class StudyService {

    private final MemberService memberService;
    private final StudyRepository studyRepository;

    public StudyService(MemberService memberService, StudyRepository studyRepository) {
        assert memberService != null;
        assert  studyRepository != null;
        this.memberService = memberService;
        this.studyRepository = studyRepository;
    }

    public Study createNewStudy(Long memberid, Study study) {
        Optional<Member> member = memberService.findById(memberid);

        if (member.isPresent()) {
            study.setOwnerId(memberid);
        } else {
            throw new IllegalArgumentException("Member doesn't exist for id: '" + memberid + "'");
        }

        Study newStudy = studyRepository.save(study);
        memberService.notify(newStudy);

        return newStudy;
    }

    public Study openStudy(Study study) {
        study.open();
        Study openedStudy = studyRepository.save(study);
        memberService.notify(openedStudy);

        return openedStudy;
    }
}
