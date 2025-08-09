package com.example.demo.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EntityInteractionStrategyFactory {

    private final List<EntityInteractionStrategy> strategies;

    public EntityInteractionStrategy getStrategy(String entityType) {
        Map<String, EntityInteractionStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getEntityType().toLowerCase(),
                        Function.identity()
                ));

        EntityInteractionStrategy strategy = strategyMap.get(entityType.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }

        return strategy;
    }
}
