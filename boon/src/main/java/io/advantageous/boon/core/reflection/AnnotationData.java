/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon.core.reflection;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.advantageous.boon.Str.uncapitalize;


/**
 * This is a helper class that helps us extract annotation data
 * from the Annotations.
 *
 * This was pulled over from crank. I have not given in a thorough review yet.
 *
 * @author Rick Hightower
 */
public class AnnotationData {

    /**
     * The name of the className of the annotation.
     */
    private String annotationClassName;

    /**
     * The simple name of the annotation.
     */
    private String annotationSimpleName;

    /**
     * The package of the annotation.
     */
    private String annotationPackageName;

    private Set<String> allowedAnnotations;

    private String name;

    private Map<String, Object> values;

    public AnnotationData( Annotation annotation ) {
        this( annotation, new HashSet<String>() );
    }

    public AnnotationData( Annotation annotation, Set<String> allowedAnnotations ) {

        this.annotationSimpleName = annotation.annotationType().getSimpleName ();
        this.annotationClassName = annotation.annotationType().getName ();
        this.annotationPackageName = annotationClassName.substring ( 0, annotationClassName.length ()
                - annotationSimpleName.length () - 1 );
        this.allowedAnnotations = allowedAnnotations;
        this.name = uncapitalize( annotationSimpleName );
        this.values = doGetValues(annotation);
    }




    /**
     * Determines if this is an annotation we care about.
     * Checks to see if the package name is in the set.
     * @return allowed
     */
    public boolean isAllowed() {
        if (allowedAnnotations ==null || allowedAnnotations.size ()==0) return true;
        return allowedAnnotations.contains( annotationPackageName );
    }

    /**
     * Get the name of the annotation by lowerCasing the first letter
     * of the simple name, e.g., short name Required becomes required.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the values from the annotation.
     * We use reflection to turn the annotation into a simple HashMap
     * of values.
     *
     * @return map of basic types and enums
     */
    Map<String, Object> doGetValues(Annotation annotation) {
        /* Holds the value map. */
        Map<String, Object> values = new HashMap<String, Object>();
        /* Get the declared staticMethodMap from the actual annotation. */
        Method[] methods = annotation.annotationType().getDeclaredMethods();

        final Object[] noargs = ( Object[] ) null;

        /* Iterate through declared staticMethodMap and extract values
         * by invoking decalared staticMethodMap if they are no arg staticMethodMap.
         */
        for ( Method method : methods ) {
            /* If it is a no arg method assume it is an annoation value. */
            if ( method.getParameterTypes().length == 0 ) {
                try {
                    /* Get the value. */
                    Object value = method.invoke( annotation, noargs ); 
                    if (value instanceof Enum) {
                        Enum enumVal = (Enum)value;
                        value = enumVal.name ();
                    }
                    values.put( method.getName(), value );
                } catch ( Exception ex ) {
                    throw new RuntimeException( ex );
                }
            }
        }
        return values;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public String toString() {
        return name;
    }


    public String getFullClassName () {
        return annotationClassName;
    }


    public String getSimpleClassName () {
        return annotationSimpleName;
    }

}
