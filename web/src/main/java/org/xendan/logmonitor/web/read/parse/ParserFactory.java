package org.xendan.logmonitor.web.read.parse;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ParserFactory {

    private final static List<UnitParser<?>> LOG4J_PARSERS = Arrays.asList(
            new WithPrecisionSpecifier("caller", 'C'),
            new DateParser(),
            new LevelParser(),
            new SimpleParser("message", 'm'),
            new SimpleParser("thread", 't'),
            new WithPrecisionSpecifier("category", 'c'),
            new SimpleParser("lineNumber", 'L'),
            new SimpleParser("fileName", 'F'),
            new SimpleParser("locationInformation", 'l'),
            new SimpleParser("methodName", 'M'),
            new SimpleParser("elapsedTime", 'r'),
            SimpleParser.createCanBeEmpty("ndc", 'x'),
            new WithPrecisionSpecifier("mdc", 'X'));

    public static List<UnitParser<?>> getParcers(String pattern) {
        return LOG4J_PARSERS.stream()
                .filter( p -> p.isPresentInPattern(pattern))
                .collect(Collectors.toList());
    }
}
