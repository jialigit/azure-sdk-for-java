// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.purview.administration;

import com.azure.analytics.purview.administration.implementation.MetadataRolesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous PurviewMetadataClient type. */
@ServiceClient(builder = PurviewMetadataClientBuilder.class)
public final class MetadataRolesClient {
    private final MetadataRolesImpl serviceClient;

    /**
     * Initializes an instance of MetadataRoles client.
     *
     * @param serviceClient the service client implementation.
     */
    MetadataRolesClient(MetadataRolesImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Lists roles for Purview Account.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     values: [
     *         {
     *             id: String
     *             name: String
     *             type: String
     *             properties: {
     *                 provisioningState: String
     *                 roleType: String
     *                 friendlyName: String
     *                 description: String
     *                 cnfCondition: [
     *                     [
     *                         {
     *                             attributeName: String
     *                             attributeValueIncludes: String
     *                             attributeValueIncludedIn: [
     *                                 String
     *                             ]
     *                             attributeValueExcludes: String
     *                             attributeValueExcludedIn: [
     *                                 String
     *                             ]
     *                         }
     *                     ]
     *                 ]
     *                 dnfCondition: [
     *                     [
     *                         (recursive schema, see above)
     *                     ]
     *                 ]
     *                 version: Long
     *             }
     *         }
     *     ]
     *     nextLink: String
     * }
     * }</pre>
     *
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @param context The context to associate with this operation.
     * @throws HttpResponseException thrown if status code is 400 or above, if throwOnError in requestOptions is not
     *     false.
     * @return list of Metadata roles.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> list(RequestOptions requestOptions, Context context) {
        return this.serviceClient.list(requestOptions, context);
    }
}
