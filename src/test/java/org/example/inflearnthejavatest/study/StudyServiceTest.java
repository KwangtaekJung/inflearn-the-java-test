package org.example.inflearnthejavatest.study;

import org.example.inflearnthejavatest.domain.Member;
import org.example.inflearnthejavatest.domain.Study;
import org.example.inflearnthejavatest.domain.StudyStatus;
import org.example.inflearnthejavatest.member.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock MemberService memberService;
    @Mock StudyRepository studyRepository;

    @Test
    @DisplayName("Mock Stubbing Test")
    void mockStubbingTest() {

        StudyService studyService = new StudyService(memberService, studyRepository);
        assertNotNull(studyService);

        Member member = new Member();
        member.setId(1L);
        member.setName("kim");

        when(memberService.findById(any())).thenReturn(Optional.of(member));

        assertEquals("kim", memberService.findById(1L).get().getName());
        assertEquals("kim", memberService.findById(2L).get().getName());

        /* Void 메소드 특정 매개변수를 받거나 호출된 경우 예오를 발생 시킬 수 있다. */
        doThrow(new IllegalArgumentException()).when(memberService).validate(1L);

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.validate(1L);
        });

        memberService.validate(2L);

        /* 메소드가 동일한 매개변수로 여러번 호출될 때 각기 다르게 행동하도록 조작할 수도 있다. */
        when(memberService.findById(any()))
                .thenReturn(Optional.of(member))
                .thenThrow(new RuntimeException())
                .thenReturn(Optional.empty());

        // 첫번째 호출
        Optional<Member> byId = memberService.findById(1L);
        assertEquals("kim", byId.get().getName());

        // 두번재 호출
        assertThrows(RuntimeException.class, ()-> {
            memberService.findById(2L);
        });

        // 세번째 호출
        assertEquals(Optional.empty(), memberService.findById(3L));
    }

    @Test
    void createNewStudy() {
        //given
        StudyService studyService = new StudyService(memberService, studyRepository);
        assertNotNull(studyService);

        Member member = new Member();
        member.setId(1L);
        member.setName("jung");

        Study study = new Study(10, "TestStudy");

        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        when(studyRepository.save(study)).thenReturn(study);

        //when
        studyService.createNewStudy(1L, study);

        //then

        assertNotNull(study.getOwnerId());
        assertEquals(1L, study.getOwnerId());

        verify(memberService, times(1)).notify(study);
        verifyNoMoreInteractions(memberService);
    }

    @DisplayName("다른 사용자가 볼 수 있도록 스터디를 공개한다.")
    @Test
    void openStudy() {
        //Given
        StudyService studyService = new StudyService(memberService, studyRepository);
        Study study = new Study(10, "더 자바 테스트");
        assertNull(study.getOpenedDateTime());
        when(studyRepository.save(study)).thenReturn(study);

        //When
        studyService.openStudy(study);

        //Then
        assertEquals(StudyStatus.OPENED, study.getStatus());
        assertNotNull(study.getOpenedDateTime());
        verify(memberService, times(1)).notify(study);
    }
}