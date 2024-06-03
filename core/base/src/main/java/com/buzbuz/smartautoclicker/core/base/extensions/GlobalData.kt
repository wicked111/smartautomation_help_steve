package com.buzbuz.smartautoclicker.core.base.extensions

class GlobalData private constructor() {
    var higherLimitText: Long? = null
    var lowerLimitText: Long? = null

    companion object {
        val instance: GlobalData by lazy { GlobalData() }
    }
}

