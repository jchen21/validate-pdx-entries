/*
 * Copyright 2024 Broadcom. All rights reserved.
 */

package com.broadcom.gemfire.example;

import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;

import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.pdx.internal.PdxField;
import org.apache.geode.pdx.internal.PdxInstanceImpl;
import org.apache.geode.pdx.internal.PdxType;

import java.util.Map;

import java.util.concurrent.atomic.AtomicInteger;

public class ValidateAllEntriesFunction implements Function, Declarable {

    public void execute(FunctionContext context) {
        AtomicInteger corruptPrimaryEntries = new AtomicInteger(0);
        AtomicInteger corruptSecondaryEntries = new AtomicInteger(0);
        context.getCache().getLogger().info("getPdxReadSerialized() is " + context.getCache().getPdxReadSerialized());
        if (context instanceof RegionFunctionContext) {
            RegionFunctionContext rfc = (RegionFunctionContext) context;
            Region<Object, Object> region = PartitionRegionHelper.getLocalData(rfc.getDataSet());
            rfc.getCache().getLogger().info("ValidateAllEntriesFunction: Validating " + region.size() + " local entries");
            region.entrySet().stream().forEach(entry -> validateEntry(rfc, entry, corruptPrimaryEntries, corruptSecondaryEntries));
            rfc.getCache().getLogger().info("ValidateAllEntriesFunction: Validated " + region.size() + " local entries with " + corruptPrimaryEntries.get() + " corrupt primary and " + corruptSecondaryEntries.get() + " corrupt secondary entries");
        }
        context.getResultSender().lastResult(true);

    }

    private void validateEntry(RegionFunctionContext rfc, Map.Entry<Object,Object> entry, AtomicInteger corruptPrimaryEntries, AtomicInteger corruptSecondaryEntries) {
        try {
            Object value = entry.getValue();
            if (value instanceof PdxInstanceImpl) {
                PdxInstanceImpl pdxInstance = (PdxInstanceImpl) value;
                PdxType pdxType = pdxInstance.getPdxType();
                for (PdxField pdxField : pdxType.getFields()) {
                    String fieldName = pdxField.getFieldName();
                    try {
                        pdxInstance.getField(fieldName);
                    } catch (Exception e) {
                        String isPrimaryStr;
                        if (isPrimary(rfc, entry.getKey())) {
                            isPrimaryStr = "primary";
                            corruptPrimaryEntries.incrementAndGet();
                        } else {
                            isPrimaryStr = "secondary";
                            corruptSecondaryEntries.incrementAndGet();
                        }
                        rfc.getCache().getLogger().warning("For " + isPrimaryStr + " entry with key=" + entry.getKey() + ", failed to get PDX field " + fieldName + " for PDX " + pdxInstance, e);
                        break; // Stopping trying the rest of the PDX fields in the entry
                    }
                }
            }
        } catch (Exception e) {
            String isPrimaryStr = null;
            if (isPrimary(rfc, entry.getKey())) {
                isPrimaryStr = "primary";
                corruptPrimaryEntries.incrementAndGet();
            } else {
                isPrimaryStr = "secondary";
                corruptSecondaryEntries.incrementAndGet();
            }
            rfc.getCache().getLogger().warning("Failed to deserialize value for " + isPrimaryStr + " entry with key=" + entry.getKey(), e);
        }
    }

    private boolean isPrimary(RegionFunctionContext rfc, Object key) {
        return rfc.getCache().getDistributedSystem().getDistributedMember().equals(PartitionRegionHelper.getPrimaryMemberForKey(rfc.getDataSet(), key));
    }

    public String getId() {
        return getClass().getSimpleName();
    }

    public boolean optimizeForWrite() {
        return true;
    }
}
