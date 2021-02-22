package com.flixbus.urlshortener.controller;

import com.flixbus.urlshortener.UrlShortenerApplication;
import com.flixbus.urlshortener.exception.ConstraintsViolationException;
import com.flixbus.urlshortener.exception.EntityNotFoundException;
import com.flixbus.urlshortener.model.TinyUrl;
import com.flixbus.urlshortener.service.TinyUrlService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.assertEquals;


/*
   Due to time constraint I skipped writing tests for findAll and findOriginalUrl
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UrlShortenerApplication.class)
public class TinyUrlControllerTest {

    @Mock private TinyUrlService tinyUrlService;
    @Mock private Environment environment;
    @InjectMocks private TinyUrlController tinyUrlController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(tinyUrlController)
                .build();
    }

    @Test
    public void createTinyUrl_whenValid_returnCreated() throws Exception {
        var originalUrl = "http://test.com/";
        var tinyUrl = mockTinyUrl(originalUrl, LocalDateTime.now().plusDays(2));
        when(tinyUrlService.create(originalUrl)).thenReturn(tinyUrl);
        when(environment.getProperty(anyString())).thenReturn("8080");
        var expected = "http://localhost:8080/123";

        var actual = mockMvc.perform(post("/v1/urls").content(originalUrl))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(expected, actual.getResponse().getContentAsString());
    }

    @Test
    public void createTinyUrl_whenBodyInvalid_returnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/urls"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTinyUrl_whenConstraintsViolationException_returnBadRequest() throws Exception {
        doThrow(ConstraintsViolationException.class).when(tinyUrlService).create(anyString());
        mockMvc.perform(post("/v1/urls").content("http://test.com/"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void redirect_whenAliasExists_thenRedirect() throws Exception{
        var alias = "123";
        var originalUrl = "http://test.com/";
        when(tinyUrlService.getOriginalUrl(alias)).thenReturn(originalUrl);
        var result = mockMvc.perform(get("/" + alias))
                .andExpect(status().is3xxRedirection()).andReturn();

        var expected = result.getResponse().getHeader("Location");
        assertEquals(expected, originalUrl);
    }

    @Test
    public void redirect_whenAliasNotExists_thenReturnNotFound() throws Exception{
        var alias = "123";
        doThrow(EntityNotFoundException.class).when(tinyUrlService).getOriginalUrl(alias);
        mockMvc.perform(get("/" + alias))
                .andExpect(status().isNotFound());
    }

    private TinyUrl mockTinyUrl(String url, LocalDateTime expDate) {
        return TinyUrl
                .builder()
                .id("url_id_123")
                .originalUrl(url)
                .alias("123")
                .expirationDate(expDate)
                .build();
    }
}
