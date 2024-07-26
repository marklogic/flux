/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifyOptionNamesTest {

    /**
     * Verifies that each option found in the listed directories do not contain any option names starting with "--"
     * and followed by any number of uppercase characters. All option names are expected to be lowercase-hyphenated.
     *
     * @throws IOException
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "src",
        "../docs",
        "../examples",
        "../test-app"
    })
    void test(String path) throws IOException {
        final File dir = new File(path);
        AtomicInteger count = new AtomicInteger(0);
        Files.walkFileTree(dir.toPath(), new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if ("../docs".equals(path) && dir.endsWith("assets")) {
                    // Don't need to check any of the docs/assets files.
                    return FileVisitResult.SKIP_SUBTREE;
                } else if ("../test-app".equals(path) && dir.endsWith("caddy")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toFile().getAbsolutePath().endsWith(".jar")) {
                    // All kinds of funky chars in a jar file, we don't need to check it.
                    return FileVisitResult.CONTINUE;
                }
                try (FileReader reader = new FileReader(path.toFile())) {
                    String text = FileCopyUtils.copyToString(reader);
                    Pattern pattern = Pattern.compile("\\-\\-[a-zA-Z]+");
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        String group = matcher.group();
                        for (int i = 0; i < group.length(); i++) {
                            if (Character.isUpperCase(group.charAt(i))) {
                                throw new RuntimeException(String.format("Found option starting with '--' that " +
                                    "contains an uppercase character: %s; file: %s", group, path.toFile()));
                            }
                        }
                        count.incrementAndGet();
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
                throw exc;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println(String.format("Found %d options in directory: %s", count.get(), dir));
        assertTrue(count.get() > 0);
    }
}
