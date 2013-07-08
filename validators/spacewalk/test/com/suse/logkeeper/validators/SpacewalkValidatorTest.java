/*
 * Copyright 2011 SUSE Linux Products GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.suse.logkeeper.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bo
 */
public class SpacewalkValidatorTest {

    public SpacewalkValidatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of inspect method, of class SpacewalkValidator.
     */
    @Test
    public void testInspect() {
        System.out.println("inspect");
        Map<String, String> extmap = new HashMap<String, String>();
        extmap.put("EVT.TYPE", "ADD_LOOSER");

        List expResult = new ArrayList();
        expResult.add("EVT.TYPE");

        assertEquals(expResult, new SpacewalkValidator().inspect(extmap));
    }

    /**
     * Test of validate method, of class SpacewalkValidator.
     */
    @Test
    public void testValidate() {
        System.out.println("validate");

        Map<String, String> extmap = new HashMap<String, String>();
        extmap.put("EVT.TYPE", "ADD_USER");
        extmap.put("EVT.SRC", "WEBUI");

        assertEquals(true, new SpacewalkValidator().validate(extmap));
    }
}