// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.model.search.impl

import com.intellij.model.search.LeafOccurrenceMapper
import com.intellij.model.search.SearchContext
import com.intellij.psi.impl.search.WordRequestInfoImpl
import com.intellij.psi.impl.search.runSearch
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor

internal class SearchWordQuery<T>(
  private val myParameters: SearchWordQueryBuilderImpl.Parameters,
  private val mapper: LeafOccurrenceMapper<T>
) : AbstractDecomposableQuery<T>() {

  override fun processResults(consumer: Processor<in T>): Boolean {
    return runSearch(myParameters.project, this, consumer)
  }

  override fun decompose(): PrimitiveRequests<T> {
    val searchScope = myParameters.searchScope
    if (!makesSenseToSearch(searchScope)) {
      return PrimitiveRequests.empty()
    }
    return PrimitiveRequests(Requests(wordRequests = listOf(
      WordRequest(
        WordRequestInfoImpl(
          myParameters.word,
          searchScope,
          myParameters.caseSensitive,
          SearchContext.mask(myParameters.searchContexts),
          myParameters.containerName
        ),
        myParameters.injection,
        mapper.asTransformation()
      )
    )))
  }

  private fun makesSenseToSearch(searchScope: SearchScope): Boolean {
    return if (searchScope is LocalSearchScope) {
      searchScope.scope.isNotEmpty()
    }
    else {
      searchScope !== GlobalSearchScope.EMPTY_SCOPE
    }
  }
}
