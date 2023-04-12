package com.digdes.school;

import java.util.*;

public class JavaSchoolStarter {
    private final List<Map<String, Object>> table;
    private final Map<String, String> COLUMNS;

    JavaSchoolStarter() {
        table = new ArrayList<>();
        COLUMNS = new HashMap<>();

        COLUMNS.put("id", "long");
        COLUMNS.put("lastName", "string");
        COLUMNS.put("cost", "double");
        COLUMNS.put("age", "long");
        COLUMNS.put("active", "boolean");

        Map<String, Object> test1 = new HashMap<>();
        Map<String, Object> test2 = new HashMap<>();

        test1.put("id", 1);
        test1.put("lastName", "Петров");
        test1.put("cost", 5.4d);
        test1.put("age", 30L);
        test1.put("active", true);


        test2.put("id", 2);
        test2.put("lastName", "Иванов");
        test2.put("cost", 4.3d);
        test2.put("age", 25L);
        test2.put("active", false);


        table.add(test1);
        table.add(test2);

    }

    public List<Map<String, Object>> executor(ArrayList<Token> tokens) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        Token startToken = tokens.get(0);

        switch (startToken.getValue()) {
            case "select" -> result = executeSelect(tokens);
            case "insert" -> result = executeInsert(tokens);
            case "delete" -> result = executeDelete(tokens);
            case "update" -> result = executeUpdate(tokens);
        }

        return result;
    }

    public List<Map<String, Object>> executeSelect(ArrayList<Token> tokens) throws Exception {
        if (tokens.size() <= 1)
            throw new Exception("Invalid command syntax. Select should contain arguments!");

        List<Map<String, Object>> result = new ArrayList<>();

        List<String> columns = new ArrayList<>();
        List<Token> conditions = new ArrayList<>();

        for (int i = 1; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getType() == Token.Type.IDENTIFIER || token.getSubType() == Token.SubType.STRING) {
                String col = token.getValue();

                if (col.equals("*")) {
                    columns.addAll(COLUMNS.keySet());
                    continue;
                }

                if (!COLUMNS.containsKey(col))
                    throw new Exception("Undefined column " + col);

                columns.add(col);
            } else if (token.getType() == Token.Type.KEYWORD && token.getValue().equals("where")) {
                conditions = tokens.subList(i + 1, tokens.size());
                break;
            } else {
                throw new Exception("Invalid syntax near " + token.getValue());
            }
        }

        for (var row : table) {
            if (checkRow(row, conditions)) {
                continue;
            }

            Map<String, Object> selRow = new HashMap<>();

            for (String col : columns) {
                selRow.put(col, row.get(col));
            }

            result.add(selRow);
        }

        return result;
    }

    public List<Map<String, Object>> executeInsert(ArrayList<Token> tokens) throws Exception {
        if (tokens.size() <= 2)
            throw new Exception("Invalid command syntax. INSERT should contain arguments!");

        if (tokens.get(1).getType() != Token.Type.KEYWORD || !tokens.get(1).getValue().equals("values"))
            throw new Exception("Invalid command syntax. INSERT should be followed with VALUES keyword!");

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> newRow = new HashMap<>();

        for (int i = 2; i < tokens.size(); i += 3) {
            Token col, op, value;
            col = tokens.get(i);
            String colname = col.getValue();

            try {
                op = tokens.get(i + 1);
                value = tokens.get(i + 2);
            } catch (Exception e) {
                throw new Exception("Invalid command syntax near: " + colname);
            }

            if (validateOperation(col, op, value) || !op.getValue().equals("=")) {
                throw new Exception("Invalid command syntax near: " + colname);
            }

            if (!COLUMNS.containsKey(colname))
                throw new Exception("Undefined column: " + colname);

            String colType = COLUMNS.get(colname);
            String valType = value.subtypeStr();

            if (validateTypes(op.getValue(), colType, valType)) {
                throw new Exception("Unable to use operator " + op.getValue() + " between types " + colType + " and " + valType);
            }

            String val = value.getValue();

            switch (colType) {
                case "string" -> {
                    newRow.put(colname, value.getValue());
                }
                case "long" -> {
                    long val2 = Integer.parseInt(val);
                    newRow.put(colname, val2);
                }
                case "double" -> {
                    double val2 = Double.parseDouble(val);
                    newRow.put(colname, val2);
                }
                case "boolean" -> {
                    boolean val2 = Boolean.parseBoolean(val);
                    newRow.put(colname, val2);
                }
                case "null" -> newRow.put(colname, null);
            }
        }

        for (String col : COLUMNS.keySet()) {
            if (!newRow.containsKey(col))
                newRow.put(col, null);
        }

        result.add(newRow);
        table.add(newRow);

        return result;
    }

    public List<Map<String, Object>> executeDelete(ArrayList<Token> tokens) throws Exception {
        if (tokens.size() <= 1)
            throw new Exception("Invalid command syntax. DELETE should contain condition!");

        List<Map<String, Object>> result = new ArrayList<>();

        List<String> columns = new ArrayList<>();
        List<Token> conditions = new ArrayList<>();

        for (int i = 1; true; i++) {
            Token token = tokens.get(i);

            if (token.getType() == Token.Type.KEYWORD && token.getValue().equals("where")) {
                conditions = tokens.subList(i + 1, tokens.size());
                break;
            } else {
                throw new Exception("Invalid syntax near " + token.getValue());
            }
        }

        for (int i = 0; i < table.size(); i++) {
            if (checkRow(table.get(i), conditions)) {
                continue;
            }

            result.add(table.get(i));
            table.remove(i);

            i = Math.max(i - 2, -1);
        }

        return result;
    }

    public List<Map<String, Object>> executeUpdate(List<Token> tokens) throws Exception {
        if (tokens.size() <= 2)
            throw new Exception("Invalid command syntax. UPDATE should contain arguments!");

        if (tokens.get(1).getType() != Token.Type.KEYWORD || !tokens.get(1).getValue().equals("values"))
            throw new Exception("Invalid command syntax. UPDATE should be followed with VALUES keyword!");

        List<Map<String, Object>> result = new ArrayList<>();

        List<Token> updates = new ArrayList<>();
        List<Token> conditions = new ArrayList<>();

        for (int i = 1; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getType() == Token.Type.KEYWORD && token.getValue().equals("where")) {
                conditions = tokens.subList(i + 1, tokens.size());
                tokens = tokens.subList(0, i);
                break;
            }
        }

        for (var row : table) {
            if (checkRow(row, conditions)) {
                continue;
            }

            for (int i = 2; i < tokens.size(); i += 3) {
                Token col, op, value;
                col = tokens.get(i);
                String colname = col.getValue();

                try {
                    op = tokens.get(i + 1);
                    value = tokens.get(i + 2);
                } catch (Exception e) {
                    throw new Exception("Invalid command syntax near: " + colname);
                }

                if (validateOperation(col, op, value) || !op.getValue().equals("=")) {
                    throw new Exception("Invalid command syntax near: " + colname);
                }

                if (!COLUMNS.containsKey(colname))
                    throw new Exception("Undefined column: " + colname);

                String colType = COLUMNS.get(colname);
                String valType = value.subtypeStr();

                if (validateTypes(op.getValue(), colType, valType)) {
                    throw new Exception("Unable to use operator " + op.getValue() + " between types " + colType + " and " + valType);
                }

                String val = value.getValue();

                switch (valType) {
                    case "string" -> {
                        row.put(colname, value.getValue());
                    }
                    case "long" -> {
                        long val2 = Integer.parseInt(val);
                        row.put(colname, val2);
                    }
                    case "double" -> {
                        double val2 = Double.parseDouble(val);
                        row.put(colname, val2);
                    }
                    case "boolean" -> {
                        boolean val2 = Boolean.parseBoolean(val);
                        row.put(colname, val2);
                    }
                    case "null" -> row.put(colname, null);
                }


            }
            result.add(row);
        }

        return result;
    }

    private boolean checkRow(Map<String, Object> row, List<Token> conditions) throws Exception {
        if (conditions.isEmpty())
            return false;

        boolean overallResult = true;
        boolean previousResult = true; // assume true for first condition

        for (int i = 0; i < conditions.size(); i += 4) {
            Token col, op, value;
            col = conditions.get(i);
            String colname = col.getValue();

            try {
                op = conditions.get(i + 1);
                value = conditions.get(i + 2);
            } catch (Exception e) {
                throw new Exception("Invalid command syntax near: " + colname);
            }

            boolean currentResult = checkCondition(row, col, op, value);

            if (i == 0) {
                overallResult = currentResult;
            } else {
                Token connector = conditions.get(i - 1);

                if (connector.getType() != Token.Type.KEYWORD || (!connector.getValue().equals("and") && !connector.getValue().equals("or"))) {
                    throw new Exception("Invalid command syntax near: " + connector.getValue());
                }

                if (connector.getValue().equals("and")) {
                    overallResult = overallResult && currentResult && previousResult;
                } else if (connector.getValue().equals("or")) {
                    overallResult = overallResult || currentResult || previousResult;
                }
            }

            previousResult = currentResult;
        }

        return !overallResult;
    }

    private boolean checkCondition(Map<String, Object> row, Token left, Token operator, Token right) throws Exception {
        String col = left.getValue();
        String value = right.getValue();
        String op = operator.getValue();

        if (!COLUMNS.containsKey(col))
            throw new Exception("Condition error: Unknown column: " + col);

        String colType = COLUMNS.get(col);
        String valueType = right.subtypeStr();

        if (valueType.equals("null") || validateOperation(left, operator, right) || validateTypes(operator.getValue(), colType, valueType)) {
            throw new Exception("Unable to use operator " + operator.getValue() + " between types " + colType + " and " + valueType);
        }

        if (colType.equals("string")) {
            String val1 = (String) row.get(col);
            String val2 = right.getValue();

            if (op.equals("=")) return val1.equals(val2);
            if (op.equals("!=")) return !val1.equals(val2);
            if (op.equals("like")) return matchesPattern(val1, val2);
            if (op.equals("ilike")) return matchesPattern(val1.toLowerCase(), val2.toLowerCase());
        } else if (colType.equals("double")) {
            double val1 = (double) row.get(col);
            double val2 = Double.parseDouble(value);

            if (op.equals("=")) return val1 == val2;
            if (op.equals("!=")) return val1 != val2;
            if (op.equals("<")) return val1 < val2;
            if (op.equals(">")) return val1 > val2;
            if (op.equals(">=")) return val1 >= val2;
            if (op.equals("<=")) return val1 <= val2;
        } else if (colType.equals("long")) {
            long val1 = (long) row.get(col);
            long val2 = Integer.parseInt(value);

            if (op.equals("=")) return val1 == val2;
            if (op.equals("!=")) return val1 != val2;
            if (op.equals("<")) return val1 < val2;
            if (op.equals(">")) return val1 > val2;
            if (op.equals(">=")) return val1 >= val2;
            if (op.equals("<=")) return val1 <= val2;
        } else if (colType.equals("boolean")) {
            boolean val1 = (boolean) row.get(col);
            boolean val2 = Boolean.parseBoolean(value);

            if (op.equals("=")) return val1 == val2;
            if (op.equals("!=")) return val1 != val2;
        } else {
            return false;
        }

        return true;
    }

    public boolean validateOperation(Token left, Token operator, Token right) {
        if (left.getType() != Token.Type.IDENTIFIER && left.getSubType() != Token.SubType.STRING) {
            return true;
        }

        if (operator.getType() != Token.Type.OPERATOR)
            return true;

        if (right.getType() != Token.Type.LITERAL)
            return true;

        return false;
    }

    public boolean validateTypes(String operator, String leftType, String rightType) {
        if (rightType.equals("null"))
            return false;

        if (!Objects.equals(leftType, rightType) && (leftType.equals("string") || rightType.equals("string")))
            return true;

        if (!Objects.equals(leftType, rightType) && (leftType.equals("boolean") || rightType.equals("boolean")))
            return true;


        if (!leftType.equals("string") && (operator.equals("like") || operator.equals("ilike")))
            return true;

        if (leftType.equals("string") && !(operator.equals("=") || operator.equals("!=") || operator.equals("like") || operator.equals("ilike")))
            return true;

        return false;
    }

    public static boolean matchesPattern(String str, String pattern) {
        String regex = pattern.replaceAll("%", ".*");
        return str.matches(regex);
    }

    public void printTable(List<Map<String, Object>> table) {
        for (String col : COLUMNS.keySet()) {
            System.out.print(col + "\t");
        }
        System.out.println();

        for (var row : table) {
            for (var col : row.values()) {
                System.out.print(col + "\t");
            }
            System.out.println();
        }
    }
}
