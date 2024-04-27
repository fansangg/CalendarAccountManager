package fan.san.calendarmanager

/**
 *@author  范三
 *@version 2024/4/27
 */

sealed class DeleteState {
	data object None:DeleteState()
	data class DeleteInfo(val id:Long,val name:String):DeleteState()
}