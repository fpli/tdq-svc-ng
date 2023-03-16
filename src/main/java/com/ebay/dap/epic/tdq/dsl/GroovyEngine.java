package com.ebay.dap.epic.tdq.dsl;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Map;

public class GroovyEngine {

    private static final GroovyShell shell = new GroovyShell();

    public static Integer evalAsInt(String rawGroovyScript, Map<String, Object> bindings) {
        Script script = shell.parse(rawGroovyScript);
        // convention is that all parameters in groovy script should start with '$'
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            String k = entry.getKey();
            if (!k.startsWith("$")) {
                k = "$" + k;
            }
            script.setProperty(k, entry.getValue());
        }

        Object result = script.run();
        return Integer.parseInt(result.toString());
    }

    public static Boolean evalAsBoolean(String rawGroovyScript, Map<String, Object> bindings) {
        //TODO: not implemented
        return null;
    }

    public static Long evalAsLong(String rawGroovyScript, Map<String, Object> bindings) {
        //TODO: not implemented
        return null;
    }

    public static String evalAsString(String rawGroovyScript, Map<String, Object> bindings) {
        //TODO: not implemented
        return null;
    }


    public static Object evalAsObj(String rawGroovyScript, Map<String, Object> bindings) {
        //TODO: not implemented
        return null;
    }

}
