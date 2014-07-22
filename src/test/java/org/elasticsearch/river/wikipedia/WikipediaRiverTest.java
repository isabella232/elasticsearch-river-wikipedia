/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.river.wikipedia;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.common.base.Predicate;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.junit.annotations.Network;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * This test requires internet connexion
 * If you want to run this test, use -Dtests.network=true
 */
@Network
public class WikipediaRiverTest extends ElasticsearchIntegrationTest {
    @Test
    public void testWikipediaRiver() throws IOException, InterruptedException {
        logger.info(" --> create wikipedia river");
        index("_river", "wikipedia", "_meta", jsonBuilder()
                .startObject()
                    .field("type", "wikipedia")
                    .startObject("index")
                        .field("bulk_size", 100)
                        .field("flush_interval", "100ms")
                    .endObject()
                .endObject());

        logger.info(" --> waiting for some documents");
        // Check that docs are indexed by the river
        assertThat(awaitBusy(new Predicate<Object>() {
            public boolean apply(Object obj) {
                try {
                    refresh();
                    CountResponse response = client().prepareCount("wikipedia").get();
                    logger.info("  -> got {} docs in {} index", response.getCount());
                    return response.getCount() > 0;
                } catch (IndexMissingException e) {
                    return false;
                }
            }
        }, 1, TimeUnit.MINUTES), equalTo(true));
    }
}
