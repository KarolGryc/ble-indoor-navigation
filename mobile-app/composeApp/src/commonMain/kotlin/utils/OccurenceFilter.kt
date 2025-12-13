package utils

class Filter<T> (
    private val occurenceThreshold: Int
){
    private var currentValue: T? = null
    private var prevValue: T? = null
    private var occurenceCount = 1

    fun getNewValue(newValue: T?): T? {
        if (prevValue == newValue) {
            occurenceCount++
        }
        else {
            prevValue = newValue
            occurenceCount = 1
        }

        if (occurenceCount >= occurenceThreshold) {
            currentValue = prevValue
        }

        return currentValue
    }
}