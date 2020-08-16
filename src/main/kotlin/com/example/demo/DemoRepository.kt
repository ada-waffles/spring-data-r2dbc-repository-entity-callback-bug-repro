package com.example.demo

import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface DemoRepository : CoroutineSortingRepository<DemoEntity, Int>
