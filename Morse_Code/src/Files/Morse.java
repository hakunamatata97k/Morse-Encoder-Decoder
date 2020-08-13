package Files;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.unmodifiableBiMap;

public final class Morse {

    private final Path dataBaseFile;
    private BiMap<String, String> data;
    private Charset cs = StandardCharsets.UTF_8;
    private String charSeparationRegex = " ";

    //Singleton Pattern via Lazy Instantiation = private constructor + static object that will be created once!.
    private static Morse defaultObj, pathObj, objWithSeparator, objWithCharSet;

    public static Morse getInstance() {
        if (null == defaultObj)
            defaultObj = new Morse();
        return defaultObj;
    }

    public static Morse getInstance(final Path dataBaseFile) {
        if (null == pathObj)
            pathObj = new Morse(dataBaseFile);
        return pathObj;
    }

    public static Morse getInstance(final Path dataBaseFile, final String separator) {
        if (null == objWithSeparator)
            objWithSeparator = new Morse(dataBaseFile, separator);
        return objWithSeparator;
    }

    public static Morse getInstance(final Path dataBaseFile, final String separator, final Charset cs) {
        if (null == objWithCharSet)
            objWithCharSet = new Morse(dataBaseFile, separator, cs);
        return objWithCharSet;
    }

    private Morse() {
        dataBaseFile = Paths.get(Morse.class.getResource( "Morse.class" ).getPath()).toAbsolutePath().normalize().getParent().resolve("Code.txt");
        checkForDataBase();
    }

    private Morse(final Path dataBaseFile) {
        this.dataBaseFile = dataBaseFile;
        checkForDataBase();
    }

    private Morse(final Path dataBaseFile, final String separator) {
        this (dataBaseFile);
        if ( !separator.contains(".") && !separator.contains("_") ) //those are reserved to the morse code!
            this.charSeparationRegex = separator;
    }

    private Morse(final Path dataBaseFile, final String separator, final Charset cs) {
        this (dataBaseFile, separator);
        this.cs = cs;
    }

    private void checkForDataBase () {
        if (!Files.exists(dataBaseFile))
            System.exit(1);
        data = unmodifiableBiMap(populateFromDataBase());
    }

    private BiMap<String, String> populateFromDataBase () {
        List<String> encodingSchema= new ArrayList<>();

        try {
            encodingSchema = Files.readAllLines(dataBaseFile, cs);
        } catch (IOException e) { e.printStackTrace(); }

        //To prevent the empty of being inserted inside the Hash we need to filter it out!
        return encodingSchema.stream().filter(s -> !s.equals(""))
                .collect(Collectors.toMap(
                        e -> e.replaceAll(charSeparationRegex," ").strip().split("\\s+")[0]
                        ,  e -> e.replaceAll(charSeparationRegex," ").strip().split("\\s+")[1]
                        , (e1, e2) -> e2
                        , HashBiMap::create)
                );
    }

    private StringBuilder decodeHelper (String message) {
        return Arrays.stream(message.split(" "))
                .collect(StringBuilder::new
                        , (builder, s) -> builder.append(data.inverse().getOrDefault(s, " "))
                        , StringBuilder::append
                );
    }

    public String decodeMessage(String message) {
        var builder = new StringBuilder();

        for (var str : message.strip().split("\t"))
            builder.append(decodeHelper(str)).append(" ");

        return builder.toString().strip();
    }

    public String encodeMessage (String message) {

        var builder = new StringBuilder();

        for (var str : message.toUpperCase().strip().split("")) {
            builder.append(data.getOrDefault(str, ""));
            if (!str.equals(" "))
                builder.append(" ");
            else
                builder.append("\t");//insert tap to tell when word ends!.
        }
        return builder.toString().strip();
    }

    public void encodeAndPlayAudio (String data) {
        var encoded = encodeMessage(data).split("\t");
        var tabsNumber = encoded.length-1;

        for (var c : encoded) {
            playAudio(c);

            if (tabsNumber-- > 0){
                System.out.print("\t");
                try { Thread.sleep(10000); } catch (InterruptedException ignored) {  }
            }
        }
        System.out.println();
    }

    private void playMp3 (String filename) {
        try (var fis = new FileInputStream(new File(Morse.class.getResource(filename).getPath()))) {
            new Player(fis).play();
        } catch (IOException |JavaLayerException e) { e.printStackTrace(); }
    }

    private void playAudio (String message) {
        for (var c : message.strip().toCharArray()){
            if (c == '.')
                playMp3("di.mp3");
            else if (c == '_')
                playMp3("dah.mp3");

            System.out.print(c);
        }
    }

    public void writeResultsToFile (String data, Path resultsPath) {
        try {
            Files.writeString(resultsPath, data, StandardOpenOption.CREATE);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Morse morse = (Morse) o;
        return dataBaseFile.equals(morse.dataBaseFile) &&
                data.equals(morse.data);
    }

    @Override
    public int hashCode() { return Objects.hash(dataBaseFile, data); }

    @Override
    public String toString() {
        return "Morse{" +
                "dataBaseFile=" + dataBaseFile +
                ", data=" + data +
                '}';
    }
}
/*
        return Arrays.stream(message.toUpperCase().strip().split(""))
                .collect(StringBuilder::new
                        , (builder, s) -> {
                                builder.append(data.getOrDefault(s, ""));
                                if (!s.equals(" "))
                                    builder.append(" ");
                                else
                                    builder.append("\t");//insert tap to tell when word ends!.
                            }
                        , StringBuilder::append
                ).toString().strip();
 */