package MorseResolver;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.unmodifiableBiMap;

/**
 * This class represents Encoder and Decoder for MorseResolver code.
 * @author  Kazem Aljalabi.
 */
public final class Morse {



    //immutable variables.
    private final BiMap<String, String> data;
    private final Path dataBaseFile;
    private final Charset cs;
    private final String charSeparationRegex;

    //Constants..
    private final static String DEFAULT_SEPARATOR = " ";
    private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final static Map<Morse, Morse> factory= new HashMap<>();



    /**
     * This Method creates a class instance of type {@link Morse} if not created before else return the already created object.
     * @return a class instance of type {@link Morse}.
     */
    public static Morse getInstance () {
        var obj = new Morse();
        return factory.computeIfAbsent(obj, k -> obj);
    }

    /**
     * This Method creates a class instance of type {@link Morse} if not created before else return the already created object.
     * @param dataBaseFile the path to the database which contains the actual decoding and encoding table of the morse code.
     * @return a class instance of type {@link Morse} linked with a database of user's choice via a {@link Path}.
     */
    public static Morse getInstance (final Path dataBaseFile) {
        var obj = new Morse(dataBaseFile);
        return factory.computeIfAbsent(obj, k -> obj);
    }


    /**
     * This Method creates a class instance of type {@link Morse} if not created before else return the already created object.
     * @param dataBaseFile the {@link Path} to the database which contains the actual decoding and encoding table of the morse code.
     * @param separator the regex which will act as a separator between the actual letter and its representation in morse code.
     * @return a class instance of type {@link Morse} linked with database path and a separator.
     */
    public static Morse getInstance (final Path dataBaseFile, final String separator) {
        var obj = new Morse(dataBaseFile, separator);
        return factory.computeIfAbsent(obj, k -> obj);
    }

    /**
     * This Method creates a class instance of type {@link Morse} if not created before else return the already created object.
     * @param dataBaseFile the path to the database which contains the actual decoding and encoding table of the morse code.
     * @param separator the regex which will act as a separator between the actual letter and its representation in morse code.
     * @param cs the {@link Charset} in which the database is written with.
     * @return a class instance of type {@link Morse} linked with the database with a specific path, charset, and separator.
     */
    public static Morse getInstance (final Path dataBaseFile, final String separator, final Charset cs) {
        var obj = new Morse(dataBaseFile, separator, cs);
        return factory.computeIfAbsent(obj, k -> obj);
    }

    /**
     * Constructor to create a class instance of type {@link Morse} with a default database called "Code.txt" placed in the same dir with the class.
     */
    private Morse() {
        this (getDefaultDataBaseFile());
    }

    /**
     * Constructor creates a class instance of type {@link Morse} with a specific database provided by the user via a valid path.
     * @param dataBaseFile the path to the database which contains the actual decoding and encoding table of the morse code.
     */
    private Morse(final Path dataBaseFile) {
        this (dataBaseFile, DEFAULT_SEPARATOR);
    }

    /**
     * Constructor creates a class instance of type {@link Morse} with a custom database with a specific separator provided by the user via a valid path.
     * @param dataBaseFile the {@link Path} to the database which contains the actual decoding and encoding table of the morse code.
     * @param separator the regex which will act as a separator between the actual letter and its representation in morse code.
     */
    private Morse(final Path dataBaseFile, final String separator) {
        this (dataBaseFile, separator, DEFAULT_CHARSET);
    }

    /**
     * Constructor creates a class instance of type {@link Morse} with a custom database with a specific separator provided by the user via a valid path.
     * The database file is written in a specific CharSet.
     * @param dataBaseFile the path to the database which contains the actual decoding and encoding table of the morse code.
     * @param separator the regex which will act as a separator between the actual letter and its representation in morse code.
     * @param cs the {@link Charset} in which the database is written with.
     */
    private Morse(final Path dataBaseFile, final String separator, final Charset cs) {
        Objects.requireNonNull(dataBaseFile, "the Path to the database cant be null!.");
        this.dataBaseFile = dataBaseFile;

        if (cs == null)
            this.cs = DEFAULT_CHARSET;
        else
            this.cs = cs;

        if (separator != null && checkForRegexValidity(separator))
            this.charSeparationRegex = separator;
        else
            this.charSeparationRegex = DEFAULT_SEPARATOR;

        checkForDataBaseExistence();
        data = unmodifiableBiMap(populateFromDataBase());
    }

    /**
     * return the path to the default database.
     * @return {@link Path} to the default database created by the program maker.
     */
    private static Path getDefaultDataBaseFile(){
        return Paths.get(Morse.class.getResource( "Code.txt" ).getPath()).toAbsolutePath();
    }


    /**
     * Method to check the existence of database file based on the given path.
     */
    private void checkForDataBaseExistence() {
        if (!Files.exists(dataBaseFile))
            try {
                throw new FileNotFoundException("THE DATABASE FILE DOESN'T EXISTS!");
            } catch (FileNotFoundException e) { System.exit(1); }
    }

    /**
     * Method to check if the separator provided by the user is a valid regex.
     * @param regex database separator provided by the user.
     * @return true if the regex is valid and doesn't contain any dots nor dashes otherwise it returns false.
     */
    private boolean checkForRegexValidity (final String regex) {
        if (regex.contains(".") || regex.contains("_"))
            return false;
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException exception) { return false; }
        return true;
    }

    /**
     * Method to populate the Database from the database {@link java.io.File}.
     * @return a {@link BiMap} which contains the encoding/decoding schema of the MorseResolver code based on the database file.
     */
    private BiMap<String, String> populateFromDataBase () {
        List<String> encodingSchema = new ArrayList<>();

        try {
            encodingSchema = Files.readAllLines(dataBaseFile, cs);
        } catch (IOException e) { e.printStackTrace(); }

        //To prevent the empty of being inserted inside the Hash we need to filter it out!
        return encodingSchema.stream().filter(s -> !s.equals(""))
                .map(e -> e.replaceAll(charSeparationRegex, " ").strip().split("\\s+"))
                .collect(Collectors.toMap(
                        e -> e[0]
                        , e -> e[1]
                        , (e1, e2) -> e2
                        , HashBiMap::create)
                );
    }

    /**
     * Method which will write a specific message to a given file.
     * @param data The data to be written to a file. the data can be an already encoded message or the decoded message of an already encoded message!.
     * @param resultsPath the path where the results would be written, if it doesn't exist it will be created.
     */
    public void writeResultsToFile (String data, Path resultsPath) {
        try {
            Files.writeString(resultsPath, data, StandardOpenOption.CREATE);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Method to decode a given Message based on the given database and the morse code logic.
     * @param message to be decoded assuming that the message contains only '_' and '.', assuming that the message given contains no foreign chars that don't exist in the database given.
     * @return a decoded version of the provided message.
     */
    public String decodeMessage(String message) {
        var builder = new StringBuilder();

        for (var str : message.strip().split("\t"))
            builder.append(decodeHelper(str)).append(" ");

        return builder.toString().strip();
    }

    /**
     * A helper method to decode One Word at a time.
     * @param word which consists of '_' and '.' which will be encoded accordingly to the given database.
     * @return a valid decoded word.
     */
    private StringBuilder decodeHelper (String word) {
        return Arrays.stream(word.split(" "))
                .collect(StringBuilder::new
                        , (builder, s) -> builder.append(data.inverse().getOrDefault(s, " "))
                        , StringBuilder::append
                );
    }

    /**
     * Method to encode a certain message based on the provided database.
     * @param message to be encoded assuming that the message given contains no foreign chars that don't exist in the database given.
     * @return an encoded version to the provided message which consists of only '_' and '.'.
     */
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

    /**
     * Method to play the actual sound of a certain message while being encoded.
     * @param data to be encoded.
     */
    public void encodeAndPlayAudio (String data) {
        var encoded = encodeMessage(data).split("\t");
        var tabsNumber = encoded.length-1;

        for (var c : encoded) {
            playAudio(c);

            if (tabsNumber-- > 0){
                System.out.print("\t");
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {  }
            }
        }
        System.out.println();
    }

    /**
     * @param filename of the soundtrack to be played.
     */
    private void playMp3 (String filename) {
        try (var fis = new FileInputStream(Morse.class.getResource(filename).getPath())) {
            new Player(fis).play();
        } catch (IOException | JavaLayerException e) { e.printStackTrace(); }
    }

    /**
     * Method to decide which soundtrack will get played based on the current char.
     * @param encodeMessage which will be played.
     */
    private void playAudio (String encodeMessage) {
        for (var c : encodeMessage.strip().toCharArray()){
            if (c == '.')
                playMp3("di.mp3");
            else if (c == '_')
                playMp3("dah.mp3");

            System.out.print(c);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Morse morse = (Morse) o;
        return dataBaseFile.equals(morse.dataBaseFile) &&
                data.equals(morse.data) &&
                cs.equals(morse.cs);
    }

    @Override
    public int hashCode() { return Objects.hash(dataBaseFile, data); }

    @Override
    public String toString() {
        return "MorseResolver{" +
                "dataBaseFile=" + dataBaseFile +
                ", data=" + data +
                ", charset= " + cs +
                '}';
    }
}
