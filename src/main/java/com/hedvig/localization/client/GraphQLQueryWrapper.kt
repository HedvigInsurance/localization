package com.hedvig.localization.client

data class GraphQLQueryWrapper(
    val query: String,
    val variables: String? = null
)
