/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.dependencytrack.notification.publisher;

import alpine.logging.Logger;
import alpine.notification.Notification;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.dependencytrack.notification.NotificationScope;
import org.dependencytrack.notification.vo.AnalysisDecisionChange;
import org.dependencytrack.notification.vo.NewVulnerabilityIdentified;
import org.dependencytrack.notification.vo.NewVulnerableDependency;
import org.dependencytrack.util.NotificationUtil;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public interface Publisher {

    void inform(Notification notification, JsonObject config);

    default String prepareTemplate(final Notification notification, final PebbleTemplate template) {
        final Map<String, Object> context = new HashMap<>();
        final long epochSecond = notification.getTimestamp().toEpochSecond(
                ZoneId.systemDefault().getRules()
                        .getOffset(notification.getTimestamp())
        );
        context.put("timestampEpochSecond", epochSecond);
        context.put("timestamp", notification.getTimestamp().toString());
        context.put("notification", notification);

        if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())) {
            if (notification.getSubject() instanceof NewVulnerabilityIdentified) {
                final NewVulnerabilityIdentified subject = (NewVulnerabilityIdentified) notification.getSubject();
                context.put("subject", subject);
                context.put("subjectJson", NotificationUtil.toJson(subject));
            } else if (notification.getSubject() instanceof NewVulnerableDependency) {
                final NewVulnerableDependency subject = (NewVulnerableDependency) notification.getSubject();
                context.put("subject", subject);
                context.put("subjectJson", NotificationUtil.toJson(subject));
            } else if (notification.getSubject() instanceof AnalysisDecisionChange) {
                final AnalysisDecisionChange subject = (AnalysisDecisionChange) notification.getSubject();
                context.put("subject", subject);
                context.put("subjectJson", NotificationUtil.toJson(subject));
            }
        }

        try (Writer writer = new StringWriter()) {
            template.evaluate(writer, context);
            return writer.toString();
        } catch (IOException e) {
            Logger.getLogger(this.getClass()).error("An error was encountered evaluating template", e);
            return null;
        }
    }

}
