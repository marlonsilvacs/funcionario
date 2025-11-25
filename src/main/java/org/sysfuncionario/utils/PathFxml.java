package org.sysfuncionario.utils;

import java.nio.file.Paths;

public class PathFxml {
    public static String pathBase(){
        return Paths.get("src/main/java/org/sysfuncionario/view").toAbsolutePath().toString();
    }
}
