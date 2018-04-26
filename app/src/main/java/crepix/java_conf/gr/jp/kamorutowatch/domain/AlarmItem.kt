package crepix.java_conf.gr.jp.kamorutowatch.domain

class AlarmItem {
    var id = 0
    var hour = 0
    var minute = 0
    var isRepeated = false
    var isEnabled = true
    var notifySunday = true
    var notifyMonday = true
    var notifyTuesday = true
    var notifyWednesday = true
    var notifyThursday = true
    var notifyFriday = true
    var notifySaturday = true

    fun paste(notification: AlarmItem) {
        id = notification.id
        hour = notification.hour
        minute = notification.minute
        isRepeated = notification.isRepeated
        isEnabled = notification.isEnabled
        notifySunday = notification.notifySunday
        notifyMonday = notification.notifyMonday
        notifyTuesday = notification.notifyTuesday
        notifyWednesday = notification.notifyWednesday
        notifyThursday = notification.notifyThursday
        notifyFriday = notification.notifyFriday
        notifySaturday = notification.notifySaturday
    }
}
