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
    
    private int _contextLength;
    private String _expected;
    private String _actual;
    private String compactExpected;
    private String compactActual;
    private int prefixLength;
    private int suffixIndex;
    
    public ComparisonCompactor(int contextLength, String expected, String actual){
        this._contextLength = contextLength;
        _expected = expected;
        _actual = actual;
    }
    
    public String compactedComparison(String message){
        if(canBeCompacted()){
            compactExpectedAndActual();
            return Assert.format(message, compactExpected, compactActual);
        }
        else{
            return Assert.format(message, _expected, _actual);
        }        
    }
    
    private void compactExpectedAndActual(){
        findCommonPrefixAndSuffix();
        compactExpected = compactString(_expected);
        compactActual = compactString(_actual);
    }
    
    private void findCommonPrefixAndSuffix(){
        findCommonPrefix();
        int suffixLength = 1;
        for(;!suffixOverlapsPrefix(suffixLength); suffixLength++){
            if(charFromEnd(_expected, suffixLength) != charFromEnd(_actual, suffixLength))
                break;
        }
        suffixIndex = suffixLength;
    }
    
    private char charFromEnd(String s, int i){
        return s.charAt(s.length() - i);
    }
    
    private boolean suffixOverlapsPrefix(int suffixLength){
        return _actual.length() - suffixLength < prefixLength 
                || _expected.length() - suffixLength < prefixLength;
    }
    
    private boolean canBeCompacted(){
        return _expected != null && _actual != null && !areStringsEqual();
    }
    
    private String compactString(String source){
        String result = DELTA_START + 
                source.substring(prefixLength, source.length() - suffixIndex + 1) + DELTA_END;
        if(prefixLength > 0)
            result = computeCommonPrefix() + result;
        if(suffixIndex > 0)
                result = result + computeCommonSuffix();
        return result;
    }
    
    private int findCommonPrefix(){
        int prefixIndex = 0;
        int end = Math.min(_expected.length(), _actual.length());
        for(;prefixIndex < end; prefixIndex++){
            if (_expected.charAt(prefixIndex) != _actual.charAt(prefixIndex))
                break;
        }
        
        return prefixIndex;
    }
        
    private String computeCommonPrefix(){
        return (prefixLength > _contextLength ? ELLIPSIS: "") + 
                _expected.substring(Math.max(0, prefixLength - _contextLength), prefixLength);
    }
    
    private String computeCommonSuffix(){
        int end = Math.min(_expected.length() - suffixIndex + 1 + _contextLength, _expected.length());
        return _expected.substring(_expected.length() - suffixIndex + 1, end) + 
                (_expected.length() - suffixIndex + 1 < _expected.length() - _contextLength ? ELLIPSIS : "");
    }
    
    private boolean areStringsEqual(){
        return _expected.equals(_actual);
    }
}
