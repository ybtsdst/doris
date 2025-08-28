package org.apache.doris.datasource.argo.source;

import com.google.common.collect.Lists;
import org.apache.doris.analysis.Analyzer;
import org.apache.doris.analysis.TupleDescriptor;
import org.apache.doris.catalog.TableIf;
import org.apache.doris.common.UserException;
import org.apache.doris.datasource.ExternalScanNode;
import org.apache.doris.planner.PlanNodeId;
import org.apache.doris.statistics.StatisticalType;
import org.apache.doris.thrift.TArgoScanNode;
import org.apache.doris.thrift.TPlanNode;
import org.apache.doris.thrift.TPlanNodeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class ArgoScanNode extends ExternalScanNode {

    private static final Logger log = LogManager.getLogger(ArgoScanNode.class);

    private final TableIf table;

    public ArgoScanNode(PlanNodeId id, TupleDescriptor desc) {
        super(id, desc, "ArgoScanNode", StatisticalType.ARGO_SCAN_NODE, false);
        table = desc.getTable();
    }

    @Override
    public void init(Analyzer analyzer) throws UserException {
        super.init(analyzer);
    }

    @Override
    public void init() throws UserException {
        super.init();
    }


    @Override
    public void finalize(Analyzer analyzer) throws UserException {
        createScanRangeLocations();
    }

    @Override
    public void finalizeForNereids() throws UserException {
        createScanRangeLocations();
    }

    @Override
    protected void createScanRangeLocations() throws UserException {
        scanRangeLocations = Lists.newArrayList(createSingleScanRangeLocations(backendPolicy));
    }

    @Override
    protected void toThrift(TPlanNode msg) {
        msg.node_type = TPlanNodeType.ARGO_SCAN_NODE;
        msg.argo_scan_node = new TArgoScanNode();
        msg.argo_scan_node.setTupleId(desc.getId().asInt());
        msg.argo_scan_node.setTableName(table.getName());
        super.toThrift(msg);
    }
}
