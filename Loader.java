package ConPascal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Loader {
    public static String read(String nameFile){
        String lines = null;
        File file = new File(nameFile);
        try{
            Scanner lector = new Scanner(file);
            lines = "";
            while(lector.hasNext())
                lines += lector.nextLine() + '\n';
        }catch(FileNotFoundException fnfe){
            System.err.println(fnfe.getMessage());
        }
        return lines;
    }
}
