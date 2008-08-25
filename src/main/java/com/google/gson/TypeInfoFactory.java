/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * A static factory class used to construct the "TypeInfo" objects.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class TypeInfoFactory {

  private TypeInfoFactory() {
    // Not instantiable since it provides factory methods only.
  }

  public static TypeInfoArray getTypeInfoForArray(Type type) {
    Preconditions.checkArgument(TypeUtils.isArray(type));
    return new TypeInfoArray(type);
  }

  /**
   * Evaluates the "actual" type for the field.  If the field is a "TypeVariable" or has a
   * "TypeVariable" in a parameterized type then it evaluates the real type.
   *
   * @param f the actual field object to retrieve the type from
   * @param typeDefiningF the type that contains the field {@code f}
   * @return the type information for the field
   */
  public static TypeInfo getTypeInfoForField(Field f, Type typeDefiningF) {
    Type actualType = getActualTypeOfField(f, typeDefiningF);
    return new TypeInfo(actualType);
  }

  private static Type getActualTypeOfField(Field f, Type typeDefiningF) {
    Class<?> classDefiningF = TypeUtils.toRawClass(typeDefiningF);
    Type type = f.getGenericType();
    if (type instanceof Class
        || type instanceof ParameterizedType
        || type instanceof GenericArrayType
        || type instanceof TypeVariable) {
      return getActualType(type, typeDefiningF, classDefiningF);
    } else {
      throw new IllegalArgumentException("Type \'" + type + "\' is not a Class, "
          + "ParameterizedType, GenericArrayType or TypeVariable. Can't extract type.");
    }
  }

  private static Type getActualType(
      Type typeToEvaluate, Type parentType, Class<?> rawParentClass) {
    if (typeToEvaluate instanceof Class) {
      return typeToEvaluate;
    } else if (typeToEvaluate instanceof GenericArrayType) {
      GenericArrayType castedType = (GenericArrayType) typeToEvaluate;
      Type componentType = castedType.getGenericComponentType();
      Type actualType = getActualType(componentType, parentType, rawParentClass);
      if (componentType.equals(actualType)) {
        return castedType;
      } else {
        return TypeUtils.wrapWithArray(TypeUtils.toRawClass(actualType));
      }
    } else if (typeToEvaluate instanceof TypeVariable) {
      // The class definition has the actual types used for the type variables.
      // Find the matching actual type for the Type Variable used for the field.
      // For example, class Foo<A> { A a; }
      // new Foo<Integer>(); defines the actual type of A to be Integer.
      // So, to find the type of the field a, we will have to look at the class'
      // actual type arguments.
      TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) typeToEvaluate;
      TypeVariable<?>[] classTypeVariables = rawParentClass.getTypeParameters();
      ParameterizedType objParameterizedType = (ParameterizedType) parentType;
      int indexOfActualTypeArgument = getIndex(classTypeVariables, fieldTypeVariable);
      Type[] actualTypeArguments = objParameterizedType.getActualTypeArguments();
      return actualTypeArguments[indexOfActualTypeArgument];
    }
    return typeToEvaluate;
  }

  private static int getIndex(TypeVariable<?>[] types, TypeVariable<?> type) {
    for (int i = 0; i < types.length; ++i) {
      if (type.equals(types[i])) {
        return i;
      }
    }
    throw new IllegalStateException("How can the type variable not be present in the class declaration!");
  }
}
