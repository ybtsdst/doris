package org.apache.doris.nereids.trees.plans.logical;

import org.apache.doris.catalog.TableIf;
import org.apache.doris.nereids.memo.GroupExpression;
import org.apache.doris.nereids.properties.LogicalProperties;
import org.apache.doris.nereids.trees.plans.Plan;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.RelationId;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class LogicalArgoScan extends LogicalCatalogRelation {

    private static final Logger log = LogManager.getLogger(LogicalArgoScan.class);

    public LogicalArgoScan(RelationId relationId, TableIf table, List<String> qualifier) {
        this(relationId, table, qualifier, Optional.empty(), Optional.empty());
    }

    public LogicalArgoScan(RelationId relationId, TableIf table, List<String> qualifier,
                           Optional<GroupExpression> groupExpression, Optional<LogicalProperties> logicalProperties) {
        super(relationId, PlanType.LOGICAL_ARGO_SCAN, table, qualifier, groupExpression, logicalProperties);
    }

    @Override
    public LogicalArgoScan withRelationId(RelationId relationId) {
        return new LogicalArgoScan(relationId, table, qualifier, Optional.empty(), Optional.empty());

    }

    @Override
    public LogicalArgoScan withGroupExpression(Optional<GroupExpression> groupExpression) {
        return new LogicalArgoScan(relationId, table, qualifier, groupExpression,
            Optional.of(getLogicalProperties()));
    }

    @Override
    public LogicalArgoScan withGroupExprLogicalPropChildren(Optional<GroupExpression> groupExpression,
                                                            Optional<LogicalProperties> logicalProperties, List<Plan> children) {
        return new LogicalArgoScan(relationId, table, qualifier, groupExpression, logicalProperties);
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitLogicalArgoScan(this, context);
    }
}
