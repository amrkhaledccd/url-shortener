package com.flixbus.urlshortener.service;

import com.flixbus.urlshortener.model.PagedResult;
import com.flixbus.urlshortener.model.TinyUrl;

public interface TinyUrlService {
    TinyUrl create(String url);
    String getOriginalUrl(String alias);
    TinyUrl getUrl(String alias);
    PagedResult<TinyUrl> findAll(int page, int size);
    void incrementRedirectionCount(String alias);
}
