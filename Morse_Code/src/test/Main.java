package test;

import Files.Morse;

public class Main {

    public static void main(String[] args) {
        var obj = Morse.getInstance();
        System.out.println(obj.decodeMessage(obj.encodeMessage("Kazem java")));
    }
}
