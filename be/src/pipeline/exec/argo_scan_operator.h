/*
 *  Created on: 2025-08-28
 *      Author: bingtao.yin@transwarp.io
 */

#pragma once

#include "common/status.h"
#include "operator.h"
#include "pipeline/exec/scan_operator.h"

namespace doris {}

namespace doris::pipeline {

class ArgoScanOperatorX;
class ArgoScanLocalState final : public ScanLocalState<ArgoScanLocalState> {
public:
    using Parent = ArgoScanOperatorX;
    ENABLE_FACTORY_CREATOR(ArgoScanLocalState);
    ArgoScanLocalState(RuntimeState* state, OperatorXBase* parent)
            : ScanLocalState<ArgoScanLocalState>(state, parent) {}

    Status _init_scanners(std::list<vectorized::VScannerSPtr> *scanners) override;

    std::string name_suffix() const override;
};

class ArgoScanOperatorX final : public ScanOperatorX<ArgoScanLocalState> {
public:
    ArgoScanOperatorX(ObjectPool* pool, const TPlanNode& tnode, int operator_id,
                      const DescriptorTbl& descs, int parallel_tasks);
private:
    friend class ArgoScanLocalState;

    TupleId _tuple_id;
    std::string _table_name;

    TupleDescriptor* _tuple_desc = nullptr;
};

} // namespace doris::pipeline
