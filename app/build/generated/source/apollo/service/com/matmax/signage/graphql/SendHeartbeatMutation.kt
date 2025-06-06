//
// AUTO-GENERATED FILE. DO NOT MODIFY.
//
// This class was automatically generated by Apollo GraphQL version '3.8.2'.
//
package com.matmax.signage.graphql

import com.apollographql.apollo3.annotations.ApolloAdaptableWith
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CompiledField
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.api.obj
import com.matmax.signage.graphql.adapter.SendHeartbeatMutation_ResponseAdapter
import com.matmax.signage.graphql.adapter.SendHeartbeatMutation_VariablesAdapter
import com.matmax.signage.graphql.selections.SendHeartbeatMutationSelections
import kotlin.Boolean
import kotlin.String
import kotlin.Unit

public data class SendHeartbeatMutation(
  public val deviceId: String,
  public val status: Optional<String?> = Optional.Absent,
  public val timestamp: Optional<String?> = Optional.Absent,
) : Mutation<SendHeartbeatMutation.Data> {
  public override fun id(): String = OPERATION_ID

  public override fun document(): String = OPERATION_DOCUMENT

  public override fun name(): String = OPERATION_NAME

  public override fun serializeVariables(writer: JsonWriter,
      customScalarAdapters: CustomScalarAdapters): Unit {
    SendHeartbeatMutation_VariablesAdapter.toJson(writer, customScalarAdapters, this)
  }

  public override fun adapter(): Adapter<Data> = SendHeartbeatMutation_ResponseAdapter.Data.obj()

  public override fun rootField(): CompiledField = CompiledField.Builder(
    name = "data",
    type = com.matmax.signage.graphql.type.Mutation.type
  )
  .selections(selections = SendHeartbeatMutationSelections.__root)
  .build()

  @ApolloAdaptableWith(SendHeartbeatMutation_ResponseAdapter.Data::class)
  public data class Data(
    public val sendHeartbeat: SendHeartbeat,
  ) : Mutation.Data

  public data class SendHeartbeat(
    public val success: Boolean,
    public val message: String?,
  )

  public companion object {
    public const val OPERATION_ID: String =
        "455e12f2771549dc5f947448613974156b5f156d139381f44a9fa4a634cc9464"

    /**
     * The minimized GraphQL document being sent to the server to save a few bytes.
     * The un-minimized version is:
     *
     * mutation SendHeartbeat($deviceId: String!, $status: String, $timestamp: String) {
     *   sendHeartbeat(input: {
     *     deviceId: $deviceId
     *     status: $status
     *     timestamp: $timestamp
     *   }
     *   ) {
     *     success
     *     message
     *   }
     * }
     */
    public val OPERATION_DOCUMENT: String
      get() =
          "mutation SendHeartbeat(${'$'}deviceId: String!, ${'$'}status: String, ${'$'}timestamp: String) { sendHeartbeat(input: { deviceId: ${'$'}deviceId status: ${'$'}status timestamp: ${'$'}timestamp } ) { success message } }"

    public const val OPERATION_NAME: String = "SendHeartbeat"
  }
}
