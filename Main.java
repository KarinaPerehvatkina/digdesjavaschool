package com.digdes.school;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        JavaSchoolStarter executor = new JavaSchoolStarter();
        Scanner scanner = new Scanner(System.in);


        while (true) {
            String line = scanner.nextLine();

            var tokens = Tokenizer.tokenize(line);

            try {
                var data = executor.executor(tokens);

                executor.printTable(data);

                System.out.println(data);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}