package string2excel;

import java.io.*;
import java.util.*;

public class WenextWebMono {

    public static Map<File, File> findStringPath(File project) {
        Map<File, File> map = new HashMap<>();

        //packages
        String packagesPath = project.getAbsoluteFile() + File.separator + "packages";
        File packages = new File(packagesPath);
        if (packages.exists() && packages.isDirectory()) {
            System.out.println("翻译: " + packagesPath);
            File[] files = packages.listFiles();
            if (files != null) {
                for (File modelFile : files) {
                    String modelName = modelFile.getName();
                    if ("demo".equals(modelName)) {
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

        //ui
        String uiPath = project.getAbsoluteFile() + File.separator + "ui";
        File ui = new File(uiPath);
        if (ui.exists() && ui.isDirectory()) {
            System.out.println("翻译: " + uiPath);
            String modelName = "ui";
            File localeDir = findLocaleDir(ui);
            if (localeDir != null) {
                System.out.println(modelName + " -> " + localeDir.getAbsolutePath());
                map.put(ui, localeDir);
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
