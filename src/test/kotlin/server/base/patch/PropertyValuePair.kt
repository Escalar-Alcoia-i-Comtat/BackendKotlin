package server.base.patch

import database.entity.BaseEntity

class PropertyValuePair<EntityType: BaseEntity, ValueType: Any>(
    val propertyName: String,
    val newValue: ValueType?,
    val propertyValue: (EntityType) -> ValueType?
)
