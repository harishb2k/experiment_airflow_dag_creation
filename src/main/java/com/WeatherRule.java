package com;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

import java.util.Map;

@Rule(name = "weather rule", description = "if it rains then take an umbrella")
public class WeatherRule {

    @Condition
    public boolean itRains(@Fact("rain") boolean rain) {
        return rain;
    }

    @Action
    public void takeAnUmbrella(@Fact("out") Map<String, Object> map) {
        map.put("data", "It rains, take an umbrella!");
    }
}
