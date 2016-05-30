package org.xendan.logmonitor.web.read.parse;


public class WithPrecisionSpecifier extends SimpleParser {
    public WithPrecisionSpecifier(String name, char letter) {
        super(name, letter);
    }

    @Override
    protected String getPatternSuffix() {
        return "(\\\\\\{.+\\\\\\})?";
    }

}
