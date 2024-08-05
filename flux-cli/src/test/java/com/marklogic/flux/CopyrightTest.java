/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux;

import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CopyrightTest {

    /**
     * You can configure Intellij to include the copyright in each new Java file you create via
     * Preferences -> Editor -> File and Code Templates. If you choose to modify the "File Header.java"
     * template under "Includes", you should then modify each of the Java templates - e.g. Class, Interface, etc - to
     * put the "File Header.java" template at the top of the file and not below the package declaration.
     */
    @Test
    void verifyAllJavaFilesHaveCopyright() throws IOException {
        Files.walkFileTree(new File("src").toPath(), new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".java")) {
                    try (FileReader reader = new FileReader(file.toFile())) {
                        String content = FileCopyUtils.copyToString(reader);
                        String message = String.format("Does not start with copyright comment: %s", file.toFile().getAbsolutePath());
                        assertTrue(content.startsWith("/*"), message);
                        assertTrue(
                            content.contains("Copyright © 2024 MarkLogic Corporation. All Rights Reserved."),
                            message
                        );
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
