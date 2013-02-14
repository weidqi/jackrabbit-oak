/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.segment;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.plugins.memory.MemoryChildNodeEntry;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeBuilder;
import org.apache.jackrabbit.oak.plugins.segment.MapRecord.Entry;
import org.apache.jackrabbit.oak.spi.state.AbstractNodeState;
import org.apache.jackrabbit.oak.spi.state.ChildNodeEntry;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

class SegmentNodeState extends AbstractNodeState {

    public static RecordId getRecordIdIfAvailable(NodeState state) {
        if (state instanceof SegmentNodeState) {
            SegmentNodeState sstate = (SegmentNodeState) state;
            return sstate.recordId;
        }
        return null;
    }

    private final SegmentReader reader;

    private final RecordId recordId;

    private NodeTemplate template = null;

    SegmentNodeState(SegmentReader reader, RecordId id) {
        this.reader = checkNotNull(reader);
        this.recordId = checkNotNull(id);
    }

    private synchronized NodeTemplate getTemplate() {
        if (template == null) {
            template = reader.readTemplate(reader.readRecordId(recordId, 0));
        }
        return template;
    }

    @Override
    public long getPropertyCount() {
        return getTemplate().getPropertyCount();
    }

    @Override @CheckForNull
    public PropertyState getProperty(String name) {
        checkNotNull(name);
        return getTemplate().getProperty(name, reader, recordId);
    }

    @Override @Nonnull
    public Iterable<PropertyState> getProperties() {
        return getTemplate().getProperties(reader, recordId);
    }

    @Override
    public long getChildNodeCount() {
        return getTemplate().getChildNodeCount(reader, recordId);
    }

    @Override
    public boolean hasChildNode(String name) {
        return getTemplate().hasChildNode(checkNotNull(name), reader, recordId);
    }

    @Override @CheckForNull
    public NodeState getChildNode(String name) {
        return getTemplate().getChildNode(checkNotNull(name), reader, recordId);
    }

    @Override
    public Iterable<String> getChildNodeNames() {
        return getTemplate().getChildNodeNames(reader, recordId);
    }

    @Override @Nonnull
    public Iterable<? extends ChildNodeEntry> getChildNodeEntries() {
        return getTemplate().getChildNodeEntries(reader, recordId);
    }

    @Override @Nonnull
    public NodeBuilder builder() {
        return new MemoryNodeBuilder(this);
    }

}
