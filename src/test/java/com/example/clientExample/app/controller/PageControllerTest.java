package com.example.clientExample.app.controller;

import com.example.clientExample.app.entities.ExampleResponse;
import com.example.clientExample.app.service.ExampleService;
import com.example.clientExample.shared.TestSecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PageControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExampleService exampleService;



    @Test
    void shouldRenderHomePageWithModel() throws Exception {

        when(exampleService.fetchExampleData()).thenReturn(new ExampleResponse(1, "Pete"));
        mockMvc.perform(get("/view"))
                .andExpect(status().isOk())
                .andExpect(view().name("example"))
                .andExpect(model().attributeExists("examples"))
                .andExpect(model().attribute("examples", Matchers.contains(new ExampleResponse(1, "Pete"))));

    }
}