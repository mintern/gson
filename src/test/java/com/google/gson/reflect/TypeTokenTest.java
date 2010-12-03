/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;
import junit.framework.TestCase;

/**
 * @author Jesse Wilson
 */
@SuppressWarnings({"UnusedDeclaration", "deprecation"})
public final class TypeTokenTest extends TestCase {

  List<Integer> listOfInteger = null;
  List<Number> listOfNumber = null;
  List<String> listOfString = null;
  List<?> listOfUnknown = null;
  List<Set<String>> listOfSetOfString = null;
  List<Set<?>> listOfSetOfUnknown = null;

  public void testIsAssignableFromRawTypes() {
    assertTrue(TypeToken.get(Object.class).isAssignableFrom(String.class));
    assertFalse(TypeToken.get(String.class).isAssignableFrom(Object.class));
    assertTrue(TypeToken.get(RandomAccess.class).isAssignableFrom(ArrayList.class));
    assertFalse(TypeToken.get(ArrayList.class).isAssignableFrom(RandomAccess.class));
  }

  public void testIsAssignableFromWithTypeParameters() throws Exception {
    Type a = getClass().getDeclaredField("listOfInteger").getGenericType();
    Type b = getClass().getDeclaredField("listOfNumber").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfInteger = listOfNumber; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfNumber = listOfInteger; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

  public void testIsAssignableFromWithBasicWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfString = listOfUnknown  // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    listOfUnknown = listOfString; // compiles; must be true
    assertTrue(TypeToken.get(b).isAssignableFrom(a));
  }

  public void testIsAssignableFromWithNestedWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfSetOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfSetOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfSetOfString = listOfSetOfUnknown; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfSetOfUnknown = listOfSetOfString; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }
}
