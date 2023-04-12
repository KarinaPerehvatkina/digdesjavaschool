package com.digdes.school;

public class Token {
    public enum Type {
        LITERAL, IDENTIFIER, OPERATOR, KEYWORD
    }
    public enum SubType {
        BOOL, DOUBLE, LONG, STRING, NULLABLE
    };

    public String subtypeStr() {
        String subtypeStr = "";

        switch(subType) {
            case BOOL -> subtypeStr = "boolean";
            case LONG -> subtypeStr = "long";
            case DOUBLE -> subtypeStr = "double";
            case STRING -> subtypeStr = "string";
            case NULLABLE -> subtypeStr = "null";
            default -> subtypeStr = "unknown";
        }

        return subtypeStr;
    }

    public String typeToString() {
        String typeStr = "";

        switch(type) {
            case KEYWORD -> typeStr = "keyword";
            case LITERAL -> typeStr = "literal";
            case IDENTIFIER -> typeStr = "identifier";
            case OPERATOR -> typeStr = "operator";
            default -> typeStr = "unknown";
        }

        return typeStr;
    }

    @Override
    public String toString() {
        return "[com.digdes.school.Token; " + typeToString() + " | " + value + "]";
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SubType getSubType() {
        return subType;
    }

    public void setSubType(SubType subType) {
        this.subType = subType;
    }

    private Type type;
    private String value;
    private SubType subType;

    public Token(Type type, SubType subType, String val) {
        this.type = type;
        this.subType = subType;
        this.value = val;
    }
}