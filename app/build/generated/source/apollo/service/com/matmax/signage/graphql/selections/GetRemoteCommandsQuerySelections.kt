//
// AUTO-GENERATED FILE. DO NOT MODIFY.
//
// This class was automatically generated by Apollo GraphQL version '3.8.2'.
//
package com.matmax.signage.graphql.selections

import com.apollographql.apollo3.api.CompiledArgument
import com.apollographql.apollo3.api.CompiledField
import com.apollographql.apollo3.api.CompiledSelection
import com.apollographql.apollo3.api.CompiledVariable
import com.apollographql.apollo3.api.list
import com.apollographql.apollo3.api.notNull
import com.matmax.signage.graphql.type.GraphQLID
import com.matmax.signage.graphql.type.GraphQLString
import com.matmax.signage.graphql.type.RemoteCommand
import kotlin.collections.List

public object GetRemoteCommandsQuerySelections {
  private val __remoteCommands: List<CompiledSelection> = listOf(
        CompiledField.Builder(
          name = "id",
          type = GraphQLID.type.notNull()
        ).build(),
        CompiledField.Builder(
          name = "type",
          type = GraphQLString.type.notNull()
        ).build(),
        CompiledField.Builder(
          name = "payload",
          type = GraphQLString.type
        ).build(),
        CompiledField.Builder(
          name = "issuedAt",
          type = GraphQLString.type
        ).build()
      )

  public val __root: List<CompiledSelection> = listOf(
        CompiledField.Builder(
          name = "remoteCommands",
          type = RemoteCommand.type.notNull().list().notNull()
        ).arguments(listOf(
          CompiledArgument.Builder("deviceId", CompiledVariable("deviceId")).build()
        ))
        .selections(__remoteCommands)
        .build()
      )
}
