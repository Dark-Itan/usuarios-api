package com.ejemplo.usuarios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class StartupLogger {

    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment env;

    public StartupLogger(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws UnknownHostException {
        String puerto = env.getProperty("server.port", "8080");
        String host = InetAddress.getLocalHost().getHostAddress();

        logger.info("Application started");
        logger.info("Responding at http://{}:{}", host, puerto);
    }
}