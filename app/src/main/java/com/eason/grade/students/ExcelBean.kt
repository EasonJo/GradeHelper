package com.eason.grade.students

/**
 * 写入 Excel 的数据 Bean
 * @author Eason
 */
data class ExcelBean(
    var studentNo: String,
    var name: String,
    var isRight: String,
    var isRead: String
)