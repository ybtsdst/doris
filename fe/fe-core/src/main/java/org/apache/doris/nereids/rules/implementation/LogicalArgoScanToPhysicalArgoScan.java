package org.apache.doris.nereids.rules.implementation;

import org.apache.doris.nereids.rules.Rule;
import org.apache.doris.nereids.rules.RuleType;
import org.apache.doris.nereids.trees.plans.physical.PhysicalArgoScan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/28.
 */
public class LogicalArgoScanToPhysicalArgoScan extends OneImplementationRuleFactory {

    private static final Logger log = LogManager.getLogger(LogicalArgoScanToPhysicalArgoScan.class);

    public LogicalArgoScanToPhysicalArgoScan() {
    }

    @Override
    public Rule build() {
        return logicalArgoScan().then(
            argoScan -> new PhysicalArgoScan(
                argoScan.getRelationId(),
                argoScan.getTable(),
                argoScan.getQualifier(),
                Optional.empty(),
                argoScan.getLogicalProperties())).toRule(RuleType.LOGICAL_ARGO_SCAN_TO_PHYSICAL_ARGO_SCAN_RULE);
    }
}
