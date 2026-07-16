package com.aliothmoon.maameow.automation.remote.engine

import com.aliothmoon.maameow.automation.remote.third.Ln
import java.util.ServiceLoader

class RemoteControllerEngineRegistry(
    factories: Iterable<RemoteControllerEngineFactory> = ServiceLoader.load(RemoteControllerEngineFactory::class.java)
) {
    private val byId: Map<String, RemoteControllerEngineFactory>

    init {
        val map = linkedMapOf<String, RemoteControllerEngineFactory>()
        factories.forEach { factory ->
            val id = factory.descriptor.controllerId
            if (map.containsKey(id)) {
                Ln.e("Duplicate remote controllerId '$id'; keeping first provider")
            } else {
                map[id] = factory
            }
        }
        byId = map
    }

    fun descriptors(): List<RemoteControllerDescriptor> = byId.values.map { it.descriptor }
    fun controllerIds(): List<String> = byId.keys.toList()
    fun factory(controllerId: String): RemoteControllerEngineFactory? = byId[controllerId]
}
