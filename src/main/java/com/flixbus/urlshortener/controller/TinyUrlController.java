package com.flixbus.urlshortener.controller;

import com.flixbus.urlshortener.model.TinyUrl;
import com.flixbus.urlshortener.service.TinyUrlService;
import com.flixbus.urlshortener.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class TinyUrlController {

    @Autowired private TinyUrlService tinyUrlService;
    @Autowired Environment environment;

    @PostMapping("v1/urls")
    @ResponseStatus(HttpStatus.CREATED)
    public String createTinyUrl(@RequestBody String originalUrl) throws UnknownHostException {
        var created = tinyUrlService.create(originalUrl);
        var host = InetAddress.getLoopbackAddress().getHostName();
        var port = environment.getProperty("server.port");
        return String.format("http://%s:%s/%s", host, port, created.getAlias());
    }

    @GetMapping("v1/urls")
    public ResponseEntity<?> findAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(tinyUrlService.findAll(page, size));
    }

    @GetMapping("v1/urls/{alias}")
    public TinyUrl findOriginalUrl(@PathVariable String alias) {
        return  tinyUrlService.getUrl(alias);
    }

    @GetMapping("/{alias}")
    public void redirect(@PathVariable String alias, HttpServletResponse httpServletResponse) {
        var originalUrl = tinyUrlService.getOriginalUrl(alias);
        httpServletResponse.setHeader("Location", originalUrl);
        httpServletResponse.setStatus(302);
        tinyUrlService.incrementRedirectionCount(alias);
    }
}
