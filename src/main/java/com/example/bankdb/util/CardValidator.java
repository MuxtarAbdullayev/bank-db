package com.example.bankdb.util;

public class CardValidator {

    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            return false;
        }

        int sum = 0;
        boolean doubleDigit = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            char ch = cardNumber.charAt(i);
            if (!Character.isDigit(ch)) {
                return false;
            }
            int digit = Character.getNumericValue(ch);

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit / 10) + (digit % 10);
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
