/*
 *  Created on: 2025-08-28
 *      Author: bingtao.yin@transwarp.io
 */

#pragma once

#include "pipeline/exec/argo_scan_operator.h"
#include "vec/exec/scan/vscanner.h"

namespace doris::vectorized {

class ArgoScanner : public VScanner {
    ENABLE_FACTORY_CREATOR(ArgoScanner);

public:
    ArgoScanner(RuntimeState* state, doris::pipeline::ArgoScanLocalState* parent, int64_t limit,
                RuntimeProfile* profile);
    Status open(RuntimeState* state) override;
    Status close(RuntimeState* state) override;

    Status prepare(RuntimeState* state, const VExprContextSPtrs& conjuncts) override;

protected:
    Status _get_block_impl(RuntimeState* state, Block* block, bool* eos) override;
};

} // namespace doris::vectorized
