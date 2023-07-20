package com.example.dingo.common

// from https://www.baeldung.com/kotlin/observer-pattern
interface IObserver {
    fun update()
}

interface IObservable {
    val observers: ArrayList<IObserver>

    fun add(observer: IObserver) {
        observers.add(observer)
    }

    fun remove(observer: IObserver) {
        observers.remove(observer)
    }

    fun sendUpdate() {
        observers.forEach {
            it.update()
        }
    }
}