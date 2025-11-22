package com.example.holoverse.auth.domain.entities

open class User(

    open val userId: String? =null,
    open val fullName: String? =null,
    open val email: String? =null,
    open val accountType: UserType
)

data class Student(
    override val userId: String?=null,
    override val fullName:String?=null,
    override val email: String?=null,
   // override val accountType: UserType = UserType.Student,
    val favouriteTopic: List<String> =emptyList(),
    val studyMajor: String?=null
): User(accountType =UserType.Student){



}

data class Teacher(
    override val userId: String?=null,
    override val fullName:String?=null,
    override val email: String?=null,
    val universityDegree: String?=null,
    val activeHours: Int?=null,
    val rating: Int?=null,
    val bio: String?=null
): User(accountType =UserType.Teacher)

enum class UserType{
    Student , Teacher
}