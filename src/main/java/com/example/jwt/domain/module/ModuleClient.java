package com.example.jwt.domain.module;

import java.util.UUID;

public interface ModuleClient {
    boolean isModuleAvailable(UUID moduleId);
}
