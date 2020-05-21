package com.frelamape.task2.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Neo4jAdapter {
    // TODO

    private static final Logger logger = LoggerFactory.getLogger(Neo4jAdapter.class);

    @Value("${com.frelamape.task2.db.Neo4jAdapter.connectionURI}")
    private String connectionURI;

    @Autowired
    private Neo4jTaskExecutor executor;

    @Autowired
    private ApplicationArguments args;

    @PostConstruct
    public void init() {
        if (args.getSourceArgs().length >= 2) {
            connectionURI = args.getSourceArgs()[3];
        }

        logger.info("Connecting to Mongo at " + connectionURI);

        // TODO
    }
  }
