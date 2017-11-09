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
        if(!shouldBeCompacted){
            return Assert.format(message, expected, actual);
        }
        
        findCommonPrefixAndSuffix();              
        return Assert.format(message, compact(expected), compact(actual));               
    }
    
    //huh??? why are we doing this, is this just for readability above?  Seems 
    //like a very strange thing to do.
    private boolean shouldBeCompacted(){
        string result = expected == null ||
            actual == null || 
            expected.equals(actual); 
    }
    
    private void findCommonPrefixAndSuffix(){
        findCommonPrefix();
        suffixLength = 0;
        while(!suffixOverlapsPrefix(suffixLengnth)){            
            if(charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength))
                break;
            suffixLength++;
        }
    }
    
    private char charFromEnd(String s, int i){
        return s.charAt(s.length() - i - 1);
    }
    
    private boolean suffixOverlapsPrefix(int suffixLength){
        return actual.length() - suffixLength <= prefixLength 
                || expected.length() - suffixLength <= prefixLength;
    }
       
    private void findCommonPrefix(){
        int end = Math.min(expected.length(), actual.length());
        while(prefixLength < end){
            if (expected.charAt(prefixLength) != actual.charAt(prefixLength))
                break;
            prefixLength++;
        }        
    }
    
    private String compact(String s){
        return new StringBuilder()
                .append(startingEllipsis())
                .append(startingContext())
                .append(DELTA_START)
                .append(delta(s))
                .append(DELTA_END)
                .append(endingContext())
                .append(endingEllipsis())
                .toString();
    }
    
    private String startingEllipsis(){
        return prefixLength> contextLength ? ELLIPSIS : "";
    }
    
    private String startingContext(){
        int contextStart = Math.max(0, prefixLength - contextLength);
        int contextEnd = prefixLength;
        return expected.substring(contextStart, contextEnd);
    }
    
    private String delta(String s){
        int deltaStart = prefixLength;
        int deltaEnd = s.length() - suffixLength;
        return s.substring(deltaStart, deltaEnd);
    }     
    
    private String endingContext(){
        int contextStart = expected.length() - suffixLength;
        int contextEnd = Math.min(contextStart + contextLength, expected.length());
        return expected.substring(contextStart, contextEnd);
    }
    
    private String endingEllipsis(){
        return (suffixLength > contextLength ? ELLIPSIS : "");
    }
}
