package com.flixbus.urlshortener.scheduler;

import com.flixbus.urlshortener.model.Alias;
import com.flixbus.urlshortener.repository.AliasRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
@Slf4j
public class KeyGenerationScheduler {

    private final String SEED = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Long MIN_COUNT = 500l;

    @Autowired private AliasRepository aliasRepository;

    @Scheduled(fixedDelay = 100)
    @SchedulerLock(name = "KeyGenerationScheduler")
    public void generateKeys() {
        var count = aliasRepository.countByUsed(Boolean.FALSE);

        if(count >= MIN_COUNT) {
            log.debug("There are {} not used aliases", count);
            return;
        }

        var alias =
                Alias.builder()
                        .key(generate())
                        .used(Boolean.FALSE)
                        .build();

        aliasRepository.save(alias);
    }

    public String generate() {
        String key = "";

        do {
            key = random(8);
        } while (aliasRepository.existsByKey(key));

        return key;
    }

    private String random(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i ++) {
            int randNum = rand.nextInt(62);
            sb.append(SEED.charAt(randNum));
        }
        return sb.toString();
    }
}
