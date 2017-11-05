/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package junit.framework;

public class AssertionFailedError extends AssertionError {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new AssertionFailedError without a detail message.
     */
    public AssertionFailedError() {
    }

    /**
     * Constructs a new AssertionFailedError with the specified detail message.
     * A null message is replaced by an empty String.
     * @param message the detail message. The detail message is saved for later 
     * retrieval by the {@code Throwable.getMessage()} method.
     */
    public AssertionFailedError(String message) {
        super(defaultString(message));
    }

    private static String defaultString(String message) {
        return message == null ? "" : message;
    }
}
