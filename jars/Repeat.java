public class Repeat {

    public static String run(String[] args) {
        int n = Integer.valueOf(args[1]);
        String msg = args[2];
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        for (int i = 0; i < n; i++) {
            stringBuilder.append(msg);
            if (i != n - 1)
                stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
