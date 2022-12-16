package com.ebay.dap.epic.tdq;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MetaProperty;
import groovy.lang.Script;
import groovy.util.Eval;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

public class GroovyTest {

    @Test
    public void f1(){
        assert Eval.me("33*3").equals(99);
        System.out.println(Eval.x(20, "x * 10"));
        System.out.println(Eval.me("k", 4, "2*k"));
    }

    @Test
    public void f2(){
        GroovyShell groovyShell = new GroovyShell();
        Script script = groovyShell.parse("20  - 10");
        System.out.println(script.run());

        System.out.println(groovyShell.evaluate("40 * 2.5"));

        System.out.println("----------");
        // bind parameter
        Binding sharedData = new Binding();
        GroovyShell shell = new GroovyShell(sharedData);
        sharedData.setProperty("text", "I am shared data!");
        sharedData.setProperty("date", Instant.now());
        System.out.println(shell.evaluate("\"At $date, $text\""));

    }

    @Test
    public void f3(){
        GroovyShell groovyShell = new GroovyShell();
        Script script = groovyShell.parse("x * 100.0 + y");
        List<MetaProperty> properties = script.getMetaClass().getProperties();
        System.out.println(properties);
        properties.forEach(p -> {
            System.out.println(p.getName());
            System.out.println(p.getType());
        });
        Binding binding = new Binding();
        script.setBinding(binding);

        binding.setProperty("x", 30);
        binding.setVariable("y", 30);
        System.out.println(script.run());
        binding.setProperty("x", 50);
        System.out.println(script.run());
    }
}
