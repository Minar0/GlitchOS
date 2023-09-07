package com.whmin.glitchos.intentengine.modules

import java.util.regex.Pattern

class CalendarModule: Module() {
    override val moduleName: String = "Calendar"
    override val intentRegex: Pattern = Pattern.compile("calendar")
    override fun getFullCommandHash(input: String): HashMap<String, String>? {
        TODO("Not yet implemented")
    }

    override fun runCommandFromHash(commandMap: HashMap<String, String>): Boolean {
        TODO("Not yet implemented")
    }
}