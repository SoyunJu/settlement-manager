package com.settlement.manager.infrastructure.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT_LOGGER");

    public void log(String action, Long userId, String detail) {
        AUDIT.info("action={} userId={} detail={}", action, userId, detail);
    }
}