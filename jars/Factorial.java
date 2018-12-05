public class Factorial {

    public static String run(String[] args) {
        int n = Integer.valueOf(args[1]);
        long res = 1;
        for (int i = 1; i <= n; i++) {
            res *= i;
        }
        return Long.toString(res);
    }
}
