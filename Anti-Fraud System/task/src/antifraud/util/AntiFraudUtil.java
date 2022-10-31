package antifraud.util;

import java.util.function.Predicate;

public class AntiFraudUtil {

    private transient static final String IPV4_PATTERN =
            "^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$";

    public static Predicate<String> isValidIP() {
        return ip -> ip.matches(IPV4_PATTERN);
    }

    public static Predicate<String> isValidNumber() {
        return number -> {
            int checkSum = Integer.parseInt(String.valueOf(number.charAt(number.length() - 1)));

            for (int i = 0; i < number.length() - 1; i++) {
                int digit = Integer.parseInt(String.valueOf(number.charAt(i)));

                if (i % 2 == 0) {
                    digit = digit * 2;

                    if (digit > 9) {
                        digit = digit - 9;
                    }
                }
                checkSum += digit;
            }

            return checkSum % 10 == 0;
        };
    }
}
