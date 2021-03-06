package org.example.inflearnthejavatest.study;

import lombok.extern.slf4j.Slf4j;
import org.example.inflearnthejavatest.domain.Member;
import org.example.inflearnthejavatest.domain.Study;
import org.example.inflearnthejavatest.domain.StudyStatus;
import org.example.inflearnthejavatest.member.MemberService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@ContextConfiguration(initializers = StudyServiceTest.ContainerPropertyInitializer.class)
class StudyServiceTest {

    @Mock
    MemberService memberService;
    @Autowired
    StudyRepository studyRepository;

    @Autowired
    Environment environment;

    @Value("${container.port}")
    int port;

    @Container
//    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres")
//            .withDatabaseName("studytest");
//    static GenericContainer postgreSQLContainer = new GenericContainer("postgres")
//            .withExposedPorts(5432)
//            .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
//            .withEnv("POSTGRES_DB", "studytest");
    static DockerComposeContainer composeContainer =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                    .withExposedService("study-db", 5432);

    @BeforeAll
    static void beforeAll() {
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
//        postgreSQLContainer.followOutput(logConsumer);
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("=======================");
//        System.out.println(postgreSQLContainer.getMappedPort(5432));
        System.out.println(environment.getProperty("container.port"));
        System.out.println("port using @Value: " + port);
        studyRepository.deleteAll();
    }

    // @Testcontainers??? ???????????? ???????????? ???????????? ??????/????????? ?????? ??????.
//    @BeforeAll
//    static void beforeAll() {
//        postgreSQLContainer.start();
////        System.out.println(postgreSQLContainer.getJdbcUrl());
//    }
//
//    @AfterAll
//    static void afterAll() {
//        postgreSQLContainer.stop();;
//    }

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

        /* Void ????????? ?????? ??????????????? ????????? ????????? ?????? ????????? ?????? ?????? ??? ??????. */
        doThrow(new IllegalArgumentException()).when(memberService).validate(1L);

        assertThrows(IllegalArgumentException.class, () -> {
            memberService.validate(1L);
        });

        memberService.validate(2L);

        /* ???????????? ????????? ??????????????? ????????? ????????? ??? ?????? ????????? ??????????????? ????????? ?????? ??????. */
        when(memberService.findById(any()))
                .thenReturn(Optional.of(member))
                .thenThrow(new RuntimeException())
                .thenReturn(Optional.empty());

        // ????????? ??????
        Optional<Member> byId = memberService.findById(1L);
        assertEquals("kim", byId.get().getName());

        // ????????? ??????
        assertThrows(RuntimeException.class, () -> {
            memberService.findById(2L);
        });

        // ????????? ??????
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
        //when(studyRepository.save(study)).thenReturn(study); // ?????? DB ????????? ???????????? stub ????????? ?????????.

        //when
        studyService.createNewStudy(1L, study);

        //then

        assertNotNull(study.getOwnerId());
        assertEquals(1L, study.getOwnerId());

        verify(memberService, times(1)).notify(study);
        verifyNoMoreInteractions(memberService);
    }

    @DisplayName("?????? ???????????? ??? ??? ????????? ???????????? ????????????.")
    @Test
    void openStudy() {
        //Given
        StudyService studyService = new StudyService(memberService, studyRepository);
        Study study = new Study(10, "??? ?????? ?????????");
        assertNull(study.getOpenedDateTime());
        //when(studyRepository.save(study)).thenReturn(study);  //?????? DB ??????????????? stub ????????? ???????????????.

        //When
        studyService.openStudy(study);

        //Then
        assertEquals(StudyStatus.OPENED, study.getStatus());
        assertNotNull(study.getOpenedDateTime());
        verify(memberService, times(1)).notify(study);
    }

    static class ContainerPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
//            TestPropertyValues.of("container.port=" + postgreSQLContainer.getMappedPort(5432))
//                    .applyTo(context.getEnvironment());
            TestPropertyValues.of("container.port=" + composeContainer.getServicePort("study-db", 5432))
                    .applyTo(context.getEnvironment());
        }
    }
}