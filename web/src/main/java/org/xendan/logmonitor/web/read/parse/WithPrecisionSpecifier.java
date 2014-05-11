package org.xendan.logmonitor.web.read.parse;


public class WithPrecisionSpecifier extends SimpleParser {
    public WithPrecisionSpecifier(char letter) {
        super(letter);
    }

    @Override
    protected String decorateSamplePatternString(String samplePatternStr) {
        return super.decorateSamplePatternString(samplePatternStr) + "(\\\\\\{.+\\\\\\})?";
    }

}
