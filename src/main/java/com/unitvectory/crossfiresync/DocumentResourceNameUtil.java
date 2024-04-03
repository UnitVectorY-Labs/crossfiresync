/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.unitvectory.crossfiresync;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The DocumentResourceNameUtil utility class.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class DocumentResourceNameUtil {

    /**
     * The pattern to extract the document path
     */
    private static final Pattern DOCUMENT_PATH_PATTERN = Pattern.compile("projects/.*/databases/.*/documents/(.+)");

    /**
     * The pattern to replace the database id
     */
    private static final Pattern DATABASE_ID_PATTERN = Pattern.compile("(projects/.*/databases/)(.*?)(/documents/.+)");

    /**
     * Extracts the document_path from the resource name using a Matcher.
     * 
     * The resource name in the format of
     * `projects/{project_id}/databases/{database_id}/documents/{document_path}`
     * will have just the `{document_path}` returned.
     * 
     * @param resourceName the resource name
     * @return the document path
     */
    public static String getDocumentPath(String resourceName) {
        // Create a new Matcher from the static Pattern for each call; Matcher is not
        // thread-safe
        Matcher matcher = DOCUMENT_PATH_PATTERN.matcher(resourceName);

        if (matcher.find()) {
            // Return the captured group which is the document path
            return matcher.group(1);
        }

        // No match found, return null or throw an exception
        return null;
    }

    /**
     * Replaces the database segment in the resource name with a new database name.
     * 
     * The resource name in the format of
     * `projects/{project_id}/databases/{database_id}/documents/{document_path}`
     * will have the `{database_id}` replaced.
     *
     * @param resourceName    the original resource name
     * @param newDatabaseName the new database name to replace the old one
     * @return the resource name with the database segment replaced
     */
    public static String setDatabaseId(String resourceName, String newDatabaseName) {
        // Regex to find the database segment
        Matcher matcher = DATABASE_ID_PATTERN.matcher(resourceName);

        if (matcher.find()) {
            // Reconstruct the resourceName with the new database name
            return matcher.group(1) + newDatabaseName + matcher.group(3);
        }

        // If no match found, return the original resourceName or throw an exception
        return resourceName;
    }
}
