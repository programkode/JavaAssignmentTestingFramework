package assignment;

public class Hello {

    public String printhei() {
        System.out.println("Hello World!"); // Not the right string, tests will fail!");
        return "Hoho";
    }
    public static void main(String[] args) {
        Hello h = new Hello();
        h.printhei();
        // System.out.println("Not the right string, tests will fail!");
    }

}
