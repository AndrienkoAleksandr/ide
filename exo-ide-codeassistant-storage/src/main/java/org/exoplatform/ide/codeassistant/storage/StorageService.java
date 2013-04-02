/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.codeassistant.storage;

import org.exoplatform.ide.codeassistant.jvm.CodeAssistantException;
import org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage;
import org.exoplatform.ide.codeassistant.jvm.shared.JavaType;
import org.exoplatform.ide.codeassistant.jvm.shared.ShortTypeInfo;
import org.exoplatform.ide.codeassistant.jvm.shared.TypeInfo;
import org.exoplatform.ide.codeassistant.storage.lucene.DataIndexFields;
import org.exoplatform.ide.codeassistant.storage.lucene.IndexType;
import org.exoplatform.ide.codeassistant.storage.lucene.LuceneInfoStorage;
import org.exoplatform.ide.codeassistant.storage.lucene.search.*;

import java.util.List;
import java.util.Set;

import static org.exoplatform.ide.codeassistant.storage.lucene.search.AndLuceneSearchConstraint.and;
import static org.exoplatform.ide.codeassistant.storage.lucene.search.DependecySearchConstraint.inArtifacts;
import static org.exoplatform.ide.codeassistant.storage.lucene.search.FieldPrefixSearchConstraint.prefix;
import static org.exoplatform.ide.codeassistant.storage.lucene.search.SearchByFieldConstraint.eq;
import static org.exoplatform.ide.codeassistant.storage.lucene.search.SearchByFieldConstraint.eqJavaType;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class StorageService implements CodeAssistantStorage {

    private final int DEFAULT_RESULT_LIMIT = 200;

    private final int DEFAULT_PACKAGE_RESULT_LIMIT = 15000;

    private final LuceneQueryExecutor queryExecutor;

    /** @param infoStorage */
    public StorageService(LuceneInfoStorage infoStorage) {
        this.queryExecutor = new LuceneQueryExecutor(infoStorage);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getAnnotations(java.lang.String, java.util.Set) */
    @Override
    public List<ShortTypeInfo> getAnnotations(String prefix, Set<String> dependencys) throws CodeAssistantException {
        return queryExecutor
                .executeQuery(
                        new ShortTypeInfoExtractor(),
                        IndexType.JAVA,
                        and(and(eqJavaType(JavaType.ANNOTATION), prefix(DataIndexFields.CLASS_NAME, prefix)),
                            inArtifacts(dependencys)), DEFAULT_RESULT_LIMIT, 0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getClasses(java.lang.String, java.util.Set) */
    @Override
    public List<ShortTypeInfo> getClasses(String prefix, Set<String> dependencys) throws CodeAssistantException {
        return queryExecutor.executeQuery(new ShortTypeInfoExtractor(), IndexType.JAVA,
                                          and(and(eqJavaType(JavaType.CLASS), prefix(DataIndexFields.CLASS_NAME, prefix)),
                                              inArtifacts(dependencys)),
                                          DEFAULT_RESULT_LIMIT, 0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getClassJavaDoc(java.lang.String, java.util.Set) */
    @Override
    public String getClassJavaDoc(String fqn, Set<String> dependencys) throws CodeAssistantException {
        return getMemberJavaDoc(fqn, dependencys);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getInterfaces(java.lang.String, java.util.Set) */
    @Override
    public List<ShortTypeInfo> getInterfaces(String prefix, Set<String> dependencys) throws CodeAssistantException {
        return queryExecutor
                .executeQuery(
                        new ShortTypeInfoExtractor(),
                        IndexType.JAVA,
                        and(and(eqJavaType(JavaType.INTERFACE), prefix(DataIndexFields.CLASS_NAME, prefix)),
                            inArtifacts(dependencys)), DEFAULT_RESULT_LIMIT, 0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getMemberJavaDoc(java.lang.String, java.util.Set) */
    @Override
    public String getMemberJavaDoc(String fqn, Set<String> dependencys) throws CodeAssistantException {
        List<String> searchResult =
                queryExecutor.executeQuery(new JavaDocExtractor(), IndexType.DOC,
                                           and(eq(DataIndexFields.FQN, fqn), inArtifacts(dependencys)), 1, 0);
        if (searchResult.isEmpty()) {
            throw new CodeAssistantException(404, "Not found");
        }
        return searchResult.get(0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getTypeByFqn(java.lang.String, java.util.Set) */
    @Override
    public TypeInfo getTypeByFqn(String fqn, Set<String> dependencys) throws CodeAssistantException {
        List<TypeInfo> searchResult =
                queryExecutor.executeQuery(new TypeInfoExtractor(), IndexType.JAVA,
                                           and(eq(DataIndexFields.FQN, fqn), inArtifacts(dependencys)), 1, 0);
        return searchResult.size() == 1 ? searchResult.get(0) : null;
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getTypesByFqnPrefix(java.lang.String, java.util.Set) */
    @Override
    public List<ShortTypeInfo> getTypesByFqnPrefix(String fqnPrefix, Set<String> dependencys)
            throws CodeAssistantException {
        return queryExecutor.executeQuery(new ShortTypeInfoExtractor(), IndexType.JAVA,
                                          and(prefix(DataIndexFields.FQN, fqnPrefix), inArtifacts(dependencys)), DEFAULT_RESULT_LIMIT, 0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getTypesByNamePrefix(java.lang.String, java.util.Set) */
    @Override
    public List<ShortTypeInfo> getTypesByNamePrefix(String namePrefix, Set<String> dependencys)
            throws CodeAssistantException {
        return queryExecutor.executeQuery(new ShortTypeInfoExtractor(), IndexType.JAVA,
                                          and(prefix(DataIndexFields.CLASS_NAME, namePrefix), inArtifacts(dependencys)),
                                          DEFAULT_RESULT_LIMIT, 0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getTypesInfoByNamePrefix(java.lang.String, java.util.Set) */
    @Override
    public List<TypeInfo> getTypesInfoByNamePrefix(String namePrefix, Set<String> dependencys)
            throws CodeAssistantException {
        return queryExecutor.executeQuery(new TypeInfoExtractor(), IndexType.JAVA,
                                          and(prefix(DataIndexFields.CLASS_NAME, namePrefix), inArtifacts(dependencys)),
                                          DEFAULT_RESULT_LIMIT, 0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getPackages(java.lang.String, java.util.Set) */
    @Override
    public List<String> getPackages(String packagePrefix, Set<String> dependencys) throws CodeAssistantException {
        return queryExecutor.executeQuery(new PackageExtractor(), IndexType.PACKAGE,
                                          and(prefix(DataIndexFields.PACKAGE, packagePrefix), inArtifacts(dependencys)),
                                          DEFAULT_RESULT_LIMIT, 0);
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage#getAllPackages(java.util.Set) */
    @Override
    public List<String> getAllPackages(Set<String> dependencys) throws CodeAssistantException {
        return queryExecutor.executeQuery(new PackageExtractor(), IndexType.PACKAGE,
                                          inArtifacts(dependencys), DEFAULT_PACKAGE_RESULT_LIMIT, 0);
    }

}
