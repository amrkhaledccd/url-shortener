package com.flixbus.urlshortener.service;

import com.flixbus.urlshortener.UrlShortenerApplication;
import com.flixbus.urlshortener.exception.ConstraintsViolationException;
import com.flixbus.urlshortener.exception.EntityExpiredException;
import com.flixbus.urlshortener.exception.EntityNotFoundException;
import com.flixbus.urlshortener.model.TinyUrl;
import com.flixbus.urlshortener.repository.TinyUrlRepository;
import com.flixbus.urlshortener.service.impl.DefaultTinyUrlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = UrlShortenerApplication.class)
public class DefaultTinyUrlServiceTest {

    @Mock private TinyUrlRepository tinyUrlRepository;
    @Mock private AliasService aliasService;
    @InjectMocks private DefaultTinyUrlService defaultTinyUrlService;

    @Test
    public void create_whenValid_returnCreatedTinyUrl() {
       var originalUrl = "http://test.com/";
       var expected = mockTinyUrl(originalUrl, LocalDateTime.now().plusDays(2));
       when(aliasService.getKey()).thenReturn("123");
       when(tinyUrlRepository.save(any())).thenReturn(expected);

       var actual = defaultTinyUrlService.create(originalUrl);
       assertEquals(expected, actual);
    }

    @Test(expected = ConstraintsViolationException.class)
    public void create_whenInvalid_ThrowConstraintsViolationException() {
       doThrow(DataIntegrityViolationException.class)
               .when(tinyUrlRepository).save(any());
        when(aliasService.getKey()).thenReturn("123");
        defaultTinyUrlService.create("http://test.com");
    }

    @Test
    public void getOriginalUrl_whenExists_returnOriginalUrl() {
        var expected = "http://test.com/";
        var tinyUrl = mockTinyUrl(expected, LocalDateTime.now().plusDays(2));
        when(tinyUrlRepository.findByAlias("123")).thenReturn(Optional.of(tinyUrl));

        var actual = defaultTinyUrlService.getOriginalUrl("123");
        assertEquals(expected, actual);
    }

    @Test (expected = EntityNotFoundException.class)
    public void getOriginalUrl_whenNotExists_ThrowEntityNotFoundException() {
        when(tinyUrlRepository.findByAlias("123")).thenReturn(Optional.empty());
        defaultTinyUrlService.getOriginalUrl("123");
    }

    @Test (expected = EntityExpiredException.class)
    public void getOriginalUrl_whenExpired_ThrowEntityExpiredException() {
        var tinyUrl = mockTinyUrl("http://test.com", LocalDateTime.now().minusDays(2));
        when(tinyUrlRepository.findByAlias("123")).thenReturn(Optional.of(tinyUrl));
        defaultTinyUrlService.getOriginalUrl("123");
    }

    // FindAll tests is straight forward, just make sure it returns what is mocked
    // due to time constraint I will skip this.

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
