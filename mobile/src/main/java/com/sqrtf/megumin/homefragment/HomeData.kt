package com.sqrtf.megumin.homefragment

import com.sqrtf.common.model.Bangumi

/**
 * Created by roya on 2017/5/24.
 */

open class HomeData constructor(
        val type: TYPE,
        val bangumi: Bangumi? = null,
        val string: String? = null,
        val datas: List<Bangumi>? = null) {

    enum class TYPE(val value: Int) { TITLE(0), CONTAINER(1), WIDE(2), LARGE(3), TAIL(4) }

    constructor () : this(TYPE.TAIL)
    constructor (bgm: Bangumi?) : this(TYPE.WIDE, bgm)
    constructor (newdata: List<Bangumi>?) : this(TYPE.CONTAINER, null, null, newdata)
    constructor (string: String) : this(TYPE.TITLE, null, string)
}