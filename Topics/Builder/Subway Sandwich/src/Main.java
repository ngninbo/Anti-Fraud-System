import java.util.Scanner;

class Sandwich {

    private String bun;
    private int salad;
    private int cheese;
    private int cucumber;
    private int ham;

    public Sandwich(String bun, int salad, int cheese, int cucumber, int ham) {
        this.bun = bun;
        this.salad = salad;
        this.cheese = cheese;
        this.cucumber = cucumber;
        this.ham = ham;
    }

    public static class Builder {

        private String bun;
        private int salad;
        private int cheese;
        private int cucumber;
        private int ham;

        public Builder addBun(String bun) {
            this.bun = bun;
            return this;
        }

        public Builder addSalad(int salad) {
            this.salad = salad;
            return this;
        }

        public Builder addCheese(int cheese) {
            this.cheese = cheese;
            return this;
        }

        public Builder addCucumber(int cucumber) {
            this.cucumber = cucumber;
            return this;
        }

        public Builder addHam(int ham) {
            this.ham = ham;
            return this;
        }

        public Sandwich build() {
            return new Sandwich(bun, salad, cheese, cucumber, ham);
        }
    }

    @Override
    public String toString() {
        return "Bun : " + bun + "\n" +
                "Salad : " + salad + "\n" +
                "Cheese : " + cheese + "\n" +
                "Cucumber : " + cucumber + "\n" +
                "Ham : " + ham;
    }
}

class TestDrive {
    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        /* Hello, which bun would like to? */
        Sandwich.Builder builder = new Sandwich.Builder();
        String bun = scanner.nextLine();
        /* How much salad would like to add? */
        builder.addBun(bun);
        int salad = Integer.parseInt(scanner.nextLine());
        /* How much cheese would like to add? */
        builder.addSalad(salad);
        int cheese = Integer.parseInt(scanner.nextLine());
        /* How much cucumber would like to add? */
        builder.addCheese(cheese);
        int cucumber = Integer.parseInt(scanner.nextLine());
        /* How much ham would like to add? */
        builder.addCucumber(cucumber);
        int ham = scanner.nextInt();
        builder.addHam(ham);
        Sandwich sandwich = builder.build();

        System.out.println(sandwich);
        scanner.close();
    }
}