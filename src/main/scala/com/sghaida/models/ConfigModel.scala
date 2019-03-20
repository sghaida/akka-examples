package com.sghaida.models

case class ConfigModel(
    modelId: String,
    datarobotProjectId: String,
    datarobotModelId: String,
    routingProxyPath: String,
    routingProxyUrl: String,
    description: String,
    isActive: String,
    created: String
)

case class ConfigModels(configs: List[ConfigModel])