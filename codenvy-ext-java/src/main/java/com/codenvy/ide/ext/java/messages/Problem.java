/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.ext.java.messages;

import com.codenvy.ide.dto.shared.RoutingType;
import com.codenvy.ide.json.JsonArray;
import com.google.gwt.webworker.client.messages.CompactJsonMessage;
import com.google.gwt.webworker.client.messages.Message;
import com.google.gwt.webworker.client.messages.SerializationIndex;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
@RoutingType(type = Message.NON_ROUTABLE_TYPE)
public interface Problem extends Message, CompactJsonMessage {

    @SerializationIndex(1)
    String originatingFileName();

    @SerializationIndex(2)
    String message();

    @SerializationIndex(3)
    int id();

    @SerializationIndex(4)
    JsonArray<String> stringArguments();

    @SerializationIndex(5)
    int severity();

    @SerializationIndex(6)
    int startPosition();

    @SerializationIndex(7)
    int endPosition();

    @SerializationIndex(8)
    int line();

    @SerializationIndex(9)
    int column();
}
