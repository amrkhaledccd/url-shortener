package com.flixbus.urlshortener.service.impl;

import com.flixbus.urlshortener.exception.ConstraintsViolationException;
import com.flixbus.urlshortener.exception.EntityExpiredException;
import com.flixbus.urlshortener.exception.EntityNotFoundException;
import com.flixbus.urlshortener.model.PagedResult;
import com.flixbus.urlshortener.model.TinyUrl;
import com.flixbus.urlshortener.repository.TinyUrlRepository;
import com.flixbus.urlshortener.service.AliasService;
import com.flixbus.urlshortener.service.TinyUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
public class DefaultTinyUrlService implements TinyUrlService {

    @Autowired private TinyUrlRepository tinyUrlRepository;
    @Autowired private AliasService aliasService;

    @Override
    public TinyUrl create(String url) {
        var tinyUrl =
                TinyUrl.builder()
                        .originalUrl(url)
                        .alias(aliasService.getKey())
                        .expirationDate(LocalDateTime.now().plusDays(7))
                        .build();

        try{
            tinyUrl = tinyUrlRepository.save(tinyUrl);
        } catch(DataIntegrityViolationException e) {
            log.warn("ConstraintsViolationException while creating tinyUrl: {}", url, e);
            throw new ConstraintsViolationException(e.getMessage());
        }

        return tinyUrl;
    }

    @Override
    public String getOriginalUrl(String alias) {
        return tinyUrlRepository
                .findByAlias(alias)
                .map(tinyUrl -> {
                    if(tinyUrl.getExpirationDate().isBefore(LocalDateTime.now())) {
                        throw new EntityExpiredException(String.format("Url with alias %s is expired", alias));
                    }
                    return tinyUrl.getOriginalUrl();
                })
                .orElseThrow(() -> new EntityNotFoundException("Could not find entity with alias: " + alias));
    }

    @Override
    public TinyUrl getUrl(String alias) {
        return tinyUrlRepository
                .findByAlias(alias)
                .orElseThrow(() -> new EntityNotFoundException("Could not find entity with alias: " + alias));
    }

    @Override
    public PagedResult<TinyUrl> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return buildPagedResult(tinyUrlRepository.findAll(pageable));
    }

    @Override
    @Async
    public void incrementRedirectionCount(String alias) {
        try{
            tinyUrlRepository
                    .findByAlias(alias)
                    .ifPresent(tinyUrl -> {
                        tinyUrl.setRedirectionCount(tinyUrl.getRedirectionCount() + 1);
                        tinyUrlRepository.save(tinyUrl);
                    });
        } catch (OptimisticLockingFailureException e) {
            log.warn("OptimisticLockingFailureException while incrementing redirectionCount for {}", alias);
            incrementRedirectionCount(alias);
        }
    }

    private PagedResult<TinyUrl> buildPagedResult(Page<TinyUrl> page){
        return PagedResult
                .<TinyUrl>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getPageable().getPageNumber())
                .size(page.getSize())
                .last(page.isLast())
                .build();
    }
}
