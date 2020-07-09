package encryptdecrypt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length - 1; i++) {
            if (i % 2 == 0) {
                map.put(args[i], args[i + 1]);
            }
        }
        String mode = Optional.ofNullable(map.get("-mode")).orElse("enc");
        int key = Integer.parseInt(Optional.ofNullable(map.get("-key")).orElse("0"));
        String data = map.get("-data");
        String in = map.get("-in");
        String out = map.get("-out");
        String alg = map.get("-alg");
        if (data == null && in == null) {
            data = "";
        } else if (data == null && in != null) {
            try {
                data = Files.readString(Path.of(in));
            } catch (IOException e) {
                System.out.println("Error");
            }
        }
        char[] chars = data.toCharArray();
        char[] result = new char[chars.length];

        ModelFactory.getResult(mode, key, alg, chars, result);
        ModelFactory.getResult(mode, key, alg, chars, result);

        if (out == null) {
            System.out.println(result);
        } else {
            try (FileWriter fileWriter = new FileWriter(new File(out))) {
                fileWriter.write(result);
            } catch (IOException e) {
                System.out.println("Error");
            }
        }

    }

}

interface model {
    void encrypt(int key, char[] data, char[] result);

    void decrypt(int key, char[] data, char[] result);
}

class ShiftMode implements model {

    @Override
    public void encrypt(int key, char[] data, char[] result) {
        for (int i = 0; i < data.length; i++) {
            char c = data[i];
            if (c == ' ') {
                result[i] = ' ';
                continue;
            }
            char cha = (char) (data[i] + key % 26);
            if (c >= 'a' && c <= 'z') {
                if (cha > 'z') {
                    result[i] = (char) ((cha - 'z') + 96);
                } else {
                    result[i] = cha;
                }
            } else if (c >= 'A' && c <= 'Z') {
                if (cha > 'Z') {
                    result[i] = (char) ((cha - 'Z') + 64);
                } else {
                    result[i] = cha;
                }
            } else {
                result[i] = c;
            }
        }
    }

    @Override
    public void decrypt(int key, char[] data, char[] result) {
        for (int i = 0; i < data.length; i++) {
            char c = data[i];
            if (c == ' ') {
                result[i] = ' ';
                continue;
            }
            char cha = (char) (data[i] - key % 26);
            if (c >= 'a' && c <= 'z') {
                if (cha < 'a') {
                    result[i] = (char) ('z' + 1 - ('a' - cha));
                } else {
                    result[i] = cha;
                }
            } else if (c >= 'A' && c <= 'Z') {
                if (cha < 'A') {
                    result[i] = (char) ('Z' + 1 - ('A' - cha));
                } else {
                    result[i] = cha;
                }
            } else {
                result[i] = c;
            }
        }
    }
}

class UnicodeModel implements model {
    @Override
    public void encrypt(int key, char[] data, char[] result) {
        for (int j = 0; j < data.length; j++) {
            result[j] = (char) (data[j] + key);
        }
    }

    @Override
    public void decrypt(int key, char[] data, char[] result) {
        for (int i = 0; i < data.length; i++) {
            result[i] = (char) (data[i] - key);
        }
    }
}

class ModelFactory {
    public static void getResult(String mode, int key, String alg, char[] data, char[] result) {
        model model;
        if ("unicode".equals(alg)) {
            model = new UnicodeModel();
        } else {
            model = new ShiftMode();
        }
        if ("enc".equals(mode)) {
            model.encrypt(key, data, result);
        } else {
            model.decrypt(key, data, result);
        }
    }
}