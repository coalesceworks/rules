package com.umg.utilities.droolsandconfig.util

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

enum Efficacy {
    INSTANCE

    def _trim(String data) {
        data?.trim()
    }

    def writeTofile(String fileName, String content) {
        Files.write(Paths.get(fileName), content.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }

    def deleteFile(String fileName) {
        try {
            Files.delete(Paths.get(fileName));
            println("deleted file :: " + fileName);
        }
        catch (Exception e)
        {
            println("Could not delete file :: "+fileName);
        }
    }

    String toSingleLine(String input)
    {
        return toSingleLine(input, ' ')?.replaceAll("[\n\r]", "")?.trim()
    }

    String toSingleLine(String input, String replaceWith)
    {
        def toSingleLinePattern = ~/\s{2,}/
        String.metaClass.toSingleLine = { (delegate =~ toSingleLinePattern).replaceAll(replaceWith) }
        return input.toSingleLine()
    }
}