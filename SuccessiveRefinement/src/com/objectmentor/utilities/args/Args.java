package com.objectmentor.utilities.args;

//Code available github.com/Leah-Belin/CleanCodeDemo 
//Uncle Bob's code: https://github.com/unclebob/javaargs/commit/53fbebe4a24fb973d546d10be7d465d73c0bf382

import java.util.*;
import java.text.ParseException;

public class Args {
    private String schema;
    private String[] args;
    private boolean valid;
    private Set<Character> unexpectedArguments = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaler> marshalers = new HashMap<Character, ArgumentMarshaler>();
    private Set<Character> argsFound = new HashSet<Character>();
    private int currentArgument;
    private char errorArgument = '\0';

enum ErrorCode{
    OK, 
    MISSING_STRING,
    MISSING_INTEGER,
    INVALID_INTEGER
}    

private ErrorCode errorCode = ErrorCode.OK;

    public Args(String schema, String[]args) throws ParseException, ArgsException{
        this.schema = schema;
        this.args = args;
        valid = parse();
    }
    
    public boolean parse() throws ParseException, ArgsException{
        if(schema.length() == 0 && args.length == 0)
            return true;
        parseSchema();
        parseArguments();
        return unexpectedArguments.size() == 0;
    }
    
    private boolean parseSchema() throws ParseException{
        for(String element: schema.split(",")){
            if(element.length() > 0){
                String trimmedElement = element.trim();
                parseSchemaElement(element);
            }            
        }
        return true;
    }
    
    private void parseSchemaElement(String element) throws ParseException{
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if(isBooleanSchemaElement(elementTail))
            parseBooleanSchemaElement(elementId);
        else if(isStringSchemaElement(elementTail))
            parseStringSchemaElement(elementId);
        else if(isIntegerSchemaElement(elementTail))
            parseIntegerSchemaElement(elementId);
    }
    
    private void validateSchemaElementId(char elementId) throws ParseException{
        if(!Character.isLetter(elementId)){
            throw new ParseException(
            "Bad character:" + elementId + "in Args format:" + schema,0);
        }
    }
        
    private boolean isStringSchemaElement(String elementTail){
        return elementTail.equals("*");
    }
    
    private boolean isBooleanSchemaElement(String elementTail){
        return elementTail.length() == 0;
    }
    
    private boolean isIntegerSchemaElement(String elementTail){
        return elementTail.equals("#");
    }
    
    private void parseBooleanSchemaElement(char elementId){
        marshalers.put(elementId, new BooleanArgumentMarshaler());
    }
    
    private void parseStringSchemaElement(char elementId){
        marshalers.put(elementId, new StringArgumentMarshaler());
    }
    
    private void parseIntegerSchemaElement(char elementId){
        marshalers.put(elementId, new IntegerArgumentMarshaler());
    }
    
    private boolean parseArguments() throws ArgsException{
        for(currentArgument = 0; currentArgument < args.length; currentArgument++){
            String arg = args[currentArgument];
            parseArgument(arg);
        }            
        return true;
    }
    
    private  void parseArgument(String arg) throws ArgsException{
        if(arg.startsWith("-"))
            parseElements(arg);
    }
    
    private void parseElements(String arg) throws ArgsException{
        for(int i = 1; i < arg.length(); i++)
            parseElement(arg.charAt(i));
    }
    
    private void parseElement(char argChar) throws ArgsException{
        if (setArgument(argChar)) 
            argsFound.add(argChar);            
        else{
            unexpectedArguments.add(argChar);
            valid = false;
        }                    
    }
    
    private boolean setArgument(char argChar) throws ArgsException{
        ArgumentMarshaler m = marshalers.get(argChar);
        try{
            if(m  instanceof BooleanArgumentMarshaler)
                setBooleanArg(m);
            else if(m instanceof StringArgumentMarshaler)
                setStringArg(m);
            else if(m instanceof IntegerArgumentMarshaler)
                setIntArg(m);
            else
                return false;
        }catch(ArgsException e){
            valid = false;
            errorArgument = argChar;
            throw e;
        }
        return true;
    }
    
    private void setStringArg(ArgumentMarshaler m) throws ArgsException{
        currentArgument++;
        try{
            m.set(args[currentArgument]);
        }catch(ArrayIndexOutOfBoundsException e){
            errorCode = ErrorCode.MISSING_STRING;
            throw new ArgsException();
        }
    }
    
    private void setBooleanArg(ArgumentMarshaler m){
        try{
            m.set("true");
        }catch(ArgsException e){            
        }
    }
    
    private void setIntArg(ArgumentMarshaler m) throws ArgsException{
        currentArgument++;
        String parameter = null;
        try{
            parameter = args[currentArgument];
            m.set(parameter);
        }catch(ArrayIndexOutOfBoundsException e){
            errorCode = ErrorCode.MISSING_INTEGER;
            throw new ArgsException();
        }catch(NumberFormatException e){
            errorCode = ErrorCode.INVALID_INTEGER;
            throw e;
        }
    }
  
    public int cardinality(){
        return argsFound.size();
    }
    public String usage(){
        if(schema.length() > 0)
            return "-["+schema+"]"; 
        else
            return "";
    }
    
    public String errorMesage() throws Exception{
        if(unexpectedArguments.size() > 0) {
            return unexpectedArgumentMessage();
        }else
            switch(errorCode){
                case MISSING_STRING:
                    return String.format("Could not find stirng parameter for -%c.", errorArgument);
                case OK:
                    throw new Exception("TILT:Should not get here.");
            }
        return "";
    }
    
    private String unexpectedArgumentMessage(){
        StringBuffer message = new StringBuffer("Argument(s)-");
        for(char c:unexpectedArguments){
            message.append(c);
        }
        message.append("unexpected.");
        
        return message.toString();
    }
    
    public boolean getBoolean(char arg){
        Args.ArgumentMarshaler am = marshalers.get(arg);
        boolean b = false;
        try{
            b = am != null && (Boolean) am.get();
        }catch(ClassCastException e){
            b = false;
        }
        return b;
    }
    
    
    private boolean falseIfNull(Boolean b){
        return b == null?false:b;
    }
    
    public String getString(char arg){
        Args.ArgumentMarshaler am = marshalers.get(arg);
        try{
           return am == null ? "" : (String)am.get(); 
        }catch(ClassCastException e){
            return "";
        }        
    }
    
    public int getInt(char arg){
        Args.ArgumentMarshaler am = marshalers.get(arg);
        try{
            return am == null ? 0 : (Integer)am.get();
        }catch(Exception e){
            return 0;
        }
    }
    
    public boolean has(char arg){
        return argsFound.contains(arg);
    }
    
    public boolean isValid(){
        return valid;
    }
    
    private abstract class ArgumentMarshaler{        
        public abstract void set(String s) throws ArgsException;
        public abstract Object get();
    }
    
    private class BooleanArgumentMarshaler extends ArgumentMarshaler{
        private boolean booleanValue = false;

        public void set(String s){
            booleanValue = true;
        }
        
        public Object get(){
            return booleanValue;
        }
    }
    
    private class StringArgumentMarshaler extends ArgumentMarshaler{
        private String stringValue = "";

        public void set(String s){
            stringValue = s;
        }
        
        public Object get(){
            return stringValue;
        }
    }
    
    private class IntegerArgumentMarshaler extends ArgumentMarshaler{
        private int intValue = 0;
        
        public void set(String s)throws ArgsException{
            try{
                intValue = Integer.parseInt(s);
            }catch(NumberFormatException e){
                throw new ArgsException();
            }
        }
        
        public Object get(){
            return intValue;
        }
    }
}
