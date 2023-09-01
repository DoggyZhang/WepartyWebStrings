package string2excel;

import java.io.*;
import java.util.*;

public class WepartyWeb {

    public static Map<File, File> findStringPath(File project) {
        Map<File, File> map = new HashMap<>();

        //src
        String srcPath = project.getAbsoluteFile() + File.separator + "src";
        File src = new File(srcPath);
        if (src.exists() && src.isDirectory()) {
            System.out.println("翻译: " + srcPath);
            File[] files = src.listFiles();
            if (files != null) {
                for (File modelFile : files) {
                    String modelName = modelFile.getName();
                    if ("anchor".equals(modelName)
                            || "greedy".equals(modelName)
                            || "slot".equals(modelName)
                            || "roomsupport".equals(modelName)
                            || "starlist".equals(modelName)
                            || "invite".equals(modelName)
                            || "vip".equals(modelName)
                    ) {
                        //过滤一些不需要翻译的内容
                        continue;
                    }

                    File localeDir = findLocaleDir(modelFile);
                    if (localeDir == null) {
                        continue;
                    }
                    System.out.println(modelName + " -> " + localeDir.getAbsolutePath());
                    map.put(modelFile, localeDir);
                }
            }
        }

        return map;
    }

    private static File findLocaleDir(File path) {
        if (path == null || !path.exists()) {
            return null;
        }
        if (path.isDirectory() && path.getName().equals("locale")) {
            return path;
        }
        if (!path.isDirectory()) {
            return null;
        }
        File[] files = path.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            File localeDir = findLocaleDir(file);
            if (localeDir != null) {
                return localeDir;
            }
        }
        return null;
    }
}
