package com;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Jeasy {


    public static Map<String, UserAttribute> buildUserAttributes() {
        UserAttribute user_ride_attempt = UserAttribute.builder()
                .name("user_ride_attempt")
                .value(0)
                .build();

        UserAttribute user_total_bill_paid = UserAttribute.builder()
                .name("user_total_bill_paid")
                .value(0)
                .build();

        Map<String, UserAttribute> attributeMap = new HashMap<>();
        attributeMap.put("user_ride_attempt", user_ride_attempt);
        attributeMap.put("user_total_bill_paid", user_total_bill_paid);
        return attributeMap;
    }

    public static List<AttributeDef> buildAttributeDef() {
        AttributeDef attributeDef = AttributeDef.builder()
                .name("user_ride_attempt")
                .events(Arrays.asList("user_ride_complete_kafka_event", "user_ride_cancelled_kafka_event"))
                .op("inc")
                .build();
        return Arrays.asList(attributeDef);
    }

    static AtomicInteger at = new AtomicInteger();

    private static class Context {
        public static final ThreadLocal<ScriptEngine> context = new ThreadLocal<>();

        public static ScriptEngine context() {
            if (context.get() == null) {
                System.out.println(at.getAndIncrement());
                context.set(mgr.getEngineByName("luaj"));
            }
            return context.get();
        }
    }

    public static ScriptEngineManager mgr = new ScriptEngineManager();
    public static ScriptEngine e = mgr.getEngineByName("luaj");


    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    foo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        Thread.sleep(100000);
    }

    public static void foo() throws Exception {

        for (int i = 0; i < 100_000; i++) {
            ScriptEngine e = Context.context();
            try {
                e.put("value", i);
                Object o = e.eval(
                        "out = value" +
                                "print(value)"+
                                ""
                );
                int out = (int) e.get("out");
                if (out != i) {
                    System.out.println("Got error - i=" + i + " out=" + out);
                    System.exit(0);
                }
                if (out % 100_000 == 0) {
                    // System.out.println("Good");
                }
            } catch (ScriptException scriptException) {
                scriptException.printStackTrace();
            }
        }

    }

    public static void _main(String[] args) throws ScriptException {

        if (true) {

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine e = mgr.getEngineByName("luaj");

            long start = System.currentTimeMillis();
            for (int i = 0; i < 100_000; i++) {

                // User current attributes which we pulled out form database
                Map<String, Object> data = new HashMap<>();
                data.put("value_1", 10);
                data.put("value_2", 20);
                data.put("value_3", 0);
                data.put("value_array", Arrays.asList(1, 2, 3));


                // Data we got from event (SQS)
                Map<String, Object> eventDataFromSQL = new HashMap<>();
                eventDataFromSQL.put("event_value_1", 11);
                eventDataFromSQL.put("event_value_2", 2.0);

                // Put all data to lua context
                data.keySet().forEach(key -> e.put(key, data.get(key)));
                eventDataFromSQL.keySet().forEach(key -> e.put(key, eventDataFromSQL.get(key)));

                // Execute your logic (Lua Code
                if (true) {
                    e.eval(
                            "require 'jse.hyperbolic'" +
                                    "value_1 = value_1 + event_value_1 " +
                                    "value_2 = value_2 / event_value_2 " +
                                    "value_3 = 180 " +
                                    "if value_1 > 2 then value_1 = 0 end " +
                                    "value_1 = hyperbolic.sinh(0.5) " +
                                    "value_1 = hyperbolic.extractintfromlist(value_array, 1) "
                            // , new LuajContext()
                    );
                } else {
                    Globals g = JsePlatform.standardGlobals();
                    g.load("require 'jse.hyperbolic' ;" +
                                    "value_1 = value_1 + event_value_1 ; "
                            /*"value_2 = value_2 / event_value_2 ; " +
                            "if value_1 > 2 then value_1 = 0 end ;" +
                            "value_1 = hyperbolic.sinh(0.5) ;" +
                            "value_1 = hyperbolic.extractintfromlist(value_array, 1) ;"*/
                    ).call();
                }

                // Update back the user attributes - this will again go back to DB
                data.keySet().forEach(key -> data.put(key, e.get(key)));
                System.out.println(data);

                System.exit(0);
            }
            long end = System.currentTimeMillis();
            System.out.println("Time=" + (end - start));

            return;
        }

        List<AttributeDef> allAttributes = buildAttributeDef();
        Map<String, UserAttribute> userAttributes = buildUserAttributes();

        // This will come from
        Map<String, Object> event = new HashMap<>();
        event.put("name", "user_ride_complete_kafka_event");

        // define facts
        Facts facts = new Facts();
        facts.put("attribute_definitions", allAttributes);
        facts.put("user_attributes", userAttributes);
        facts.put("event", event);

        Map<String, Object> out = new HashMap<>();
        facts.put("out", out);

        // define rules
        Rules rules = new Rules();
        rules.register(new IncrementOp());

        // fire rules on known facts
        RulesEngine rulesEngine = new DefaultRulesEngine();
        rulesEngine.fire(rules, facts);

        System.out.println("\n\n\n");
        System.out.println(facts.getFact("user_attributes"));
    }
}


