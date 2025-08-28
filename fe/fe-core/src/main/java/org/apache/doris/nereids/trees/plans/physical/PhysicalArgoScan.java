package org.apache.doris.nereids.trees.plans.physical;

import org.apache.doris.catalog.TableIf;
import org.apache.doris.nereids.memo.GroupExpression;
import org.apache.doris.nereids.properties.LogicalProperties;
import org.apache.doris.nereids.properties.PhysicalProperties;
import org.apache.doris.nereids.trees.plans.Plan;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.RelationId;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;
import org.apache.doris.statistics.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class PhysicalArgoScan extends PhysicalCatalogRelation {

    private static final Logger log = LogManager.getLogger(PhysicalArgoScan.class);

    public PhysicalArgoScan(RelationId relationId, TableIf table, List<String> qualifier, Optional<GroupExpression> groupExpression,
                            LogicalProperties logicalProperties) {
        this(relationId, table, qualifier, groupExpression, logicalProperties, null, null);
    }

    public PhysicalArgoScan(RelationId relationId, TableIf table, List<String> qualifier, Optional<GroupExpression> groupExpression,
                            LogicalProperties logicalProperties, PhysicalProperties physicalProperties, Statistics statistics) {
        super(relationId, PlanType.PHYSICAL_ARGO_SCAN, table, qualifier, groupExpression, logicalProperties, physicalProperties,
            statistics);
    }

    @Override
    public PhysicalPlan withPhysicalPropertiesAndStats(PhysicalProperties physicalProperties, Statistics statistics) {
        return new PhysicalArgoScan(relationId, table, qualifier, Optional.empty(), getLogicalProperties(),
            physicalProperties, statistics);
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitPhysicalArgoScan(this, context);
    }

    @Override
    public PhysicalArgoScan withGroupExpression(Optional<GroupExpression> groupExpression) {
        return new PhysicalArgoScan(relationId, table, qualifier, groupExpression, getLogicalProperties());
    }

    @Override
    public PhysicalArgoScan withGroupExprLogicalPropChildren(Optional<GroupExpression> groupExpression,
                                                             Optional<LogicalProperties> logicalProperties,
                                                             List<Plan> children) {
        return new PhysicalArgoScan(relationId, table, qualifier, groupExpression, logicalProperties.get());
    }

}
