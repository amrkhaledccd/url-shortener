package com.flixbus.urlshortener.service.impl;

import com.flixbus.urlshortener.repository.AliasRepository;
import com.flixbus.urlshortener.service.AliasService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultAliasService implements AliasService {

    @Autowired private AliasRepository aliasRepository;

    @Override
    public String getKey() {
        try{
            var alias = aliasRepository.findFirstByUsed(Boolean.FALSE);
            alias.setUsed(Boolean.TRUE);
            aliasRepository.save(alias);
            return alias.getKey();
        }catch (OptimisticLockingFailureException e) {
            log.warn("OptimisticLockingFailureException while fetching a key");
            return getKey();
        }
    }
}
