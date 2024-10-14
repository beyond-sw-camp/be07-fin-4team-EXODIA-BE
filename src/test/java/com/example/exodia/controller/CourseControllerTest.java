//package com.example.exodia.controller;
//
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//
//import com.example.exodia.course.controller.CourseController;
//import com.example.exodia.registration.service.RegistrationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//@WebMvcTest(CourseController.class)
//class CourseControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private RegistrationService registrationService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testRegisterParticipant() throws Exception {
//        // Mock the registrationService to return success message
//        when(registrationService.registerParticipant(anyLong(), anyLong())).thenReturn("등록 완료");
//
//        // Perform MockMvc POST request
//        mockMvc.perform(post("/course/register/1")
//                        .param("userId", "123"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("등록 완료"));
//    }
//
//    @Test
//    void testRegisterParticipantExceedsMax() throws Exception {
//        // Mock the registrationService to return error message
//        when(registrationService.registerParticipant(anyLong(), anyLong())).thenReturn("참가자 수가 초과되었습니다.");
//
//        // Perform MockMvc POST request
//        mockMvc.perform(post("/course/register/1")
//                        .param("userId", "123"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("참가자 수가 초과되었습니다."));
//    }
//}
