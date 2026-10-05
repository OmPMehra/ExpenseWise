package com.example.expensewise

import org.junit.Test
import org.junit.Assert.*
import com.example.expensewise.utils.CategoryUtils

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCategoryInitials() {
        assertEquals("BNK", CategoryUtils.getCategoryInitials("Bank Transfer"))
        assertEquals("SAL", CategoryUtils.getCategoryInitials("Salary"))
        assertEquals("FD", CategoryUtils.getCategoryInitials("Food"))
        assertEquals("TRN", CategoryUtils.getCategoryInitials("Transport"))
        assertEquals("SHP", CategoryUtils.getCategoryInitials("Shopping"))
        assertEquals("BIL", CategoryUtils.getCategoryInitials("Bills"))
        assertEquals("ENT", CategoryUtils.getCategoryInitials("Entertainment"))
        assertEquals("EDU", CategoryUtils.getCategoryInitials("Education"))
        assertEquals("HLT", CategoryUtils.getCategoryInitials("Healthcare"))
        assertEquals("RNT", CategoryUtils.getCategoryInitials("Rent"))
        assertEquals("TRV", CategoryUtils.getCategoryInitials("Travel"))
        assertEquals("FLC", CategoryUtils.getCategoryInitials("Freelance"))
        assertEquals("BUS", CategoryUtils.getCategoryInitials("Business"))
        assertEquals("ALW", CategoryUtils.getCategoryInitials("Allowance"))
        assertEquals("INT", CategoryUtils.getCategoryInitials("Interest"))
        assertEquals("GFT", CategoryUtils.getCategoryInitials("Gift"))
        assertEquals("OTH", CategoryUtils.getCategoryInitials("Other"))
        assertEquals("GRO", CategoryUtils.getCategoryInitials("Groceries"))
    }
}
