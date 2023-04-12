package com.digdes.school;

import java.util.ArrayList;
import java.util.Locale;

public class Tokenizer {
    private final static String[] OPERATORS = new String[]{
            ">=", "<=", "<", ">", "=", "!=", "like", "ilike"
    };
    private final static String[] KEYWORDS = new String[]{
            "and", "or", "select", "update", "where", "insert", "delete", "values"
    };

    private final static char[] STOP_CHARS = new char[]{
            ',', ' ', ';'
    };

    public static ArrayList<Token> tokenize(String query) {
        query += " ";

        ArrayList<Token> tokens = new ArrayList<>();
        Token.Type lastToken = null;
        Token.SubType lastTokenSub = null;
        int start = 0;

        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);

            if (lastToken != null && isStopChar(ch)) {
                String value = query.substring(start, i);

                if (lastToken == Token.Type.IDENTIFIER) {
                    if (value.equals("true") || value.equals("false")) {
                        lastTokenSub = Token.SubType.BOOL;
                        lastToken = Token.Type.LITERAL;
                    } else if (value.equals("null")) {
                        lastTokenSub = Token.SubType.NULLABLE;
                        lastToken = Token.Type.LITERAL;
                    } else if (isOperator(value)) {
                        lastToken = Token.Type.OPERATOR;
                        value = value.toLowerCase(Locale.ROOT);
                    } else if (isKeyword(value)) {
                        lastToken = Token.Type.KEYWORD;
                        value = value.toLowerCase(Locale.ROOT);
                    }

                }

                var token = new Token(lastToken, lastTokenSub, value);
                tokens.add(token);

                lastToken = null;
                lastTokenSub = null;
                continue;
            }


            if (lastToken == Token.Type.OPERATOR) {
                if (!isOperatorChar(ch)) {
                    String value = query.substring(start, i);

                    var token = new Token(lastToken, lastTokenSub, value);
                    tokens.add(token);

                    lastToken = null;
                    lastTokenSub = null;
                    start = i;
                }
            }

            if (lastToken != null && lastToken != Token.Type.OPERATOR) {

                if (isOperatorChar(ch)) {
                    String value = query.substring(start, i);

                    var token = new Token(lastToken, lastTokenSub, value);
                    tokens.add(token);

                    lastToken = Token.Type.OPERATOR;
                    start = i;
                }
            }

            if (lastToken == null && !isStopChar(ch)) {
                if (ch == '"' || ch == '\'') {
                    lastToken = Token.Type.LITERAL;
                    lastTokenSub = Token.SubType.STRING;

                } else if (Character.isDigit(ch)) {
                    lastToken = Token.Type.LITERAL;
                    lastTokenSub = Token.SubType.LONG;
                } else if (isOperatorChar(ch)) {
                    lastToken = Token.Type.OPERATOR;
                } else {
                    lastToken = Token.Type.IDENTIFIER;
                }

                start = i;
            } else if (lastToken == Token.Type.LITERAL) {
                if ((ch == '"' || ch == '\'') && query.charAt(start) == ch) {
                    String value = query.substring(start + 1, i);

                    var token = new Token(lastToken, lastTokenSub, value);
                    tokens.add(token);

                    lastToken = null;
                    lastTokenSub = null;
                    start = i;
                }

                if (ch == '.' && lastTokenSub == Token.SubType.LONG) {
                    lastTokenSub = Token.SubType.DOUBLE;
                }
            }
        }

        return tokens;
    }

    private static boolean isStopChar(char ch) {
        for (char stop : STOP_CHARS) {
            if (ch == stop) return true;
        }
        return false;
    }

    private static boolean isOperatorChar(char ch) {
        return ch == '!' || ch == '>' || ch == '<' || ch == '=';
    }

    private static boolean isOperator(String str) {
        for (String operator : OPERATORS) {
            if (str.equalsIgnoreCase(operator)) return true;
        }
        return false;
    }

    private static boolean isKeyword(String str) {
        for (String keyword : KEYWORDS) {
            if (str.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }
}
