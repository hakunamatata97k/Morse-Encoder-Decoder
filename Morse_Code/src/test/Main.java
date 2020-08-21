package test;

import MorseResolver.Morse;

public class Main {

    public static void main(String[] args) {
        var obj = Morse.getInstance();
        System.out.println(obj.encodeMessage("cool java"));
        System.out.println(obj.decodeMessage(obj.encodeMessage("cool java")));
        obj.encodeAndPlayAudio("cool java cool");
    }
}
