package com.hedvig.client

data class GraphQLQueryWrapper(
    val query: String,
    val variables: String? = null
)
