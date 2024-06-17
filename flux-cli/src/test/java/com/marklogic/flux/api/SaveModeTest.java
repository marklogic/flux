/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaveModeTest {

    @Test
    void test() {
        assertEquals(SaveMode.values().length,
            org.apache.spark.sql.SaveMode.values().length,
            "Expect each Spark SaveMode value to be associated with one of the values in our own " +
                "SaveMode enum.");
    }
}
