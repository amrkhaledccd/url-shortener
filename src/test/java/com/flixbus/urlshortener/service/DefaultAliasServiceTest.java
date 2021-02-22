package com.flixbus.urlshortener.service;

import com.flixbus.urlshortener.UrlShortenerApplication;
import com.flixbus.urlshortener.model.Alias;
import com.flixbus.urlshortener.repository.AliasRepository;
import com.flixbus.urlshortener.service.impl.DefaultAliasService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = UrlShortenerApplication.class)
public class DefaultAliasServiceTest {

    @Mock private AliasRepository aliasRepository;
    @InjectMocks private DefaultAliasService defaultAliasService;

    @Test
    public void getKey_whenValid_returnKey() {
        var alias = Alias.builder().key("123").build();
        when(aliasRepository.findFirstByUsed(Boolean.FALSE)).thenReturn(alias);
        var actual = defaultAliasService.getKey();

        assertEquals(alias.getKey(), actual);
    }

    @Test
    public void getKey_whenConcurrencyException_tryToGetAnotherKey() {
        var alias1 = Alias.builder().key("123").build();
        var alias2 = Alias.builder().key("456").build();

        when(aliasRepository.findFirstByUsed(Boolean.FALSE)).thenReturn(alias1).thenReturn(alias2);
        when(aliasRepository.save(alias1)).thenThrow(OptimisticLockingFailureException.class);
        var actual = defaultAliasService.getKey();

        assertEquals(alias2.getKey(), actual);
        verify(aliasRepository, times(2)).findFirstByUsed(Boolean.FALSE);
        verify(aliasRepository, times(1)).save(alias1);
        verify(aliasRepository, times(1)).save(alias2);
    }
}
