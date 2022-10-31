import java.util.List;
import java.util.Scanner;

class Geek {

    private String type;
    private List<String> languages;
    private int experience;

    Geek(String type, List<String> languages, int experience) {
        this.type = type;
        this.languages = languages;
        this.experience = experience;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public static class GeekBuilder {

        private String type;
        private List<String> languages;
        private int experience;

        public GeekBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public GeekBuilder setLanguages(List<String> languages) {
            this.languages = languages;
            return this;
        }

        public GeekBuilder setExperience(int experience) {
            this.experience = experience;
            return this;
        }

        public Geek build() {
            /* write your code here */
            return new Geek(type, languages, experience);
        }
    }

    public String toString() {
        return "Type : " + type + "\n" +
                "Languages : " + languages + "\n" +
                "Experience : " + experience + " years";
    }
}

class TestDrive {
    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final String geekName = scanner.nextLine();
        final int geekAge = scanner.nextInt();
        System.out.println("Geek " + geekName + " created.");

        Geek geek = new Geek.GeekBuilder()
                .setType("Admin")
                .setLanguages(List.of("Perl", "PowerShell"))
                .setExperience(geekAge)
                .build();

        System.out.println(geek);
    }
}