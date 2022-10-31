import java.util.Scanner;
import java.util.stream.IntStream;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int n = scanner.nextInt();
        long count = IntStream.rangeClosed(a, b).filter(i -> i % n == 0).count();
        System.out.println(count);
    }
}