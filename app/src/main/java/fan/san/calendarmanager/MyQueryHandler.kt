package fan.san.calendarmanager

import android.content.AsyncQueryHandler
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log

/**
 *@author  范三
 *@version 2024/4/27
 */

class MyQueryHandler(contentResolver: ContentResolver):AsyncQueryHandler(contentResolver) {



	override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
		val viewModel = cookie as? MainViewModel
		viewModel?.calendarList?.clear()
		cursor?.use {
			if (it.moveToFirst()){
				do {
					val idIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
					val accountNameIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
					val displayNameIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
					val typeIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE)
					val ownerAccountIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.OWNER_ACCOUNT)
					val colorIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)
					val id = it.getLong(idIndex)
					val name = it.getString(accountNameIndex)
					val displayName = it.getString(displayNameIndex)
					val type = it.getString(typeIndex)
					val ownerAccount = it.getString(ownerAccountIndex)
					val color = it.getInt(colorIndex)

					Log.d("fansangg", "id ==  $id-- name == $name -- displayName == $displayName -- type == $type -- ownerAccount == $ownerAccount  color == $color")
					viewModel?.calendarList?.add(CalendarInfoBean(id, name, displayName, type,color))
				}while (it.moveToNext())
			}
		}

	}

	override fun onInsertComplete(token: Int, cookie: Any?, uri: Uri?) {
		Log.d("fansangg", "uri == $uri 创建成功")
		if (uri != null){
			val result = (cookie as? MainViewModel)?.eventFlow?.tryEmit("创建账户成功")
			Log.d("fansangg", "result == $result")
		}else{
			val result = (cookie as? MainViewModel)?.eventFlow?.tryEmit("error")
			Log.d("fansangg", "result == $result")
		}

	}

	override fun onDeleteComplete(token: Int, cookie: Any?, result: Int) {
		Log.d("fansangg", "删除成功 result == $result")
		(cookie as? MainViewModel)?.eventFlow?.tryEmit("删除成功")
	}

}