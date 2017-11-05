/*
 * This is the original version of the JUnit Internals ComparisonCompactor from 
 * figure 15-2.
 *
 *  The junit team's code for this class is at https://github.com/junit-team/junit4/blob/master/src/main/java/junit/framework/ComparisonCompactor.java
 *
 */
package junit.framework;

import java.lang.AssertionError;

public class ComparisonCompactor {
    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";
    
    private int contextLength;
    private String expected;
    private String actual;
    private String compactExpected;
    private String compactActual;
    private int prefixLength;
    private int suffixLength;
    
    public ComparisonCompactor(int contextLength, String expected, String actual){
        this.contextLength = contextLength;
        this.expected = expected;
        this.actual = actual;
    }
    
    public String compactedComparison(String message){
        if(canBeCompacted()){
            compactExpectedAndActual();
            return Assert.format(message, compactExpected, compactActual);
        }
        else{
            return Assert.format(message, expected, actual);
        }        
    }
    
    private void compactExpectedAndActual(){
        findCommonPrefixAndSuffix();
        compactExpected = compactString(expected);
        compactActual = compactString(actual);
    }
    
    private void findCommonPrefixAndSuffix(){
        findCommonPrefix();
        suffixLength = 0;
        for(;!suffixOverlapsPrefix(suffixLength); suffixLength++){
            if(charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength))
                break;
        }
    }
    
    private char charFromEnd(String s, int i){
        return s.charAt(s.length() - i - 1);
    }
    
    private boolean suffixOverlapsPrefix(int suffixLength){
        return actual.length() - suffixLength <= prefixLength 
                || expected.length() - suffixLength <= prefixLength;
    }
    
    private boolean canBeCompacted(){
        return expected != null && actual != null && !areStringsEqual();
    }
    
    private String compactString(String source){
        return
            computeCommonPrefix() + DELTA_START + 
                source.substring(prefixLength, source.length() - suffixLength) +
                DELTA_END + computeCommonSuffix();
    }
    
    private void findCommonPrefix(){
        int end = Math.min(expected.length(), actual.length());
        for(;prefixLength < end; prefixLength++){
            if (expected.charAt(prefixLength) != actual.charAt(prefixLength))
                break;
        }        
    }
        
    private String computeCommonPrefix(){
        return (prefixLength > contextLength ? ELLIPSIS: "") + 
                expected.substring(Math.max(0, prefixLength - contextLength), prefixLength);
    }
    
    private String computeCommonSuffix(){
        int end = Math.min(expected.length() - suffixLength + contextLength, expected.length());
        return expected.substring(expected.length() - suffixLength, end) + 
                (expected.length() - suffixLength < expected.length() - contextLength ? ELLIPSIS : "");
    }
    
    private boolean areStringsEqual(){
        return expected.equals(actual);
    }
}
