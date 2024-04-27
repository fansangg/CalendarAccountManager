package fan.san.calendarmanager

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.provider.CalendarContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow


/**
 *@author  范三
 *@version 2024/4/26
 */

class MainViewModel : ViewModel() {

	val calendarList = mutableStateListOf<CalendarInfoBean>()
	val eventFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
	var deleteState by mutableStateOf<DeleteState>(DeleteState.None)

	fun getCalendarAccountInfo(context: Context) {
		val contentResolver = context.contentResolver
		val myQueryHandler = MyQueryHandler(contentResolver = contentResolver)
		myQueryHandler.startQuery(
			-1,
			this,
			CalendarContract.Calendars.CONTENT_URI,
			arrayOf(
				CalendarContract.Calendars._ID,
				CalendarContract.Calendars.ACCOUNT_NAME,
				CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
				CalendarContract.Calendars.OWNER_ACCOUNT,
				CalendarContract.Calendars.ACCOUNT_TYPE,
				CalendarContract.Calendars.CALENDAR_COLOR,
				CalendarContract.Calendars.CALENDAR_COLOR_KEY,
			),
			null,
			null,
			null
		)
	}

	fun createTestCalendarAccount(context: Context) {

		val myQueryHandler = MyQueryHandler(context.contentResolver)
		val value = ContentValues()
		value.put(CalendarContract.Calendars.NAME, "test")
		value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
		value.put(CalendarContract.Calendars.ACCOUNT_NAME, "test")
		value.put(CalendarContract.Calendars.VISIBLE, 1)
		value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "test")
		value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.RED)
		value.put(CalendarContract.Calendars.OWNER_ACCOUNT, "测试")
		value.put(
			CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
			CalendarContract.Calendars.CAL_ACCESS_OWNER
		)
		value.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
		myQueryHandler.startInsert(-1, this, CalendarContract.Calendars.CONTENT_URI, value)
	}

	fun removeAccount(context: Context, id: Long) {
		val myQueryHandler = MyQueryHandler(context.contentResolver)
		myQueryHandler.startDelete(
			-1,
			this,
			CalendarContract.Calendars.CONTENT_URI,
			"${CalendarContract.Calendars._ID}=?",
			arrayOf(id.toString())
		)
	}
}