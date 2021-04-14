package com;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Rule(name = "increment op")
public class IncrementOp {

    @Condition
    public boolean itRains(@Fact("attribute_definitions") List<AttributeDef> allAttributes,
                           @Fact("event") Map<String, Object> event) {

        String eventName = (String) event.get("name");
        AtomicBoolean found = new AtomicBoolean(false);
        allAttributes.forEach(attributeDef -> {
            attributeDef.getEvents().stream().filter(s -> Objects.equals(s, eventName)).findAny().ifPresent(s -> {
                if (Objects.equals(attributeDef.getOp(), "inc")) {
                    found.set(true);
                }
            });
        });
        return found.get();
    }

    @Action
    public void takeAnUmbrella(@Fact("out") Map<String, Object> map,
                               @Fact("user_attributes") Map<String, UserAttribute> userAttributes,
                               @Fact("attribute_definitions") List<AttributeDef> allAttributes,
                               @Fact("event") Map<String, Object> event
    ) {
        String eventName = (String) event.get("name");
        allAttributes.forEach(attributeDef -> {
            attributeDef.getEvents().forEach(_eventName -> {
                if (!Objects.equals(_eventName, eventName)) return;

                UserAttribute userAttribute = userAttributes.get(attributeDef.getName());
                Number value = (Number) userAttribute.getValue();
                value = value.doubleValue() + 1;
                userAttribute.setValue(value);
            });
        });
    }
}
