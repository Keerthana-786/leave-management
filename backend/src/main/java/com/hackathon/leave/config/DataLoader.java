package com.hackathon.leave.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Override
    public void run(String... args) {
        log.info("Contract Phase: Application booted successfully on H2 in-memory DB.");
    }
}
