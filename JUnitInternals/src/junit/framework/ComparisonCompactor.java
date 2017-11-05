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
    private int prefix;
    private int suffix;
    
    public ComparisonCompactor(int contextLength, String expected, String actual){
        this._contextLength = contextLength;
        _expected = expected;
        _actual = actual;
    }
    
    public String compact(String message){
        if(shouldNotCompact())
            return Assert.format(message, _expected, _actual);
        
        findCommonPrefix();
        findCommonSuffix();
        String compactExpected = compactString(_expected);
        String compactActual = compactString(_actual);
        return Assert.format(message, compactExpected, compactActual);
    }
    
    private boolean shouldNotCompact(){
        return _expected == null || _actual == null || areStringsEqual();
    }
    
    private String compactString(String source){
        String result = DELTA_START + 
                source.substring(prefix, source.length() - suffix + 1) + DELTA_END;
        if(prefix > 0)
            result = computeCommonPrefix() + result;
        if(suffix > 0)
                result = result + computeCommonSuffix();
        return result;
    }
    
    private void findCommonPrefix(){
        prefix = 0;
        int end = Math.min(_expected.length(), _actual.length());
        for(;prefix < end; prefix++){
            if (_expected.charAt(prefix) != _actual.charAt(prefix))
                break;
        }
    }
    
    private void findCommonSuffix(){
        int expectedSuffix = _expected.length() - 1;
        int actualSuffix = _actual.length() - 1;
        for(; actualSuffix >= prefix && expectedSuffix >= prefix;
                actualSuffix--, expectedSuffix--){
            if(_expected.charAt(expectedSuffix) != _actual.charAt(actualSuffix))
                break;
        }
        suffix = _expected.length() - expectedSuffix;
    }
    
    private String computeCommonPrefix(){
        return (prefix > _contextLength ? ELLIPSIS: "") + _expected.substring(Math.max(0, prefix - _contextLength), prefix);
    }
    
    private String computeCommonSuffix(){
        int end = Math.min(_expected.length() - suffix + 1 + _contextLength, _expected.length());
        return _expected.substring(_expected.length() - suffix + 1, end) + 
                (_expected.length() - suffix + 1 < _expected.length() - _contextLength ? ELLIPSIS : "");
    }
    
    private boolean areStringsEqual(){
        return _expected.equals(_actual);
    }
}
