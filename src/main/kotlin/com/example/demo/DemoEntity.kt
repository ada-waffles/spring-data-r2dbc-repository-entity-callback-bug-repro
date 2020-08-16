package com.example.demo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class DemoEntity(@Id val id: Int? = null, val data: String)
