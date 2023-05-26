package com.example.catchycraft

data class Product(var amount:String?=null,var description:String?=null,var phoneNumber: String ?=null,var id: String ?=null,var title: String ="",var url: String ?=null){

    override fun toString(): String {
        return title
    }

}

