package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by nicolas on 01/03/15.
 */
public class TestLong {
    public static void main(String[] args) throws FileNotFoundException {
        long l = Long.valueOf("420649776636362752").longValue();
        System.out.println(l);
        Scanner sc = new Scanner(new File("tweets_dev"));
        int i= 0;
        while (sc.hasNextLine() && (i < 5)) {
            String s = sc.nextLine();
            System.out.println(s.charAt(0)+ "------");
            System.out.println(s.trim().replace(" ", "").substring(1));
            //System.out.println(Long.valueOf(s).longValue());
            i++;
        }
    }
}
