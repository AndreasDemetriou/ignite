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

package org.apache.ignite.internal.processors.cache.distributed.dht.preloader;

import org.apache.ignite.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.processors.cache.*;
import org.apache.ignite.internal.util.tostring.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.plugin.extensions.communication.*;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Force keys response. Contains absent keys.
 */
public class GridDhtForceKeysResponse<K, V> extends GridCacheMessage<K, V> implements GridCacheDeployable {
    /** */
    private static final long serialVersionUID = 0L;

    /** Future ID. */
    private IgniteUuid futId;

    /** Mini-future ID. */
    private IgniteUuid miniId;

    /** */
    @GridDirectCollection(byte[].class)
    private Collection<byte[]> missedKeyBytes;

    /** Missed (not found) keys. */
    @GridToStringInclude
    @GridDirectTransient
    private Collection<K> missedKeys;

    /** Cache entries. */
    @GridToStringInclude
    @GridDirectTransient
    private List<GridCacheEntryInfo<K, V>> infos;

    /** */
    private byte[] infosBytes;

    /**
     * Required by {@link Externalizable}.
     */
    public GridDhtForceKeysResponse() {
        // No-op.
    }

    /**
     * @param cacheId Cache ID.
     * @param futId Request id.
     * @param miniId Mini-future ID.
     */
    public GridDhtForceKeysResponse(int cacheId, IgniteUuid futId, IgniteUuid miniId) {
        assert futId != null;
        assert miniId != null;

        this.cacheId = cacheId;
        this.futId = futId;
        this.miniId = miniId;
    }

    /** {@inheritDoc} */
    @Override public boolean allowForStartup() {
        return true;
    }

    /**
     * @return Keys.
     */
    public Collection<K> missedKeys() {
        return missedKeys == null ? Collections.<K>emptyList() : missedKeys;
    }

    /**
     * @return Forced entries.
     */
    public Collection<GridCacheEntryInfo<K, V>> forcedInfos() {
        return infos == null ? Collections.<GridCacheEntryInfo<K,V>>emptyList() : infos;
    }

    /**
     * @return Future ID.
     */
    public IgniteUuid futureId() {
        return futId;
    }

    /**
     * @return Mini-future ID.
     */
    public IgniteUuid miniId() {
        return miniId;
    }

    /**
     * @param key Key.
     */
    public void addMissed(K key) {
        if (missedKeys == null)
            missedKeys = new ArrayList<>();

        missedKeys.add(key);
    }

    /**
     * @param info Entry info to add.
     */
    public void addInfo(GridCacheEntryInfo<K, V> info) {
        assert info != null;

        if (infos == null)
            infos = new ArrayList<>();

        infos.add(info);
    }

    /** {@inheritDoc}
     * @param ctx*/
    @Override public void prepareMarshal(GridCacheSharedContext<K, V> ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        if (missedKeys != null && missedKeyBytes == null)
            missedKeyBytes = marshalCollection(missedKeys, ctx);

        if (infos != null) {
            marshalInfos(infos, ctx);

            infosBytes = ctx.marshaller().marshal(infos);
        }
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext<K, V> ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        if (missedKeys == null && missedKeyBytes != null)
            missedKeys = unmarshalCollection(missedKeyBytes, ctx, ldr);

        if (infosBytes != null) {
            infos = ctx.marshaller().unmarshal(infosBytes, ldr);

            unmarshalInfos(infos, ctx.cacheContext(cacheId()), ldr);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneCallsConstructors"})
    @Override public MessageAdapter clone() {
        GridDhtForceKeysResponse _clone = new GridDhtForceKeysResponse();

        clone0(_clone);

        return _clone;
    }

    /** {@inheritDoc} */
    @Override protected void clone0(MessageAdapter _msg) {
        super.clone0(_msg);

        GridDhtForceKeysResponse _clone = (GridDhtForceKeysResponse)_msg;

        _clone.futId = futId;
        _clone.miniId = miniId;
        _clone.missedKeyBytes = missedKeyBytes;
        _clone.missedKeys = missedKeys;
        _clone.infos = infos;
        _clone.infosBytes = infosBytes;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("all")
    @Override public boolean writeTo(ByteBuffer buf) {
        writer.setBuffer(buf);

        if (!super.writeTo(buf))
            return false;

        if (!typeWritten) {
            if (!writer.writeByte(null, directType()))
                return false;

            typeWritten = true;
        }

        switch (state) {
            case 3:
                if (!writer.writeIgniteUuid("futId", futId))
                    return false;

                state++;

            case 4:
                if (!writer.writeByteArray("infosBytes", infosBytes))
                    return false;

                state++;

            case 5:
                if (!writer.writeIgniteUuid("miniId", miniId))
                    return false;

                state++;

            case 6:
                if (!writer.writeCollection("missedKeyBytes", missedKeyBytes, byte[].class))
                    return false;

                state++;

        }

        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("all")
    @Override public boolean readFrom(ByteBuffer buf) {
        reader.setBuffer(buf);

        if (!super.readFrom(buf))
            return false;

        switch (state) {
            case 3:
                futId = reader.readIgniteUuid("futId");

                if (!reader.isLastRead())
                    return false;

                state++;

            case 4:
                infosBytes = reader.readByteArray("infosBytes");

                if (!reader.isLastRead())
                    return false;

                state++;

            case 5:
                miniId = reader.readIgniteUuid("miniId");

                if (!reader.isLastRead())
                    return false;

                state++;

            case 6:
                missedKeyBytes = reader.readCollection("missedKeyBytes", byte[].class);

                if (!reader.isLastRead())
                    return false;

                state++;

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 43;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridDhtForceKeysResponse.class, this, super.toString());
    }
}
