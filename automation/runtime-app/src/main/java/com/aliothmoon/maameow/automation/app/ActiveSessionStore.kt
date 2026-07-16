package com.aliothmoon.maameow.automation.app

class ActiveSessionStore {
    @Volatile private var active: ActiveSession? = null
    fun get(): ActiveSession? = active
    fun set(session: ActiveSession?) { active = session }
    fun clear() { active = null }
}
