package com.example.spotifyclone

open class Event<out T>(private val data : T) {
 var hasbeenHandled = false
    private set
 fun notHandled() : T? {
     return if(hasbeenHandled){
         null
     } else {
         hasbeenHandled = true
         data
     }
 }

 fun peekContent() = data
}