public class Degree {

    public static String run(String[] args) {
        int t = Integer.valueOf(args[1]);
        int n = Integer.valueOf(args[2]);
        long res = 1;
        for (int i = 0; i < n; i++) {
            res *= t;
        }
        return Long.toString(res);
    }
}
