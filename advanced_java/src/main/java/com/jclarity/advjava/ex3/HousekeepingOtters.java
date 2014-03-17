package com.jclarity.advjava.ex3;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;

/**
 * This tool should allow the user to specify 2 parameters - the Otter to
 * move and the new location where their record should be put. 
 * 
 * Yes, most operating systems already have copy functionality. But the 
 * Diabolical Developer prefers to do everything in Java! 
 * 
 * Why? There is no "why". That's why it's Diabolical! 
 */
public class HousekeepingOtters {

    /**
     * Implement the move using the Files class
     *
     * @param args location of Otter, destination of Otter
     */
    public static void main(String[] args) {
    	if (args.length < 2) usage();
    	
    	final String src = args[0];
    	final String dst = args[1];
    	
        // HINT: The Diabolical Developer has provided some sample otters 
        // in resources/data, you should practice with these and the Path and Files
        // class.
        
        // Bonus points for dealing with incorrect Paths!
        
        // Further bonus points for reading and displaying the contents of the 
        // Otter files

//        jdk7impl(src, dst);

        jdk6impl(src, dst);

    }

    private static void jdk6impl(final String src, final String dst) {
        BufferedReader source = null;
        BufferedWriter dest = null;
        File file = new File(src);
        if (!file.isFile()) {  // handle incorrect path
            return;
        }
        try {
            source = new BufferedReader(new FileReader(file));
            dest = new BufferedWriter(new FileWriter(new File(dst)));
            String line;
            while ((line = source.readLine()) != null) {
                dest.write(line);
                dest.newLine();
                System.out.println(line);  // display the contents
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (source != null) {
                try {source.close();} catch (IOException e) {}
                try {
                    if (dest != null) {dest.close();}} catch (IOException e) {}
            }
        }
    }

    private static void jdk7impl(final String src, final String dst) {
        Path in = Paths.get(src);
        Path out = Paths.get(dst);
        if (in.toFile().isFile()) {
            try {
                displayOtter(in);
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayOtter(final Path path) {
        try {
            for (String line : Files.readAllLines(path, Charset.defaultCharset())) {
                System.out.println(line);
            }
        } catch (IOException e) {


        }
    }

    private static void usage() {
		System.out.println("Usage: HousekeepingOtters <src> <dst>");
		System.exit(-1);
	}
}
